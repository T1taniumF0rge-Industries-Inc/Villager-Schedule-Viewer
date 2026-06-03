package com.villagerscheduleviewer.schedule;

public record VillagerActivity(String id, int startTick, int endTick, int color) {
    public boolean contains(long dayTime) {
        int t = (int)Math.floorMod(dayTime, 24000L);
        return startTick <= endTick ? t >= startTick && t < endTick : t >= startTick || t < endTick;
    }
    public int duration() { return endTick >= startTick ? endTick - startTick : 24000 - startTick + endTick; }
}
