package com.villagerscheduleviewer.village;

import com.villagerscheduleviewer.brain.VillagerSnapshot;
import com.villagerscheduleviewer.client.VillagerScheduleViewerClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class VillageScanner {
    private static VillageScanResult cached = VillageScanResult.empty(-100_000);
    private VillageScanner() {}

    public static VillageScanResult get(PlayerEntity player) {
        if (player == null || player.getWorld() == null) return cached;
        long now = player.getWorld().getTime();
        if (now - cached.scannedAt() < VillagerScheduleViewerClient.CONFIG.villageScanIntervalTicks) return cached;
        cached = scan(player, now);
        return cached;
    }

    private static VillageScanResult scan(PlayerEntity player, long now) {
        double radius = VillagerScheduleViewerClient.CONFIG.dashboardRadius;
        List<VillagerSnapshot> villagers = player.getWorld().getEntitiesByClass(VillagerEntity.class, player.getBoundingBox().expand(radius), v -> true).stream()
                .map(VillagerSnapshot::capture)
                .sorted(Comparator.comparing(VillagerSnapshot::profession).thenComparing(VillagerSnapshot::entityId))
                .toList();
        Map<BlockPos, List<UUID>> bedClaims = new HashMap<>();
        Map<BlockPos, List<UUID>> jobClaims = new HashMap<>();
        for (VillagerSnapshot villager : villagers) {
            if (villager.home().assigned()) bedClaims.computeIfAbsent(villager.home().pos(), ignored -> new ArrayList<>()).add(villager.uuid());
            if (villager.jobSite().assigned()) jobClaims.computeIfAbsent(villager.jobSite().pos(), ignored -> new ArrayList<>()).add(villager.uuid());
        }
        List<PoiRecord> beds = scanBlocks(player, (int)Math.min(radius, 64), true, bedClaims);
        List<PoiRecord> workstations = scanBlocks(player, (int)Math.min(radius, 64), false, jobClaims);
        int duplicateClaims = duplicateClaims(bedClaims) + duplicateClaims(jobClaims);
        VillageScanResult withoutScores = new VillageScanResult(villagers, beds, workstations, bedClaims.size(), jobClaims.size(), duplicateClaims, null, now);
        return new VillageScanResult(villagers, beds, workstations, bedClaims.size(), jobClaims.size(), duplicateClaims, VillageHealthScores.from(withoutScores), now);
    }

    private static List<PoiRecord> scanBlocks(PlayerEntity player, int radius, boolean beds, Map<BlockPos, List<UUID>> claims) {
        List<PoiRecord> records = new ArrayList<>();
        BlockPos center = player.getBlockPos();
        int scanned = 0;
        for (BlockPos pos : BlockPos.iterate(center.add(-radius, -8, -radius), center.add(radius, 8, radius))) {
            if (++scanned > 250_000) break;
            Block block = player.getWorld().getBlockState(pos).getBlock();
            if (beds ? isBed(block) : isWorkstation(block)) {
                double distance = Math.sqrt(player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                records.add(new PoiRecord(block.getName().getString(), pos.toImmutable(), List.copyOf(claims.getOrDefault(pos.toImmutable(), List.of())), distance, distance <= 48));
            }
        }
        return records.stream().sorted(Comparator.comparing(PoiRecord::type).thenComparing(record -> squaredDistance(record.pos(), center))).toList();
    }

    private static double squaredDistance(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static boolean isBed(Block block) { return block.getName().getString().toLowerCase(java.util.Locale.ROOT).contains("bed"); }

    private static boolean isWorkstation(Block block) {
        return block == Blocks.BLAST_FURNACE || block == Blocks.SMOKER || block == Blocks.CARTOGRAPHY_TABLE || block == Blocks.BREWING_STAND
                || block == Blocks.COMPOSTER || block == Blocks.BARREL || block == Blocks.FLETCHING_TABLE || block == Blocks.CAULDRON
                || block == Blocks.LECTERN || block == Blocks.STONECUTTER || block == Blocks.LOOM || block == Blocks.SMITHING_TABLE || block == Blocks.GRINDSTONE;
    }

    private static int duplicateClaims(Map<BlockPos, List<UUID>> claims) { return (int)claims.values().stream().filter(owners -> owners.size() > 1).count(); }
}
