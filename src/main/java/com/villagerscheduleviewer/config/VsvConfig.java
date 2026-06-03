package com.villagerscheduleviewer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.villagerscheduleviewer.client.VillagerScheduleViewerClient;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VsvConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean hudEnabled = true;
    public boolean overlayEnabled = true;
    public boolean tracersThroughWalls = true;
    public boolean showDashboardWarnings = true;
    public double targetRange = 24.0;
    public int cacheRefreshTicks = 20;
    public int dashboardRadius = 96;
    public int villageScanIntervalTicks = 100;
    public int highlightTimeoutTicks = 20 * 30;
    public int renderDistance = 96;
    public boolean overlayBeds = true;
    public boolean overlayWorkstations = true;
    public boolean overlayMeetingPoints = true;
    public boolean overlayDestinations = true;
    public boolean overlayPaths = true;
    public boolean overlayWarnings = true;
    public boolean compactMode = false;
    public int bedHighlightColor = 0xFF60A0FF;
    public int workstationHighlightColor = 0xFFFFB000;
    public int meetingHighlightColor = 0xFFB060FF;
    public int warningHighlightColor = 0xFFFF5555;

    public static VsvConfig load() {
        Path path = path();
        if (Files.exists(path)) {
            try { return GSON.fromJson(Files.readString(path), VsvConfig.class); }
            catch (Exception ignored) { }
        }
        VsvConfig config = new VsvConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(this));
        } catch (IOException ignored) { }
    }

    private static Path path() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve(VillagerScheduleViewerClient.MOD_ID + ".json");
    }
}
