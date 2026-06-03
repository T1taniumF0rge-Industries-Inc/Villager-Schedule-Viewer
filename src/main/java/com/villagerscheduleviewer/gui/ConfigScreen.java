package com.villagerscheduleviewer.gui;

import com.villagerscheduleviewer.config.VsvConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ConfigScreen extends Screen {
    private final Screen parent; private final VsvConfig config;
    public ConfigScreen(Screen parent, VsvConfig config) { super(Text.translatable("text.villagerscheduleviewer.config.title")); this.parent = parent; this.config = config; }
    @Override protected void init() {
        int y = height / 2 - 78;
        addToggle("HUD", y, () -> config.hudEnabled, value -> config.hudEnabled = value); y += 24;
        addToggle("POI overlay", y, () -> config.overlayEnabled, value -> config.overlayEnabled = value); y += 24;
        addToggle("Bed overlays", y, () -> config.overlayBeds, value -> config.overlayBeds = value); y += 24;
        addToggle("Workstation overlays", y, () -> config.overlayWorkstations, value -> config.overlayWorkstations = value); y += 24;
        addToggle("Path overlays", y, () -> config.overlayPaths, value -> config.overlayPaths = value); y += 30;
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(width/2-100, y, 200, 20).build());
    }
    private void addToggle(String label, int y, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + (getter.getAsBoolean()?"on":"off")), b -> {
            setter.accept(!getter.getAsBoolean()); config.save(); b.setMessage(Text.literal(label + ": " + (getter.getAsBoolean()?"on":"off")));
        }).dimensions(width/2-100, y, 200, 20).build());
    }
    @Override public void close() { if (client != null) client.setScreen(parent); }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) { renderBackground(context, mouseX, mouseY, delta); context.drawCenteredTextWithShadow(textRenderer, title, width/2, 30, 0xFFFFFF); super.render(context, mouseX, mouseY, delta); }
}
