package com.villagerscheduleviewer.diagnostic;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public record PathDiagnostics(BlockPos destination, int currentNodeIndex, int pathLength, List<BlockPos> nodes,
                              long attemptTicks, long lastSuccessfulUpdate, boolean stuck, String status) {
    private static final java.util.Map<Integer, PathState> STATES = new java.util.HashMap<>();

    public static PathDiagnostics inspect(VillagerEntity villager) {
        Object path = villager.getNavigation().getCurrentPath();
        int entityId = villager.getId();
        long now = villager.getWorld().getTime();
        List<BlockPos> nodes = readNodes(path);
        BlockPos destination = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
        int currentIndex = readInt(path, "getCurrentNodeIndex", 0);
        PathState state = STATES.computeIfAbsent(entityId, ignored -> new PathState(villager.getBlockPos(), now, now, destination));
        double moved = Math.sqrt(squaredDistance(villager.getBlockPos(), state.lastPosition));
        boolean destinationChanged = destination != null && !destination.equals(state.lastDestination);
        if (moved > 1.25 || destinationChanged) {
            state.lastPosition = villager.getBlockPos();
            state.lastProgressTick = now;
            if (destinationChanged) state.startedAt = now;
            state.lastDestination = destination;
            state.recalculations++;
        }
        boolean hasPath = path != null && !nodes.isEmpty();
        boolean stuck = hasPath && now - state.lastProgressTick > 100 && villager.getVelocity().horizontalLengthSquared() < 0.0004;
        String status = !hasPath ? "No active path" : stuck ? "No progress for " + (now - state.lastProgressTick) + " ticks" : "Path updating";
        return new PathDiagnostics(destination, currentIndex, nodes.size(), nodes, hasPath ? now - state.startedAt : 0, state.lastProgressTick, stuck, status);
    }

    private static List<BlockPos> readNodes(Object path) {
        List<BlockPos> nodes = new ArrayList<>();
        if (path == null) return nodes;
        int length = readInt(path, "getLength", 0);
        for (int i = 0; i < length && i < 128; i++) {
            Object node = invoke(path, "getNode", new Class<?>[]{int.class}, new Object[]{i});
            BlockPos pos = nodeToPos(node);
            if (pos != null) nodes.add(pos);
        }
        return nodes;
    }

    private static double squaredDistance(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static BlockPos nodeToPos(Object node) {
        if (node == null) return null;
        try {
            int x = node.getClass().getField("x").getInt(node);
            int y = node.getClass().getField("y").getInt(node);
            int z = node.getClass().getField("z").getInt(node);
            return new BlockPos(x, y, z);
        } catch (ReflectiveOperationException ignored) { return null; }
    }

    private static int readInt(Object target, String method, int fallback) {
        Object value = invoke(target, method, new Class<?>[0], new Object[0]);
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static Object invoke(Object target, String method, Class<?>[] parameterTypes, Object[] args) {
        if (target == null) return null;
        try {
            Method m = target.getClass().getMethod(method, parameterTypes);
            return m.invoke(target, args);
        } catch (ReflectiveOperationException ignored) { return null; }
    }

    private static final class PathState {
        private BlockPos lastPosition;
        private long startedAt;
        private long lastProgressTick;
        private BlockPos lastDestination;
        private int recalculations;
        private PathState(BlockPos lastPosition, long startedAt, long lastProgressTick, BlockPos lastDestination) {
            this.lastPosition = lastPosition;
            this.startedAt = startedAt;
            this.lastProgressTick = lastProgressTick;
            this.lastDestination = lastDestination;
        }
    }
}
