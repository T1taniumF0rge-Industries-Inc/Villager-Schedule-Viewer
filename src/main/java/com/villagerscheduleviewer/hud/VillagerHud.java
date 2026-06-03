package com.villagerscheduleviewer.hud;

import com.villagerscheduleviewer.brain.VillagerSnapshot;
import com.villagerscheduleviewer.client.VillagerScheduleViewerClient;
import com.villagerscheduleviewer.schedule.ScheduleModel;
import com.villagerscheduleviewer.util.Formatters;
import com.villagerscheduleviewer.util.SnapshotCache;
import com.villagerscheduleviewer.util.Targeting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.passive.VillagerEntity;

public final class VillagerHud {
    private VillagerHud() {}
    public static void render(DrawContext context) {
        if (!VillagerScheduleViewerClient.CONFIG.hudEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        VillagerEntity villager = Targeting.getTargetedVillager(client, VillagerScheduleViewerClient.CONFIG.targetRange);
        if (villager == null || client.textRenderer == null) return;
        VillagerSnapshot s = SnapshotCache.get(villager);
        long time = villager.getWorld().getTimeOfDay();
        int x = 8, y = 8, w = 190, h = 46;
        context.fill(x, y, x+w, y+h, 0x90000000);
        context.drawTextWithShadow(client.textRenderer, s.profession()+" L"+s.level(), x+6, y+6, 0xFFFFFF);
        context.drawTextWithShadow(client.textRenderer, "Now: "+s.activity(), x+6, y+18, 0xE0E0E0);
        context.drawTextWithShadow(client.textRenderer, "Next: "+ScheduleModel.next(s.schedule(), time).id()+" in "+ Formatters.duration(ScheduleModel.ticksUntilNext(s.schedule(), time)), x+6, y+30, 0xE0E0E0);
        if (!s.warnings().isEmpty()) context.drawTextWithShadow(client.textRenderer, "⚠", x+w-16, y+6, 0xFFAA00);
    }
}
