package com.villagerscheduleviewer.diagnostic;

import com.villagerscheduleviewer.brain.MemoryEntry;
import com.villagerscheduleviewer.util.Formatters;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record PoiDetails(PoiKind kind, BlockPos pos, double distance, boolean assigned, boolean reachable, String reachabilityReason) {
    public static PoiDetails missing(PoiKind kind) { return new PoiDetails(kind, null, -1.0, false, false, "No " + kind.displayName().toLowerCase() + " assigned."); }

    public static PoiDetails fromMemory(VillagerEntity villager, List<MemoryEntry> memories, PoiKind kind) {
        return memories.stream().filter(m -> m.name().equals(kind.name())).findFirst()
                .filter(MemoryEntry::valid)
                .map(memory -> fromPos(villager, kind, memory.pos()))
                .orElseGet(() -> missing(kind));
    }

    public static PoiDetails fromPos(VillagerEntity villager, PoiKind kind, BlockPos pos) {
        if (pos == null) return missing(kind);
        double distance = Math.sqrt(villager.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
        boolean closeEnough = distance <= (kind == PoiKind.MEETING_POINT ? 80.0 : 48.0);
        boolean yReasonable = Math.abs(pos.getY() - villager.getBlockY()) <= 12;
        boolean reachable = closeEnough && yReasonable;
        String reason = reachable ? "Likely reachable" : "Possibly unreachable: " + (!closeEnough ? "too far" : "large vertical gap");
        return new PoiDetails(kind, pos, distance, true, reachable, reason);
    }

    public String summary() {
        if (!assigned) return "No " + kind.displayName().toLowerCase() + " assigned.";
        return kind.displayName() + " at " + Formatters.pos(pos) + " (" + Formatters.decimal(distance) + " blocks, " + reachabilityReason + ")";
    }
}
