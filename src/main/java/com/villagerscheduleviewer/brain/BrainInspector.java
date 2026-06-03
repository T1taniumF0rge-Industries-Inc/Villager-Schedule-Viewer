package com.villagerscheduleviewer.brain;

import com.villagerscheduleviewer.diagnostic.BreedingDiagnostics;
import com.villagerscheduleviewer.diagnostic.PathDiagnostics;
import com.villagerscheduleviewer.diagnostic.PoiDetails;
import com.villagerscheduleviewer.diagnostic.PoiKind;
import com.villagerscheduleviewer.diagnostic.VillageDoctor;
import com.villagerscheduleviewer.schedule.ScheduleModel;
import com.villagerscheduleviewer.util.Formatters;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BrainInspector {
    private BrainInspector() {}
    public static VillagerSnapshot inspect(VillagerEntity villager) {
        long time = villager.getWorld().getTimeOfDay();
        var schedule = ScheduleModel.forVillager(villager);
        List<MemoryEntry> memories = new ArrayList<>();
        Brain<?> brain = villager.getBrain();
        addGlobal(memories, brain, "HOME", MemoryModuleType.HOME);
        addGlobal(memories, brain, "JOB_SITE", MemoryModuleType.JOB_SITE);
        addGlobal(memories, brain, "MEETING_POINT", MemoryModuleType.MEETING_POINT);
        addWalk(memories, brain);
        addEntity(memories, brain, "BREED_TARGET", MemoryModuleType.BREED_TARGET);
        addEntity(memories, brain, "INTERACTION_TARGET", MemoryModuleType.INTERACTION_TARGET);
        addLook(memories, brain);
        PoiDetails home = PoiDetails.fromMemory(villager, memories, PoiKind.HOME);
        PoiDetails jobSite = PoiDetails.fromMemory(villager, memories, PoiKind.JOB_SITE);
        PathDiagnostics path = PathDiagnostics.inspect(villager);
        BreedingDiagnostics breeding = BreedingDiagnostics.inspect(villager, memories);
        var issues = VillageDoctor.diagnose(villager, memories, home, jobSite, path, breeding);
        List<String> warnings = warnings(villager, memories);
        issues.stream().map(issue -> issue.severity().name() + ": " + issue.problem()).forEach(warnings::add);
        String prof = String.valueOf(Registries.VILLAGER_PROFESSION.getId(villager.getVillagerData().getProfession()));
        String type = String.valueOf(Registries.VILLAGER_TYPE.getId(villager.getVillagerData().getType()));
        return new VillagerSnapshot(villager.getUuid(), villager.getId(), prof, villager.getVillagerData().getLevel(), type, villager.isBaby(), ScheduleModel.current(schedule, time).id(), schedule, memories, home, jobSite, path, breeding, issues, warnings, villager.getWorld().getTime());
    }
    private static <T> Optional<T> mem(Brain<?> brain, MemoryModuleType<T> type) { return brain.getOptionalRegisteredMemory(type); }
    private static void addGlobal(List<MemoryEntry> out, Brain<?> brain, String name, MemoryModuleType<GlobalPos> type) {
        Optional<GlobalPos> p = mem(brain, type); out.add(new MemoryEntry(name, p.map(Formatters::global).orElse("missing"), p.map(GlobalPos::pos).orElse(null), p.isPresent(), p.isPresent()?"":"not present"));
    }
    private static void addWalk(List<MemoryEntry> out, Brain<?> brain) {
        Optional<WalkTarget> target = mem(brain, MemoryModuleType.WALK_TARGET); out.add(new MemoryEntry("WALK_TARGET", target.map(Object::toString).orElse("missing"), null, target.isPresent(), target.isPresent()?"":"not present"));
    }
    private static void addLook(List<MemoryEntry> out, Brain<?> brain) {
        Optional<?> target = mem(brain, MemoryModuleType.LOOK_TARGET); out.add(new MemoryEntry("LOOK_TARGET", target.map(Object::toString).orElse("missing"), null, target.isPresent(), target.isPresent()?"":"not present"));
    }
    private static <T extends LivingEntity> void addEntity(List<MemoryEntry> out, Brain<?> brain, String name, MemoryModuleType<T> type) {
        Optional<T> e = mem(brain, type); out.add(new MemoryEntry(name, e.map(x -> x.getType().getName().getString()+" #"+x.getId()).orElse("missing"), e.map(x -> x.getBlockPos()).orElse(null), e.isPresent(), e.isPresent()?"":"not present"));
    }
    private static List<String> warnings(VillagerEntity villager, List<MemoryEntry> memories) {
        List<String> warnings = new ArrayList<>();
        boolean hasHome = memories.stream().anyMatch(m -> m.name().equals("HOME") && m.valid());
        boolean hasJob = memories.stream().anyMatch(m -> m.name().equals("JOB_SITE") && m.valid());
        if (!hasHome) warnings.add("Missing bed / HOME memory");
        if (!villager.isBaby() && Registries.VILLAGER_PROFESSION.getId(villager.getVillagerData().getProfession()).getPath().matches("(?!none|nitwit).+") && !hasJob) warnings.add("Missing workstation / JOB_SITE memory");
        if (villager.getNavigation().isIdle() && villager.getVelocity().horizontalLengthSquared() < 0.0001 && !villager.isSleeping()) warnings.add("Potentially idle or stuck; verify path reachability");
        return warnings;
    }
}
