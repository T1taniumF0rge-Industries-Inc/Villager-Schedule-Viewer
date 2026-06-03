package com.villagerscheduleviewer.village;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public record PoiRecord(String type, BlockPos pos, List<UUID> owners, double nearestDistance, boolean reachable) {}
