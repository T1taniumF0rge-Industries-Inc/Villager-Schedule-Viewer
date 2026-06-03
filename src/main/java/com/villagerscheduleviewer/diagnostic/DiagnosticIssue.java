package com.villagerscheduleviewer.diagnostic;

import net.minecraft.util.math.BlockPos;

public record DiagnosticIssue(String problem, String explanation, Severity severity, String suggestedFix, BlockPos focus) {
    public String compact() { return severity().name() + ": " + problem() + " — " + suggestedFix(); }
}
