package com.villagerscheduleviewer.village;

public record VillageHealthScores(int populationHealth, int employment, int housing, int pathfinding, int breedingReadiness, int poiCoverage, int overall) {
    public static VillageHealthScores from(VillageScanResult result) {
        int villagers = Math.max(1, result.villagers().size());
        int critical = (int)result.villagers().stream().flatMap(v -> v.issues().stream()).filter(i -> i.severity().name().equals("CRITICAL")).count();
        int warning = (int)result.villagers().stream().flatMap(v -> v.issues().stream()).filter(i -> i.severity().name().equals("WARNING")).count();
        int housing = score(result.claimedBeds(), villagers, critical);
        int employment = score(result.claimedWorkstations(), villagers, warning);
        int path = Math.max(0, 100 - critical * 15 - warning * 5);
        int breeding = Math.max(0, 100 - (int)result.villagers().stream().filter(v -> !v.breeding().likelyEligible()).count() * 100 / villagers);
        int poi = Math.max(0, (housing + employment) / 2);
        int population = Math.max(0, 100 - critical * 10);
        int overall = (population + employment + housing + path + breeding + poi) / 6;
        return new VillageHealthScores(population, employment, housing, path, breeding, poi, overall);
    }
    private static int score(int found, int expected, int penalties) { return Math.max(0, Math.min(100, found * 100 / Math.max(1, expected) - penalties * 6)); }
}
