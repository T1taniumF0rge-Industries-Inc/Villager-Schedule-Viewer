package com.villagerscheduleviewer.render;

import com.villagerscheduleviewer.brain.MemoryEntry;
import com.villagerscheduleviewer.client.VillagerScheduleViewerClient;
import com.villagerscheduleviewer.util.SnapshotCache;
import com.villagerscheduleviewer.util.Targeting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class PoiOverlayRenderer {
    private PoiOverlayRenderer() {}

    public static void render(WorldRenderContext context) {
        if (!VillagerScheduleViewerClient.CONFIG.overlayEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        MatrixStack matrices = context.matrixStack();
        if (matrices == null || context.consumers() == null || client.player == null) return;
        Vec3d cam = context.camera().getPos();
        VillagerEntity villager = Targeting.getTargetedVillager(client, VillagerScheduleViewerClient.CONFIG.targetRange);
        if (villager != null) renderTargetVillagerData(context, matrices, cam, villager);
        renderActiveHighlights(context, matrices, cam, client);
    }

    private static void renderTargetVillagerData(WorldRenderContext context, MatrixStack matrices, Vec3d cam, VillagerEntity villager) {
        var snapshot = SnapshotCache.get(villager);
        for (MemoryEntry memory : snapshot.memories()) {
            BlockPos pos = memory.pos();
            if (pos == null || !shouldRenderMemory(memory.name())) continue;
            int color = color(memory.name());
            drawBlockOutline(context, matrices, cam, pos, color);
        }
        if (VillagerScheduleViewerClient.CONFIG.overlayPaths) {
            for (BlockPos node : snapshot.path().nodes()) drawBlockOutline(context, matrices, cam, node, 0xFF55FF55);
            if (snapshot.path().destination() != null) drawBeam(context, matrices, cam, snapshot.path().destination(), 0xFF55FF55, 8);
        }
    }

    private static boolean shouldRenderMemory(String name) {
        return (name.equals("HOME") && VillagerScheduleViewerClient.CONFIG.overlayBeds)
                || (name.equals("JOB_SITE") && VillagerScheduleViewerClient.CONFIG.overlayWorkstations)
                || (name.equals("MEETING_POINT") && VillagerScheduleViewerClient.CONFIG.overlayMeetingPoints);
    }

    private static int color(String name) {
        return switch (name) {
            case "HOME" -> VillagerScheduleViewerClient.CONFIG.bedHighlightColor;
            case "JOB_SITE" -> VillagerScheduleViewerClient.CONFIG.workstationHighlightColor;
            case "MEETING_POINT" -> VillagerScheduleViewerClient.CONFIG.meetingHighlightColor;
            default -> VillagerScheduleViewerClient.CONFIG.warningHighlightColor;
        };
    }

    private static void renderActiveHighlights(WorldRenderContext context, MatrixStack matrices, Vec3d cam, MinecraftClient client) {
        for (ActiveHighlight highlight : HighlightManager.active()) {
            if (client.player.squaredDistanceTo(highlight.pos().getX(), highlight.pos().getY(), highlight.pos().getZ()) > VillagerScheduleViewerClient.CONFIG.renderDistance * VillagerScheduleViewerClient.CONFIG.renderDistance) continue;
            drawBlockOutline(context, matrices, cam, highlight.pos(), highlight.color());
            drawBeam(context, matrices, cam, highlight.pos(), highlight.color(), 32);
            drawTracer(context, matrices, cam, client.player.getEyePos(), Vec3d.ofCenter(highlight.pos()), highlight.color());
            drawLabel(context, matrices, cam, Vec3d.ofCenter(highlight.pos()).add(0, 1.4, 0), highlight.label(), highlight.color());
        }
    }

    private static void drawBlockOutline(WorldRenderContext context, MatrixStack matrices, Vec3d cam, BlockPos pos, int color) {
        Box box = new Box(pos).offset(-cam.x, -cam.y, -cam.z).expand(0.025);
        net.minecraft.client.render.WorldRenderer.drawBox(matrices, context.consumers().getBuffer(RenderLayer.getLines()), box, red(color), green(color), blue(color), 1.0f);
    }

    private static void drawBeam(WorldRenderContext context, MatrixStack matrices, Vec3d cam, BlockPos pos, int color, int height) {
        for (int y = 0; y < height; y += 2) {
            Box beam = new Box(pos.getX() + 0.45, pos.getY() + y, pos.getZ() + 0.45, pos.getX() + 0.55, pos.getY() + y + 1.7, pos.getZ() + 0.55).offset(-cam.x, -cam.y, -cam.z);
            net.minecraft.client.render.WorldRenderer.drawBox(matrices, context.consumers().getBuffer(RenderLayer.getLines()), beam, red(color), green(color), blue(color), 0.85f);
        }
    }

    private static void drawTracer(WorldRenderContext context, MatrixStack matrices, Vec3d cam, Vec3d start, Vec3d end, int color) {
        Vec3d delta = end.subtract(start);
        int segments = Math.max(1, Math.min(48, (int)(delta.length() * 1.5)));
        for (int i = 0; i < segments; i++) {
            Vec3d a = start.add(delta.multiply(i / (double)segments));
            Vec3d b = start.add(delta.multiply((i + 1) / (double)segments));
            Box box = new Box(a, b).expand(0.015).offset(-cam.x, -cam.y, -cam.z);
            net.minecraft.client.render.WorldRenderer.drawBox(matrices, context.consumers().getBuffer(RenderLayer.getLines()), box, red(color), green(color), blue(color), 0.75f);
        }
    }

    private static void drawLabel(WorldRenderContext context, MatrixStack matrices, Vec3d cam, Vec3d pos, String label, int color) {
        MinecraftClient client = MinecraftClient.getInstance();
        matrices.push();
        matrices.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        matrices.multiply(context.camera().getRotation());
        matrices.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float x = -client.textRenderer.getWidth(label) / 2.0f;
        client.textRenderer.draw(label, x, 0, color, false, matrix, context.consumers(), TextRenderer.TextLayerType.SEE_THROUGH, 0x70000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        matrices.pop();
    }

    private static float red(int color) { return ((color >> 16) & 255) / 255.0f; }
    private static float green(int color) { return ((color >> 8) & 255) / 255.0f; }
    private static float blue(int color) { return (color & 255) / 255.0f; }
}
