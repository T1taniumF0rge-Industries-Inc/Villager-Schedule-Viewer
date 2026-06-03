package com.villagerscheduleviewer.village;

import com.villagerscheduleviewer.brain.VillagerSnapshot;

import java.util.List;

public record VillageScanResult(List<VillagerSnapshot> villagers, List<PoiRecord> beds, List<PoiRecord> workstations,
                                int claimedBeds, int claimedWorkstations, int duplicateClaims, VillageHealthScores scores, long scannedAt) {
    public static VillageScanResult empty(long time) { return new VillageScanResult(List.of(), List.of(), List.of(), 0, 0, 0, new VillageHealthScores(100,100,100,100,100,100,100), time); }
}
