package com.villagerscheduleviewer.schedule;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.List;

public final class ScheduleModel {
    private static final int WORK = 0xFFB8872E, GATHER = 0xFF7A5BBF, WANDER = 0xFF5CA65C, SLEEP = 0xFF385B9D, PLAY = 0xFFE8C84F, IDLE = 0xFF777777;
    private ScheduleModel() {}
    public static List<VillagerActivity> forVillager(VillagerEntity villager) {
        boolean baby = villager.isBaby();
        boolean employed = villager.getVillagerData().getProfession() != VillagerProfession.NONE && villager.getVillagerData().getProfession() != VillagerProfession.NITWIT;
        List<VillagerActivity> list = new ArrayList<>();
        list.add(new VillagerActivity("sleep", 12000, 0, SLEEP));
        list.add(new VillagerActivity("wander", 0, 2000, WANDER));
        if (baby) list.add(new VillagerActivity("play", 2000, 9000, PLAY));
        else if (employed) list.add(new VillagerActivity("work", 2000, 9000, WORK));
        else list.add(new VillagerActivity("wander", 2000, 9000, WANDER));
        list.add(new VillagerActivity("gather", 9000, 11000, GATHER));
        list.add(new VillagerActivity("wander", 11000, 12000, WANDER));
        return list;
    }
    public static VillagerActivity current(List<VillagerActivity> list, long time) { return list.stream().filter(a -> a.contains(time)).findFirst().orElse(new VillagerActivity("idle",0,24000,IDLE)); }
    public static VillagerActivity next(List<VillagerActivity> list, long time) {
        int t = (int)Math.floorMod(time, 24000L); VillagerActivity best = null; int bestDelta = 24001;
        for (VillagerActivity a : list) { int delta = Math.floorMod(a.startTick() - t, 24000); if (delta > 0 && delta < bestDelta) { bestDelta = delta; best = a; } }
        return best == null ? list.get(0) : best;
    }
    public static int ticksUntilNext(List<VillagerActivity> list, long time) { return Math.floorMod(next(list, time).startTick() - (int)Math.floorMod(time, 24000L), 24000); }
}
