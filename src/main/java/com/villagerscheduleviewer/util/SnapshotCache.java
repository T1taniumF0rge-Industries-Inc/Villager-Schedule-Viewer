package com.villagerscheduleviewer.util;

import com.villagerscheduleviewer.brain.VillagerSnapshot;
import com.villagerscheduleviewer.client.VillagerScheduleViewerClient;
import net.minecraft.entity.passive.VillagerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Small client-only cache used by HUD and overlay render paths so expensive brain reads happen on a tick cadence rather than every frame.
 */
public final class SnapshotCache {
    private static final Map<UUID, VillagerSnapshot> SNAPSHOTS = new HashMap<>();
    private SnapshotCache() {}

    public static VillagerSnapshot get(VillagerEntity villager) {
        VillagerSnapshot cached = SNAPSHOTS.get(villager.getUuid());
        int refreshTicks = Math.max(1, VillagerScheduleViewerClient.CONFIG.cacheRefreshTicks);
        if (cached == null || villager.getWorld().getTime() - cached.capturedAt() >= refreshTicks) {
            cached = VillagerSnapshot.capture(villager);
            SNAPSHOTS.put(villager.getUuid(), cached);
        }
        return cached;
    }
}
