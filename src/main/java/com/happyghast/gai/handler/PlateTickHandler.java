package com.happyghast.gai.handler;

import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.gui.ParticleTrailManager;
import com.happyghast.gai.plate.PlateDisplayManager;
import com.happyghast.gai.zone.ImpoundZone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlateTickHandler {

    private static int containmentCounter = 0;
    private static final int CONTAINMENT_INTERVAL = 3;
    private static int particleCounter = 0;
    private static final int PARTICLE_INTERVAL = 4;
    private static int mileageCounter = 0;
    private static final int MILEAGE_INTERVAL = 5;

    private static final Map<UUID, Vec3d> lastGhastPositions = new ConcurrentHashMap<>();

    private static final Map<UUID, Long> TP_FREEZE_UNTIL = new ConcurrentHashMap<>();
    private static final long TP_FREEZE_DURATION_MS = 3000;

    private static final long SALE_FREEZE_DURATION_MS = 10000;

    public static void setTpFreeze(UUID ghastUuid) {
        TP_FREEZE_UNTIL.put(ghastUuid, System.currentTimeMillis() + TP_FREEZE_DURATION_MS);
    }

    public static void setSaleFreeze(UUID ghastUuid) {
        TP_FREEZE_UNTIL.put(ghastUuid, System.currentTimeMillis() + SALE_FREEZE_DURATION_MS);
    }

    public static void onServerTick(MinecraftServer server) {
        GhastRegistryState state = GhastRegistryState.get(server);

        boolean spawnParticles = false;
        particleCounter++;
        if (particleCounter >= PARTICLE_INTERVAL) {
            particleCounter = 0;
            spawnParticles = true;
        }

        boolean trackMileage = false;
        mileageCounter++;
        if (mileageCounter >= MILEAGE_INTERVAL) {
            mileageCounter = 0;
            trackMileage = true;
        }

        for (GhastVehicleData data : state.getRegisteredGhasts()) {
            boolean needsPlateUpdate = data.getFrontPlateEntityUuid() != null || data.getBackPlateEntityUuid() != null;
            boolean needsParticle = spawnParticles && !"none".equals(data.getParticleId());

            for (ServerWorld world : server.getWorlds()) {
                Entity ghast = world.getEntity(data.getGhastUuid());
                if (ghast != null) {
                    if (needsPlateUpdate) {
                        PlateDisplayManager.updatePlatePositions(world, ghast, data);
                    }
                    if (needsParticle) {
                        ParticleTrailManager.spawnTrail(world, ghast, data);
                    }
                    if (trackMileage && ghast.hasPassengers()) {
                        trackMileage(data, ghast);
                    } else if (trackMileage) {
                        lastGhastPositions.remove(data.getGhastUuid());
                    }
                    break;
                }
            }
        }

        checkTpFreezes(server, state);

        containmentCounter++;
        if (containmentCounter >= CONTAINMENT_INTERVAL) {
            containmentCounter = 0;
            enforceImpoundContainment(server, state);
        }
    }

    private static void trackMileage(GhastVehicleData data, Entity ghast) {
        Vec3d currentPos = new Vec3d(ghast.getX(), ghast.getY(), ghast.getZ());
        Vec3d lastPos = lastGhastPositions.get(data.getGhastUuid());
        if (lastPos != null) {
            double dist = lastPos.distanceTo(currentPos);
            if (dist > 0.1 && dist < 100) {
                data.addMileage(dist);
            }
        }
        lastGhastPositions.put(data.getGhastUuid(), currentPos);
    }

    private static void checkTpFreezes(MinecraftServer server, GhastRegistryState state) {
        if (TP_FREEZE_UNTIL.isEmpty()) return;
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Long>> it = TP_FREEZE_UNTIL.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            UUID ghastUuid = entry.getKey();

            for (ServerWorld world : server.getWorlds()) {
                Entity entity = world.getEntity(ghastUuid);
                if (entity instanceof HappyGhastEntity ghast) {
                    if (ghast.hasPassengers()) {
                        if (ghast instanceof MobEntity mob) mob.setAiDisabled(false);
                        it.remove();
                    } else if (now >= entry.getValue()) {
                        if (ghast instanceof MobEntity mob) mob.setAiDisabled(false);
                        it.remove();
                    }
                    break;
                }
            }
        }
    }

    private static void enforceImpoundContainment(MinecraftServer server, GhastRegistryState state) {
        GaiConfig config = state.getConfig();
        if (!config.isImpoundZoneConfigured()) return;

        Vec3d center = ImpoundZone.getZoneCenter(config);

        for (GhastVehicleData data : state.getImpoundedGhasts()) {
            for (ServerWorld world : server.getWorlds()) {
                Entity entity = world.getEntity(data.getGhastUuid());
                if (entity instanceof HappyGhastEntity ghast) {
                    if (!ImpoundZone.isEntityInZone(ghast, config, world)) {
                        ServerWorld impoundWorld = ImpoundZone.getImpoundWorld(server, config);
                        if (impoundWorld != null) {
                            ghast.teleport(impoundWorld, center.x, center.y, center.z,
                                    Set.of(), ghast.getYaw(), ghast.getPitch(), false);
                        }
                    }
                    if (ghast.hasPassengers()) {
                        ghast.removeAllPassengers();
                    }
                    break;
                }
            }
        }
    }
}
