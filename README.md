# Villager Schedule Viewer

Villager Schedule Viewer is a **client-side Fabric mod** for Minecraft 1.21.x.  Its primary build target is Minecraft 1.21, while the implementation intentionally keeps version-sensitive code small and isolated so it can be carried across 1.21–1.21.11 with minimal mapping churn.

## Features

- Press **V** while looking at a villager to open a vanilla-styled inspection screen.
- Inspect profession, level, biome villager type, age, UUID, current inferred activity, active schedule, and upcoming transitions.
- Visual 0–24000 tick timeline with current-time marker and countdown to the next activity.
- Brain memory inspection for `HOME`, `JOB_SITE`, `MEETING_POINT`, `WALK_TARGET`, `LOOK_TARGET`, `BREED_TARGET`, and `INTERACTION_TARGET`.
- Warnings for common villager-management problems such as missing beds, missing workstations, and potentially stuck behavior.
- Lightweight HUD for the currently targeted villager.
- Optional world-space POI outlines for linked beds, workstations, and meeting points.
- Nearby-villager dashboard bound to **N** for aggregate village checks.
- Mod Menu integration when Mod Menu is installed, with graceful operation without server-side installation.

## Keybinds

| Key | Action |
| --- | --- |
| V | Open the detailed villager viewer |
| H | Toggle the HUD |
| B | Toggle POI overlays |
| N | Open the nearby villager dashboard |


## Village Doctor diagnostics

The inspector now focuses on explaining problems and giving actionable fixes, not just displaying raw data.  When a villager has a claimed bed or workstation, the screen shows exact coordinates, distance, and an estimated non-invasive reachability status.  The **Highlight Workstation** and **Highlight Bed** buttons create temporary world-space highlights with a vertical beam, block outline, tracer, and label.

The Village Doctor reports each issue with:

- **Problem**
- **Explanation**
- **Severity** (`Info`, `Warning`, or `Critical`)
- **Suggested Fix**

Diagnostics cover missing or unreachable beds/workstations, missing meeting points, stuck pathfinding, breeding readiness, excessive POI distances, and duplicate POI claims in the village dashboard.

## Dashboard tabs

The dashboard includes Overview, Villagers, Beds, Workstations, Warnings, and Diagnostics tabs.  It provides a cached village health score across population health, employment, housing, pathfinding, breeding readiness, POI coverage, and overall village health.  Bed and workstation tables are searchable and rows can be clicked to highlight the POI and jump to the owning villager when available.

## Performance model

Village scans are cached and controlled by `villageScanIntervalTicks`.  Overlay rendering is distance-limited, target villager snapshots are cached, and block scans are capped so large villages remain usable.

## Compatibility notes

This project uses Fabric Loader, Fabric API, Yarn mappings, and Java 21.  The source is organized into focused packages (`client`, `gui`, `hud`, `render`, `schedule`, `brain`, `config`, `util`, `mixin`, and `compat`) to make future Minecraft updates straightforward.

The mod is intentionally client-only: it reads synced client entity data and available client-side brain state for inspection, but it does not mutate villager behavior and does not require a server mod.

## Building

```bash
gradle build
```

The default dependency set targets Minecraft `1.21`, Yarn `1.21+build.9`, Fabric Loader `0.16.14`, and Fabric API `0.102.0+1.21`.
