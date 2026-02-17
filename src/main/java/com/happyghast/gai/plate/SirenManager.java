package com.happyghast.gai.plate;

import com.happyghast.gai.data.GhastVehicleData;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Set;
import java.util.UUID;

public class SirenManager {

    private static final double SIREN_Y_OFFSET = 7.0;
    private static final float SIREN_SCALE = 1.8f;
    private static final double SIDE_OFFSET = 1.0;

    private static final String GAI_PLATE_TEXT = "\u0413322\u0410\u0418 67";

    public static String getGaiPlateText() {
        return GAI_PLATE_TEXT;
    }

    public static void createSirens(ServerWorld world, Entity ghast, GhastVehicleData data) {
        removeSirens(world, data);

        Vec3d basePos = getSirenBasePos(ghast);

        UUID blueUuid = spawnBlockDisplay(world, ghast, basePos, -SIDE_OFFSET, false);
        UUID redUuid = spawnBlockDisplay(world, ghast, basePos, SIDE_OFFSET, true);

        data.setSirenRedUuid(redUuid);
        data.setSirenBlueUuid(blueUuid);
    }

    public static void removeSirens(ServerWorld world, GhastVehicleData data) {
        removeEntity(world, data.getSirenRedUuid());
        removeEntity(world, data.getSirenBlueUuid());
        data.setSirenRedUuid(null);
        data.setSirenBlueUuid(null);
    }

    public static void removeSirensAllWorlds(net.minecraft.server.MinecraftServer server, GhastVehicleData data) {
        for (ServerWorld world : server.getWorlds()) {
            removeEntity(world, data.getSirenRedUuid());
            removeEntity(world, data.getSirenBlueUuid());
        }
        data.setSirenRedUuid(null);
        data.setSirenBlueUuid(null);
    }

    public static void updateSirenPositions(ServerWorld world, Entity ghast, GhastVehicleData data) {
        Vec3d basePos = getSirenBasePos(ghast);

        updateBlockDisplay(world, ghast, data.getSirenBlueUuid(), basePos, -SIDE_OFFSET);
        updateBlockDisplay(world, ghast, data.getSirenRedUuid(), basePos, SIDE_OFFSET);
    }

    private static final int COLOR_RED = 0xFF0000;
    private static final int COLOR_BLUE = 0x004DFF;

    public static void spawnSirenParticles(ServerWorld world, Entity ghast, GhastVehicleData data, boolean bluePhase) {
        Vec3d basePos = getSirenBasePos(ghast);
        float yawRad = (float) Math.toRadians(ghast.getYaw());
        double rightX = Math.cos(yawRad);
        double rightZ = Math.sin(yawRad);

        if (bluePhase) {
            Vec3d blueCenter = basePos.add(-rightX * SIDE_OFFSET, 0, -rightZ * SIDE_OFFSET);
            spawnCubeParticles(world, blueCenter, COLOR_BLUE);
        } else {
            Vec3d redCenter = basePos.add(rightX * SIDE_OFFSET, 0, rightZ * SIDE_OFFSET);
            spawnCubeParticles(world, redCenter, COLOR_RED);
        }
    }

    public static void validateSirens(ServerWorld world, Entity ghast, GhastVehicleData data) {
        if (!data.isGaiMode()) return;

        boolean needsRecreate = false;
        if (data.getSirenRedUuid() != null) {
            if (world.getEntity(data.getSirenRedUuid()) == null) needsRecreate = true;
        } else {
            needsRecreate = true;
        }
        if (data.getSirenBlueUuid() != null) {
            if (world.getEntity(data.getSirenBlueUuid()) == null) needsRecreate = true;
        } else {
            needsRecreate = true;
        }

        if (needsRecreate) {
            removeSirens(world, data);
            createSirens(world, ghast, data);
        }
    }

    private static UUID spawnBlockDisplay(ServerWorld world, Entity ghast, Vec3d basePos,
                                           double sideOffset, boolean isRed) {
        DisplayEntity.BlockDisplayEntity blockDisplay = new DisplayEntity.BlockDisplayEntity(
                EntityType.BLOCK_DISPLAY, world);

        float yawRad = (float) Math.toRadians(ghast.getYaw());
        double rightX = Math.cos(yawRad) * sideOffset;
        double rightZ = Math.sin(yawRad) * sideOffset;
        blockDisplay.refreshPositionAndAngles(
                basePos.x + rightX, basePos.y, basePos.z + rightZ,
                ghast.getYaw(), 0);

        if (isRed) {
            blockDisplay.setBlockState(Blocks.REDSTONE_BLOCK.getDefaultState());
        } else {
            blockDisplay.setBlockState(Blocks.LAPIS_BLOCK.getDefaultState());
        }

        float offset = -SIREN_SCALE / 2f;
        blockDisplay.setTransformation(new AffineTransformation(
                new Vector3f(offset, offset, offset),
                null,
                new Vector3f(SIREN_SCALE, SIREN_SCALE, SIREN_SCALE),
                null
        ));
        blockDisplay.setBillboardMode(DisplayEntity.BillboardMode.CENTER);

        world.spawnEntity(blockDisplay);
        return blockDisplay.getUuid();
    }

    private static void updateBlockDisplay(ServerWorld world, Entity ghast,
                                            @Nullable UUID uuid, Vec3d basePos, double sideOffset) {
        if (uuid == null) return;
        Entity entity = world.getEntity(uuid);
        if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplay) {
            float yawRad = (float) Math.toRadians(ghast.getYaw());
            double rightX = Math.cos(yawRad) * sideOffset;
            double rightZ = Math.sin(yawRad) * sideOffset;

            blockDisplay.teleport(world,
                    basePos.x + rightX, basePos.y, basePos.z + rightZ,
                    Set.of(), ghast.getYaw(), 0, false);
        }
    }

    private static void spawnCubeParticles(ServerWorld world, Vec3d center, int color) {
        DustParticleEffect dust = new DustParticleEffect(color, 2.0f);
        double half = SIREN_SCALE / 2.0;

        for (int edge = 0; edge < 12; edge++) {
            for (double t = 0; t <= 1.0; t += 0.5) {
                double px = center.x, py = center.y, pz = center.z;
                double v = -half + t * (2 * half);
                switch (edge) {
                    case 0 -> { px += v; py += -half; pz += -half; }
                    case 1 -> { px += v; py += -half; pz += half; }
                    case 2 -> { px += v; py += half; pz += -half; }
                    case 3 -> { px += v; py += half; pz += half; }
                    case 4 -> { py += v; px += -half; pz += -half; }
                    case 5 -> { py += v; px += -half; pz += half; }
                    case 6 -> { py += v; px += half; pz += -half; }
                    case 7 -> { py += v; px += half; pz += half; }
                    case 8 -> { pz += v; px += -half; py += -half; }
                    case 9 -> { pz += v; px += -half; py += half; }
                    case 10 -> { pz += v; px += half; py += -half; }
                    case 11 -> { pz += v; px += half; py += half; }
                }
                world.spawnParticles(dust, px, py, pz, 1, 0, 0, 0, 0);
            }
        }
    }

    private static Vec3d getSirenBasePos(Entity ghast) {
        return new Vec3d(ghast.getX(), ghast.getY() + SIREN_Y_OFFSET, ghast.getZ());
    }

    private static void removeEntity(ServerWorld world, @Nullable UUID uuid) {
        if (uuid == null) return;
        Entity entity = world.getEntity(uuid);
        if (entity != null) entity.discard();
    }
}
