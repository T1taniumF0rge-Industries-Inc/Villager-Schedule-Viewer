package com.villagerscheduleviewer.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public final class Formatters {
    private Formatters() {}
    public static String ticksToClock(long dayTime) {
        long t = Math.floorMod(dayTime, 24000L);
        long minutes = (t * 60L / 1000L + 360L) % 1440L;
        return "%02d:%02d".formatted(minutes / 60, minutes % 60);
    }
    public static String duration(long ticks) {
        long s = Math.max(0, ticks) / 20;
        return s >= 60 ? "%dm %02ds".formatted(s / 60, s % 60) : "%ds".formatted(s);
    }
    public static String pos(BlockPos pos) { return pos == null ? "—" : pos.getX()+", "+pos.getY()+", "+pos.getZ(); }
    public static String decimal(double value) { return value < 0 ? "—" : String.format(java.util.Locale.ROOT, "%.1f", value); }
    public static String global(GlobalPos pos) { return pos == null ? "—" : pos.dimension().getValue()+" @ "+pos(pos.pos()); }
}
