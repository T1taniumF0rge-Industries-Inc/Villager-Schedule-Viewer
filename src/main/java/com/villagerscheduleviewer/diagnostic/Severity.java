package com.villagerscheduleviewer.diagnostic;

public enum Severity {
    INFO(0xFF80B8FF), WARNING(0xFFFFC857), CRITICAL(0xFFFF6B6B);
    private final int color;
    Severity(int color) { this.color = color; }
    public int color() { return color; }
}
