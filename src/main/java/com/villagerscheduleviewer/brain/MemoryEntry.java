package com.villagerscheduleviewer.brain;

import net.minecraft.util.math.BlockPos;

public record MemoryEntry(String name, String value, BlockPos pos, boolean valid, String warning) {}
