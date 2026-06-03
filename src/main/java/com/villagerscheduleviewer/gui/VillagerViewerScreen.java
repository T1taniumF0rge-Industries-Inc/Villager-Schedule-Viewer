package com.villagerscheduleviewer.gui;

import com.villagerscheduleviewer.brain.VillagerSnapshot;
import com.villagerscheduleviewer.diagnostic.DiagnosticIssue;
import com.villagerscheduleviewer.render.HighlightManager;
import com.villagerscheduleviewer.schedule.ScheduleModel;
import com.villagerscheduleviewer.util.Formatters;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.Text;

public final class VillagerViewerScreen extends Screen {
    private final int entityId;
    private VillagerSnapshot snapshot;

    public VillagerViewerScreen(VillagerEntity villager) {
        super(Text.translatable("text.villagerscheduleviewer.viewer.title"));
        this.entityId = villager.getId();
        this.snapshot = VillagerSnapshot.capture(villager);
    }

    @Override protected void init() {
        int bx = width - 176;
        addDrawableChild(ButtonWidget.builder(Text.literal("Highlight Workstation"), b -> HighlightManager.highlight(snapshot.jobSite())).dimensions(bx, 42, 152, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Highlight Bed"), b -> HighlightManager.highlight(snapshot.home())).dimensions(bx, 66, 152, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Copy Workstation"), b -> copy(snapshot.jobSite().assigned() ? Formatters.pos(snapshot.jobSite().pos()) : "No workstation assigned")).dimensions(bx, 90, 152, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Copy Bed"), b -> copy(snapshot.home().assigned() ? Formatters.pos(snapshot.home().pos()) : "No bed assigned")).dimensions(bx, 114, 152, 20).build());
    }

    @Override public boolean shouldPause() { return false; }

    @Override public void tick() {
        if (client != null && client.world != null && client.world.getTime() % 20 == 0) {
            Entity e = client.world.getEntityById(entityId);
            if (e instanceof VillagerEntity v) snapshot = VillagerSnapshot.capture(v);
        }
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int x = 24, y = 20, w = width - 220;
        context.fill(x-8, y-8, x+w+8, height-24, 0xC0101010);
        context.drawTextWithShadow(textRenderer, title, x, y, 0xFFFFFF); y += 18;
        line(context, x, y, "UUID: " + snapshot.uuid()); y += 10;
        line(context, x, y, "Profession: " + snapshot.profession() + "  Level: " + snapshot.level() + "  Type: " + snapshot.type() + "  Age: " + (snapshot.baby()?"Baby":"Adult")); y += 10;
        long time = client == null || client.world == null ? 0 : client.world.getTimeOfDay();
        line(context, x, y, "Current: " + snapshot.activity() + "  Next: " + ScheduleModel.next(snapshot.schedule(), time).id() + " in " + Formatters.duration(ScheduleModel.ticksUntilNext(snapshot.schedule(), time))); y += 16;
        drawTimeline(context, x, y, w, 18, time); y += 30;

        header(context, x, y, "Assigned POIs"); y += 12;
        line(context, x, y, snapshot.jobSite().assigned() ? snapshot.jobSite().summary() : "No workstation assigned."); y += 10;
        line(context, x, y, snapshot.home().assigned() ? snapshot.home().summary() : "No bed assigned."); y += 14;

        header(context, x, y, "Pathfinding diagnostics"); y += 12;
        line(context, x, y, "Destination: " + Formatters.pos(snapshot.path().destination()) + "  Nodes: " + snapshot.path().pathLength() + "  Current node: " + snapshot.path().currentNodeIndex()); y += 10;
        line(context, x, y, "Attempting for: " + snapshot.path().attemptTicks() + " ticks  Last progress: " + snapshot.path().lastSuccessfulUpdate() + "  Status: " + snapshot.path().status()); y += 14;

        header(context, x, y, "Breeding analytics"); y += 12;
        line(context, x, y, "Breed target: " + snapshot.breeding().breedTarget() + "  Nearby partners: " + snapshot.breeding().nearbyPartners() + "  Eligible: " + yesNo(snapshot.breeding().likelyEligible())); y += 10;
        if (!snapshot.breeding().notes().isEmpty()) { line(context, x, y, String.join(" ", snapshot.breeding().notes())); y += 10; }
        y += 4;

        header(context, x, y, "Village Doctor"); y += 12;
        if (snapshot.issues().isEmpty()) { line(context, x, y, "No actionable problems detected."); y += 10; }
        for (DiagnosticIssue issue : snapshot.issues()) {
            if (y > height - 42) break;
            context.drawTextWithShadow(textRenderer, issue.severity().name() + ": " + issue.problem(), x, y, issue.severity().color()); y += 10;
            line(context, x + 8, y, "Explanation: " + issue.explanation()); y += 10;
            line(context, x + 8, y, "Suggested Fix: " + issue.suggestedFix()); y += 12;
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void copy(String value) { if (client != null) client.keyboard.setClipboard(value); }
    private String yesNo(boolean value) { return value ? "yes" : "no"; }
    private void header(DrawContext c, int x, int y, String s) { c.drawTextWithShadow(textRenderer, s, x, y, 0xFFD966); }
    private void line(DrawContext c, int x, int y, String s) { c.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(s, width - x - 28), x, y, 0xE0E0E0); }

    private void drawTimeline(DrawContext c, int x, int y, int w, int h, long time) {
        c.fill(x, y, x+w, y+h, 0xFF202020);
        for (var a : snapshot.schedule()) {
            int sx = x + (int)(w * (a.startTick() / 24000.0)); int ex = x + (int)(w * (a.endTick() / 24000.0));
            if (a.startTick() > a.endTick()) { c.fill(sx, y, x+w, y+h, a.color()); c.fill(x, y, ex, y+h, a.color()); }
            else c.fill(sx, y, Math.max(sx+1, ex), y+h, a.color());
            c.drawText(textRenderer, a.id(), sx + 2, y + 5, 0xFF000000, false);
        }
        int tx = x + (int)(w * (Math.floorMod(time, 24000L) / 24000.0)); c.fill(tx-1, y-3, tx+2, y+h+3, 0xFFFFFFFF);
        c.drawTextWithShadow(textRenderer, Formatters.ticksToClock(time), Math.min(x+w-40, tx+3), y+h+4, 0xFFFFFF);
    }
}
