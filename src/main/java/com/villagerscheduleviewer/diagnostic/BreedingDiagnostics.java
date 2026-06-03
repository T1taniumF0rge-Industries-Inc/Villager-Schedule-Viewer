package com.villagerscheduleviewer.diagnostic;

import com.villagerscheduleviewer.brain.MemoryEntry;
import net.minecraft.entity.passive.VillagerEntity;

import java.util.List;

public record BreedingDiagnostics(String breedTarget, int nearbyPartners, boolean hasHome, boolean likelyEligible, List<String> notes) {
    public static BreedingDiagnostics inspect(VillagerEntity villager, List<MemoryEntry> memories) {
        String target = memories.stream().filter(m -> m.name().equals("BREED_TARGET")).findFirst().map(MemoryEntry::value).orElse("missing");
        int partners = villager.getWorld().getEntitiesByClass(VillagerEntity.class, villager.getBoundingBox().expand(8.0), other -> other != villager && !other.isBaby()).size();
        boolean home = memories.stream().anyMatch(m -> m.name().equals("HOME") && m.valid());
        java.util.ArrayList<String> notes = new java.util.ArrayList<>();
        if (villager.isBaby()) notes.add("Villager is a baby.");
        if (partners == 0) notes.add("No breeding partner detected nearby.");
        if (!home) notes.add("No assigned bed; nearby free beds may still be required.");
        if (target.equals("missing")) notes.add("No active breed target memory.");
        boolean eligible = !villager.isBaby() && partners > 0 && home;
        return new BreedingDiagnostics(target, partners, home, eligible, notes);
    }
}
