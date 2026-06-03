package com.villagerscheduleviewer.client;

import com.villagerscheduleviewer.config.VsvConfig;
import com.villagerscheduleviewer.gui.VillagerDashboardScreen;
import com.villagerscheduleviewer.gui.VillagerViewerScreen;
import com.villagerscheduleviewer.hud.VillagerHud;
import com.villagerscheduleviewer.render.HighlightManager;
import com.villagerscheduleviewer.render.PoiOverlayRenderer;
import com.villagerscheduleviewer.util.Targeting;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.passive.VillagerEntity;
import org.lwjgl.glfw.GLFW;

public final class VillagerScheduleViewerClient implements ClientModInitializer {
    public static final String MOD_ID = "villagerscheduleviewer";
    public static final VsvConfig CONFIG = VsvConfig.load();
    private static KeyBinding openViewer;
    private static KeyBinding toggleHud;
    private static KeyBinding toggleOverlay;
    private static KeyBinding openDashboard;

    @Override
    public void onInitializeClient() {
        openViewer = register("open_viewer", GLFW.GLFW_KEY_V);
        toggleHud = register("toggle_hud", GLFW.GLFW_KEY_H);
        toggleOverlay = register("toggle_overlay", GLFW.GLFW_KEY_B);
        openDashboard = register("open_dashboard", GLFW.GLFW_KEY_N);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HighlightManager.tick();
            while (openViewer.wasPressed()) openViewer(client);
            while (toggleHud.wasPressed()) { CONFIG.hudEnabled = !CONFIG.hudEnabled; CONFIG.save(); }
            while (toggleOverlay.wasPressed()) { CONFIG.overlayEnabled = !CONFIG.overlayEnabled; CONFIG.save(); }
            while (openDashboard.wasPressed()) client.setScreen(new VillagerDashboardScreen(client.player));
        });
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> VillagerHud.render(drawContext));
        WorldRenderEvents.AFTER_TRANSLUCENT.register(PoiOverlayRenderer::render);
    }

    private static KeyBinding register(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding("key." + MOD_ID + "." + id,
                InputUtil.Type.KEYSYM, key, "key.categories." + MOD_ID));
    }

    private static void openViewer(MinecraftClient client) {
        VillagerEntity villager = Targeting.getTargetedVillager(client, CONFIG.targetRange);
        if (villager == null) {
            if (client.player != null) client.player.sendMessage(net.minecraft.text.Text.translatable("text.villagerscheduleviewer.no_villager"), true);
            return;
        }
        client.setScreen(new VillagerViewerScreen(villager));
    }
}
