package com.villagerscheduleviewer.compat;

import com.villagerscheduleviewer.client.VillagerScheduleViewerClient;
import com.villagerscheduleviewer.config.VsvConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

final class ClothConfigIntegration {
    private ClothConfigIntegration() {}

    static Screen create(Screen parent) {
        VsvConfig config = VillagerScheduleViewerClient.CONFIG;
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.translatable("text.villagerscheduleviewer.config.title"));
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        ConfigEntryBuilder entries = builder.entryBuilder();
        general.addEntry(entries.startBooleanToggle(Text.literal("HUD enabled"), config.hudEnabled).setSaveConsumer(value -> config.hudEnabled = value).build());
        general.addEntry(entries.startBooleanToggle(Text.literal("POI overlay enabled"), config.overlayEnabled).setSaveConsumer(value -> config.overlayEnabled = value).build());
        general.addEntry(entries.startBooleanToggle(Text.literal("Tracer lines through walls"), config.tracersThroughWalls).setSaveConsumer(value -> config.tracersThroughWalls = value).build());
        general.addEntry(entries.startDoubleField(Text.literal("Target range"), config.targetRange).setMin(4.0).setMax(64.0).setSaveConsumer(value -> config.targetRange = value).build());
        general.addEntry(entries.startIntField(Text.literal("Cache refresh ticks"), config.cacheRefreshTicks).setMin(1).setMax(200).setSaveConsumer(value -> config.cacheRefreshTicks = value).build());
        general.addEntry(entries.startIntField(Text.literal("Dashboard radius"), config.dashboardRadius).setMin(16).setMax(256).setSaveConsumer(value -> config.dashboardRadius = value).build());
        general.addEntry(entries.startIntField(Text.literal("Highlight timeout ticks"), config.highlightTimeoutTicks).setMin(20).setMax(20 * 60 * 10).setSaveConsumer(value -> config.highlightTimeoutTicks = value).build());
        general.addEntry(entries.startIntField(Text.literal("Village scan interval ticks"), config.villageScanIntervalTicks).setMin(20).setMax(20 * 60).setSaveConsumer(value -> config.villageScanIntervalTicks = value).build());
        general.addEntry(entries.startBooleanToggle(Text.literal("Show bed overlays"), config.overlayBeds).setSaveConsumer(value -> config.overlayBeds = value).build());
        general.addEntry(entries.startBooleanToggle(Text.literal("Show workstation overlays"), config.overlayWorkstations).setSaveConsumer(value -> config.overlayWorkstations = value).build());
        general.addEntry(entries.startBooleanToggle(Text.literal("Show path overlays"), config.overlayPaths).setSaveConsumer(value -> config.overlayPaths = value).build());
        general.addEntry(entries.startBooleanToggle(Text.literal("Compact mode"), config.compactMode).setSaveConsumer(value -> config.compactMode = value).build());
        builder.setSavingRunnable(config::save);
        return builder.build();
    }
}
