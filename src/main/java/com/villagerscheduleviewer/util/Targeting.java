package com.villagerscheduleviewer.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class Targeting {
    private Targeting() {}
    public static VillagerEntity getTargetedVillager(MinecraftClient client, double range) {
        if (client.player == null || client.world == null) return null;
        HitResult hit = client.crosshairTarget;
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof VillagerEntity villager) return villager;
        Vec3d start = client.player.getCameraPosVec(1.0F);
        Vec3d end = start.add(client.player.getRotationVec(1.0F).multiply(range));
        Box search = client.player.getBoundingBox().stretch(client.player.getRotationVec(1.0F).multiply(range)).expand(1.0);
        VillagerEntity best = null;
        double bestDistance = range * range;
        for (Entity entity : client.world.getOtherEntities(client.player, search, e -> e instanceof VillagerEntity)) {
            Box box = entity.getBoundingBox().expand(entity.getTargetingMargin());
            var optional = box.raycast(start, end);
            if (optional.isPresent()) {
                double d = start.squaredDistanceTo(optional.get());
                if (d < bestDistance) { bestDistance = d; best = (VillagerEntity) entity; }
            }
        }
        return best;
    }
}
