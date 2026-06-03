package com.villagerscheduleviewer.diagnostic;

import com.villagerscheduleviewer.brain.MemoryEntry;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.List;

public final class VillageDoctor {
    private VillageDoctor() {}

    public static List<DiagnosticIssue> diagnose(VillagerEntity villager, List<MemoryEntry> memories, PoiDetails home, PoiDetails job, PathDiagnostics path, BreedingDiagnostics breeding) {
        List<DiagnosticIssue> issues = new ArrayList<>();
        if (!home.assigned()) issues.add(new DiagnosticIssue("Missing bed", "The villager has no HOME memory, so sleep and breeding reliability are reduced.", Severity.CRITICAL, "Place enough valid beds and let the villager claim one during daytime.", null));
        else if (!home.reachable()) issues.add(new DiagnosticIssue("Unreachable bed", home.summary(), Severity.CRITICAL, "Move the bed closer or remove blocks between the villager and bed.", home.pos()));
        else if (home.distance() > 32) issues.add(new DiagnosticIssue("Bed far away", home.summary(), Severity.WARNING, "Move the bed closer to reduce nighttime wandering.", home.pos()));

        boolean employed = villager.getVillagerData().getProfession() != VillagerProfession.NONE && villager.getVillagerData().getProfession() != VillagerProfession.NITWIT;
        if (employed && !job.assigned()) issues.add(new DiagnosticIssue("Missing workstation", "The villager has a profession but no JOB_SITE memory.", Severity.CRITICAL, "Place or replace the correct workstation near the villager.", null));
        else if (employed && !job.reachable()) issues.add(new DiagnosticIssue("Unable to work", job.summary(), Severity.CRITICAL, "Move the workstation closer or remove obstacles.", job.pos()));
        else if (employed && job.distance() > 32) issues.add(new DiagnosticIssue("Workstation far away", job.summary(), Severity.WARNING, "Move the workstation closer to improve restocks and work behavior.", job.pos()));

        boolean meeting = memories.stream().anyMatch(memory -> memory.name().equals("MEETING_POINT") && memory.valid());
        if (!meeting) issues.add(new DiagnosticIssue("No meeting point", "The villager has no MEETING_POINT memory.", Severity.INFO, "Place a bell if you want village gathering behavior.", null));
        if (path.stuck()) issues.add(new DiagnosticIssue("Stuck pathfinding", path.status(), Severity.CRITICAL, "Clear nearby blocks, water, trapdoors, or fences that are interrupting navigation.", path.destination()));
        if (!breeding.likelyEligible()) issues.add(new DiagnosticIssue("Breeding not ready", String.join(" ", breeding.notes()), Severity.INFO, "Provide food, reachable beds, and another adult villager nearby.", null));
        return issues;
    }
}
