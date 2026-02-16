package com.happyghast.gai.handler;

import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.gui.GhastInfoGui;
import com.happyghast.gai.gui.GhastMenuLock;
import com.happyghast.gai.gui.ImpoundReleaseGui;
import com.happyghast.gai.gui.RegistrationGui;
import com.happyghast.gai.zone.ImpoundZone;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhastInteractionHandler {

    public static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand,
                                           Entity entity, @Nullable EntityHitResult hitResult) {
        if (world.isClient()) return ActionResult.PASS;
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!player.isSneaking()) return ActionResult.PASS;
        if (!(entity instanceof HappyGhastEntity ghast)) return ActionResult.PASS;
        if (ghast.isBaby()) return ActionResult.PASS;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

        MinecraftServer server = ((net.minecraft.server.world.ServerWorld) serverPlayer.getEntityWorld()).getServer();
        GhastRegistryState state = GhastRegistryState.get(server);
        GhastVehicleData data = state.getGhast(ghast.getUuid());

        if (data == null) return ActionResult.PASS;

        GaiConfig config = state.getConfig();
        boolean isOwner = serverPlayer.getUuid().equals(data.getOwnerUuid());
        boolean isAdmin = serverPlayer.getCommandSource().getPermissions()
                .hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS));

        if (data.isImpounded() && ImpoundZone.isPlayerInZone(serverPlayer, config)) {
            ImpoundReleaseGui.open(serverPlayer, ghast, data, state);
        } else if (!data.isRegistered()) {
            if (isOwner || !data.isRegistered()) {
                RegistrationGui.openNameInput(serverPlayer, ghast, data, state);
            }
        } else if (isOwner || isAdmin) {
            if (GhastMenuLock.tryLock(ghast.getUuid(), serverPlayer, isAdmin, ghast, server)) {
                GhastInfoGui.open(serverPlayer, ghast, data, state);
            }
        } else {
            GhastInfoGui.openReadOnly(serverPlayer, ghast, data, state);
        }

        return ActionResult.SUCCESS;
    }
}
