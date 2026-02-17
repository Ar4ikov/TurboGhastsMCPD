package com.happyghast.gai.plate;

import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class PlateDisplayManager {

    private static final double FRONT_FORWARD_OFFSET = 2.3;
    private static final double BACK_FORWARD_OFFSET = -3.0;
    private static final double Y_OFFSET = 1.2;

    public static void createPlates(ServerWorld world, Entity ghast, GhastVehicleData data) {
        String plateText = data.getPlateNumber();
        if (plateText.isEmpty()) return;

        String displayText = data.isGaiMode() ? SirenManager.getGaiPlateText() : plateText;
        UUID frontUuid = spawnPlateEntity(world, ghast, displayText, FRONT_FORWARD_OFFSET);
        UUID backUuid = spawnPlateEntity(world, ghast, displayText, BACK_FORWARD_OFFSET);

        data.setFrontPlateEntityUuid(frontUuid);
        data.setBackPlateEntityUuid(backUuid);
    }

    public static void updatePlatePositions(ServerWorld world, Entity ghast, GhastVehicleData data) {
        if (data.getFrontPlateEntityUuid() != null) {
            updateSinglePlate(world, ghast, data.getFrontPlateEntityUuid(), FRONT_FORWARD_OFFSET);
        }
        if (data.getBackPlateEntityUuid() != null) {
            updateSinglePlate(world, ghast, data.getBackPlateEntityUuid(), BACK_FORWARD_OFFSET);
        }
    }

    public static void removePlates(ServerWorld world, GhastVehicleData data) {
        removeSinglePlate(world, data.getFrontPlateEntityUuid());
        removeSinglePlate(world, data.getBackPlateEntityUuid());
        data.setFrontPlateEntityUuid(null);
        data.setBackPlateEntityUuid(null);
    }

    private static UUID spawnPlateEntity(ServerWorld world, Entity ghast, String plateText, double forwardOffset) {
        Vec3d pos = calculatePlatePos(ghast, forwardOffset);

        DisplayEntity.TextDisplayEntity textDisplay = new DisplayEntity.TextDisplayEntity(
                EntityType.TEXT_DISPLAY, world
        );
        textDisplay.refreshPositionAndAngles(pos.x, pos.y, pos.z, ghast.getYaw(), 0);

        Text styledText = Text.literal(plateText).setStyle(
                Style.EMPTY.withColor(Formatting.GOLD).withBold(true));
        textDisplay.setText(styledText);
        textDisplay.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        textDisplay.setBackground(0xA0000000);
        textDisplay.setTextOpacity((byte) -1);
        textDisplay.setLineWidth(200);
        textDisplay.setDisplayFlags((byte) 0x01);

        world.spawnEntity(textDisplay);
        return textDisplay.getUuid();
    }

    private static void updateSinglePlate(ServerWorld world, Entity ghast, UUID plateUuid, double forwardOffset) {
        Entity plateEntity = world.getEntity(plateUuid);
        if (plateEntity instanceof DisplayEntity.TextDisplayEntity textDisplay) {
            Vec3d pos = calculatePlatePos(ghast, forwardOffset);
            textDisplay.teleport(world, pos.x, pos.y, pos.z,
                    Set.of(), ghast.getYaw(), 0, false);
        }
    }

    private static void removeSinglePlate(ServerWorld world, @Nullable UUID plateUuid) {
        if (plateUuid == null) return;
        Entity plateEntity = world.getEntity(plateUuid);
        if (plateEntity != null) plateEntity.discard();
    }

    private static Vec3d calculatePlatePos(Entity ghast, double forwardOffset) {
        float yawRad = (float) Math.toRadians(ghast.getYaw());
        double offsetX = -Math.sin(yawRad) * forwardOffset;
        double offsetZ = Math.cos(yawRad) * forwardOffset;
        return new Vec3d(
                ghast.getX() + offsetX,
                ghast.getY() + Y_OFFSET,
                ghast.getZ() + offsetZ
        );
    }

    public static void refreshPlateText(ServerWorld world, GhastVehicleData data) {
        String displayText = data.isGaiMode() ? SirenManager.getGaiPlateText() : data.getPlateNumber();
        if (displayText.isEmpty()) return;

        Text styledText = Text.literal(displayText).setStyle(
                Style.EMPTY.withColor(Formatting.GOLD).withBold(true));

        if (data.getFrontPlateEntityUuid() != null) {
            Entity e = world.getEntity(data.getFrontPlateEntityUuid());
            if (e instanceof DisplayEntity.TextDisplayEntity td) {
                td.setText(styledText);
            }
        }
        if (data.getBackPlateEntityUuid() != null) {
            Entity e = world.getEntity(data.getBackPlateEntityUuid());
            if (e instanceof DisplayEntity.TextDisplayEntity td) {
                td.setText(styledText);
            }
        }
    }

    public static void validatePlates(ServerWorld world, Entity ghast, GhastVehicleData data, GhastRegistryState state) {
        if (!data.isRegistered() || data.getPlateNumber().isEmpty()) return;

        boolean needsRecreate = false;
        if (data.getFrontPlateEntityUuid() != null) {
            if (world.getEntity(data.getFrontPlateEntityUuid()) == null) needsRecreate = true;
        } else {
            needsRecreate = true;
        }
        if (data.getBackPlateEntityUuid() != null) {
            if (world.getEntity(data.getBackPlateEntityUuid()) == null) needsRecreate = true;
        } else {
            needsRecreate = true;
        }

        if (needsRecreate) {
            removePlates(world, data);
            createPlates(world, ghast, data);
            state.markDirty();
        }
    }
}
