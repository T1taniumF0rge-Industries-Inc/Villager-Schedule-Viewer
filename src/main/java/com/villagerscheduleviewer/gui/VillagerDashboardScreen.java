package com.villagerscheduleviewer.gui;

import com.villagerscheduleviewer.brain.VillagerSnapshot;
import com.villagerscheduleviewer.diagnostic.PoiKind;
import com.villagerscheduleviewer.render.HighlightManager;
import com.villagerscheduleviewer.util.Formatters;
import com.villagerscheduleviewer.village.PoiRecord;
import com.villagerscheduleviewer.village.VillageScanResult;
import com.villagerscheduleviewer.village.VillageScanner;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class VillagerDashboardScreen extends Screen {
    private final PlayerEntity anchor;
    private TextFieldWidget search;
    private Tab tab = Tab.OVERVIEW;
    private VillageScanResult scan = VillageScanResult.empty(0);

    public VillagerDashboardScreen(PlayerEntity anchor) { super(Text.translatable("text.villagerscheduleviewer.dashboard.title")); this.anchor = anchor; }

    @Override protected void init() {
        int x = 24;
        for (Tab value : Tab.values()) {
            addDrawableChild(ButtonWidget.builder(Text.literal(value.label), b -> tab = value).dimensions(x, 22, 92, 20).build());
            x += 96;
        }
        search = new TextFieldWidget(textRenderer, 24, 48, 220, 18, Text.literal("Search"));
        search.setPlaceholder(Text.literal("Search villagers, POIs, warnings"));
        addDrawableChild(search);
        refresh();
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void tick() { if (client != null && client.world != null && client.world.getTime() % 20 == 0) refresh(); }
    private void refresh() { scan = VillageScanner.get(anchor); }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawTextWithShadow(textRenderer, title.copy().append(" - " + tab.label), 24, 10, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
        int y = 74;
        context.fill(16, y - 8, width - 16, height - 18, 0xC0101010);
        switch (tab) {
            case OVERVIEW -> renderOverview(context, y);
            case VILLAGERS -> renderVillagers(context, y);
            case BEDS -> renderPoiTable(context, y, scan.beds(), PoiKind.HOME);
            case WORKSTATIONS -> renderPoiTable(context, y, scan.workstations(), PoiKind.JOB_SITE);
            case WARNINGS -> renderWarnings(context, y);
            case DIAGNOSTICS -> renderDiagnostics(context, y);
        }
    }

    private void renderOverview(DrawContext context, int y) {
        line(context, 24, y, "Villagers: " + scan.villagers().size() + "  Beds: " + scan.beds().size() + " (claimed " + scan.claimedBeds() + ")  Workstations: " + scan.workstations().size() + " (claimed " + scan.claimedWorkstations() + ")  Duplicate claims: " + scan.duplicateClaims()); y += 18;
        score(context, 24, y, "Overall Village Score", scan.scores().overall()); y += 16;
        score(context, 24, y, "Population Health", scan.scores().populationHealth()); y += 12;
        score(context, 24, y, "Employment", scan.scores().employment()); y += 12;
        score(context, 24, y, "Housing", scan.scores().housing()); y += 12;
        score(context, 24, y, "Pathfinding", scan.scores().pathfinding()); y += 12;
        score(context, 24, y, "Breeding Readiness", scan.scores().breedingReadiness()); y += 12;
        score(context, 24, y, "POI Coverage", scan.scores().poiCoverage());
    }

    private void renderVillagers(DrawContext context, int y) {
        header(context, y, "Profession", "Activity", "Bed", "Workstation", "Top warning"); y += 12;
        for (VillagerSnapshot s : filteredVillagers()) {
            if (y > height - 30) break;
            int color = s.issues().isEmpty() ? 0xE0E0E0 : s.issues().get(0).severity().color();
            context.drawTextWithShadow(textRenderer, s.profession()+" L"+s.level(), 24, y, color);
            context.drawTextWithShadow(textRenderer, s.activity(), 170, y, color);
            context.drawTextWithShadow(textRenderer, s.home().assigned() ? Formatters.pos(s.home().pos()) : "missing", 270, y, s.home().assigned() ? 0x80FF80 : 0xFFAA66);
            context.drawTextWithShadow(textRenderer, s.jobSite().assigned() ? Formatters.pos(s.jobSite().pos()) : "missing", 390, y, s.jobSite().assigned() ? 0x80FF80 : 0xFFAA66);
            context.drawTextWithShadow(textRenderer, s.issues().isEmpty() ? "—" : s.issues().get(0).problem(), 520, y, color);
            y += 11;
        }
    }

    private void renderPoiTable(DrawContext context, int y, List<PoiRecord> records, PoiKind kind) {
        int claimed = (int)records.stream().filter(record -> !record.owners().isEmpty()).count();
        int duplicate = (int)records.stream().filter(record -> record.owners().size() > 1).count();
        line(context, 24, y, "Total: " + records.size() + "  Claimed: " + claimed + "  Unclaimed: " + (records.size() - claimed) + "  Duplicate claims: " + duplicate); y += 16;
        header(context, y, kind.displayName() + " Type", "Coordinates", "Owner(s)", "Distance", "Reachable"); y += 12;
        for (PoiRecord record : filterPois(records)) {
            if (y > height - 30) break;
            context.drawTextWithShadow(textRenderer, record.type(), 24, y, 0xE0E0E0);
            context.drawTextWithShadow(textRenderer, Formatters.pos(record.pos()), 190, y, 0xE0E0E0);
            context.drawTextWithShadow(textRenderer, record.owners().isEmpty() ? "unclaimed" : String.valueOf(record.owners().size()), 320, y, record.owners().isEmpty() ? 0xFFAA66 : 0x80FF80);
            context.drawTextWithShadow(textRenderer, Formatters.decimal(record.nearestDistance()), 430, y, 0xE0E0E0);
            context.drawTextWithShadow(textRenderer, record.reachable() ? "yes" : "far", 500, y, record.reachable() ? 0x80FF80 : 0xFFAA66);
            y += 11;
        }
    }

    private void renderWarnings(DrawContext context, int y) {
        for (VillagerSnapshot villager : filteredVillagers()) {
            for (var issue : villager.issues()) {
                if (y > height - 34) return;
                context.drawTextWithShadow(textRenderer, issue.severity().name() + " - " + issue.problem(), 24, y, issue.severity().color()); y += 10;
                line(context, 40, y, issue.explanation() + " Fix: " + issue.suggestedFix()); y += 12;
            }
        }
    }

    private void renderDiagnostics(DrawContext context, int y) {
        line(context, 24, y, "Diagnostics are cached and refreshed incrementally every " + com.villagerscheduleviewer.client.VillagerScheduleViewerClient.CONFIG.villageScanIntervalTicks + " ticks."); y += 14;
        line(context, 24, y, "Path warnings: " + scan.villagers().stream().filter(v -> v.path().stuck()).count()); y += 12;
        line(context, 24, y, "Breeding-ready villagers: " + scan.villagers().stream().filter(v -> v.breeding().likelyEligible()).count()); y += 12;
        line(context, 24, y, "Unreachable claimed workstations: " + scan.villagers().stream().filter(v -> v.jobSite().assigned() && !v.jobSite().reachable()).count()); y += 12;
        line(context, 24, y, "Unreachable claimed beds: " + scan.villagers().stream().filter(v -> v.home().assigned() && !v.home().reachable()).count());
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY >= 102 && (tab == Tab.BEDS || tab == Tab.WORKSTATIONS)) {
            int row = ((int)mouseY - 102) / 11;
            List<PoiRecord> records = filterPois(tab == Tab.BEDS ? scan.beds() : scan.workstations());
            if (row >= 0 && row < records.size()) {
                PoiRecord record = records.get(row);
                HighlightManager.highlight(tab == Tab.BEDS ? PoiKind.HOME : PoiKind.JOB_SITE, record.pos(), record.type() + " @ " + Formatters.pos(record.pos()));
                if (!record.owners().isEmpty() && client != null && client.world != null) {
                    client.world.getEntitiesByClass(VillagerEntity.class, anchor.getBoundingBox().expand(com.villagerscheduleviewer.client.VillagerScheduleViewerClient.CONFIG.dashboardRadius), villager -> villager.getUuid().equals(record.owners().get(0)))
                            .stream().findFirst().ifPresent(villager -> client.setScreen(new VillagerViewerScreen(villager)));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private List<VillagerSnapshot> filteredVillagers() {
        String q = query();
        return scan.villagers().stream().filter(v -> q.isEmpty() || v.profession().toLowerCase(Locale.ROOT).contains(q) || v.activity().toLowerCase(Locale.ROOT).contains(q) || v.warnings().stream().anyMatch(w -> w.toLowerCase(Locale.ROOT).contains(q))).toList();
    }

    private List<PoiRecord> filterPois(List<PoiRecord> records) {
        String q = query();
        return records.stream().filter(r -> q.isEmpty() || r.type().toLowerCase(Locale.ROOT).contains(q) || Formatters.pos(r.pos()).contains(q)).sorted(Comparator.comparing(PoiRecord::type)).toList();
    }

    private String query() { return search == null ? "" : search.getText().toLowerCase(Locale.ROOT).trim(); }
    private void header(DrawContext c, int y, String a, String b, String d, String e, String f) { c.drawTextWithShadow(textRenderer, a, 24, y, 0xFFD966); c.drawTextWithShadow(textRenderer, b, 170, y, 0xFFD966); c.drawTextWithShadow(textRenderer, d, 270, y, 0xFFD966); c.drawTextWithShadow(textRenderer, e, 390, y, 0xFFD966); c.drawTextWithShadow(textRenderer, f, 520, y, 0xFFD966); }
    private void line(DrawContext c, int x, int y, String s) { c.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(s, width - x - 24), x, y, 0xE0E0E0); }
    private void score(DrawContext c, int x, int y, String label, int score) { int color = score >= 80 ? 0x80FF80 : score >= 50 ? 0xFFFFC857 : 0xFFFF6B6B; c.drawTextWithShadow(textRenderer, label + ": " + score + "/100", x, y, color); c.fill(x + 170, y + 2, x + 270, y + 8, 0xFF333333); c.fill(x + 170, y + 2, x + 170 + score, y + 8, color); }

    private enum Tab { OVERVIEW("Overview"), VILLAGERS("Villagers"), BEDS("Beds"), WORKSTATIONS("Workstations"), WARNINGS("Warnings"), DIAGNOSTICS("Diagnostics"); final String label; Tab(String label) { this.label = label; } }
}
