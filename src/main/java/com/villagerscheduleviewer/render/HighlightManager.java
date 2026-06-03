package com.villagerscheduleviewer.render;

import com.villagerscheduleviewer.client.VillagerScheduleViewerClient;
import com.villagerscheduleviewer.diagnostic.PoiDetails;
import com.villagerscheduleviewer.diagnostic.PoiKind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class HighlightManager {
    private static final List<ActiveHighlight> ACTIVE = new ArrayList<>();
    private HighlightManager() {}

    public static List<ActiveHighlight> active() { return ACTIVE; }

    public static void highlight(PoiDetails details) {
        if (details == null || !details.assigned() || details.pos() == null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) client.player.sendMessage(Text.literal("No " + (details == null ? "POI" : details.kind().displayName().toLowerCase()) + " assigned."), true);
            return;
        }
        highlight(details.kind(), details.pos(), details.summary());
    }

    public static void highlight(PoiKind kind, BlockPos pos, String label) {
        MinecraftClient client = MinecraftClient.getInstance();
        long now = client.world == null ? 0 : client.world.getTime();
        int color = switch (kind) {
            case HOME -> VillagerScheduleViewerClient.CONFIG.bedHighlightColor;
            case JOB_SITE -> VillagerScheduleViewerClient.CONFIG.workstationHighlightColor;
            case MEETING_POINT -> VillagerScheduleViewerClient.CONFIG.meetingHighlightColor;
        };
        ACTIVE.removeIf(active -> active.kind() == kind && active.pos().equals(pos));
        ACTIVE.add(new ActiveHighlight(kind, pos, color, now + VillagerScheduleViewerClient.CONFIG.highlightTimeoutTicks, label));
    }

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        long now = client.world == null ? 0 : client.world.getTime();
        for (Iterator<ActiveHighlight> iterator = ACTIVE.iterator(); iterator.hasNext();) {
            if (iterator.next().expiresAt() < now) iterator.remove();
        }
    }
}
