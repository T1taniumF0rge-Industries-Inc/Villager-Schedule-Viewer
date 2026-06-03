package com.villagerscheduleviewer.diagnostic;

public enum PoiKind {
    HOME("Bed"), JOB_SITE("Workstation"), MEETING_POINT("Meeting Point");
    private final String displayName;
    PoiKind(String displayName) { this.displayName = displayName; }
    public String displayName() { return displayName; }
}
