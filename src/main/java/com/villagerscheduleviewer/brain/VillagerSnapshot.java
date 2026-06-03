package com.villagerscheduleviewer.brain;

import com.villagerscheduleviewer.diagnostic.BreedingDiagnostics;
import com.villagerscheduleviewer.diagnostic.DiagnosticIssue;
import com.villagerscheduleviewer.diagnostic.PathDiagnostics;
import com.villagerscheduleviewer.diagnostic.PoiDetails;
import com.villagerscheduleviewer.schedule.VillagerActivity;
import net.minecraft.entity.passive.VillagerEntity;

import java.util.List;
import java.util.UUID;

public record VillagerSnapshot(UUID uuid, int entityId, String profession, int level, String type, boolean baby, String activity,
                               List<VillagerActivity> schedule, List<MemoryEntry> memories, PoiDetails home, PoiDetails jobSite,
                               PathDiagnostics path, BreedingDiagnostics breeding, List<DiagnosticIssue> issues,
                               List<String> warnings, long capturedAt) {
    public static VillagerSnapshot capture(VillagerEntity villager) { return BrainInspector.inspect(villager); }
}
