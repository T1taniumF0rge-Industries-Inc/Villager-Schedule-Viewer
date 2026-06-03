package com.villagerscheduleviewer.render;

import com.villagerscheduleviewer.diagnostic.PoiKind;
import net.minecraft.util.math.BlockPos;

public record ActiveHighlight(PoiKind kind, BlockPos pos, int color, long expiresAt, String label) {}
