package com.happyghast.gai.handler;

import com.happyghast.gai.HappyGhastGaiMod;
import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.plate.PlateDisplayManager;
import com.happyghast.gai.plate.SirenManager;
import com.happyghast.gai.zone.ImpoundZone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Set;
import java.util.UUID;

public class TickHandler {

    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 200;

    public static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;

        GhastRegistryState state = GhastRegistryState.get(server);
        GaiConfig config = state.getConfig();

        if (!config.isImpoundZoneConfigured()) return;

        long now = System.currentTimeMillis();

        for (GhastVehicleData data : state.getAllGhasts()) {
            if (data.isRegistered() || data.isImpounded()) continue;
            if (now > data.getRegistrationDeadline()) {
                impoundGhast(server, data, state, config);
            }
        }
    }

    public static void impoundGhast(MinecraftServer server, GhastVehicleData data,
                                    GhastRegistryState state, GaiConfig config) {
        data.setImpounded(true);
        state.markDirty();

        ServerWorld impoundWorld = getWorldByDimension(server, config.getImpoundDimension());
        if (impoundWorld == null) {
            HappyGhastGaiMod.LOGGER.error("[GAI] Impound dimension not found: {}", config.getImpoundDimension());
            return;
        }

        Entity ghastEntity = findGhastInWorlds(server, data.getGhastUuid());
        if (ghastEntity instanceof HappyGhastEntity ghast) {
            removePlatesAllWorlds(server, data);

            Vec3d center = ImpoundZone.getZoneCenter(config);
            ghast.teleport(impoundWorld, center.x, center.y, center.z,
                    Set.of(), ghast.getYaw(), ghast.getPitch(), false);

            HappyGhastGaiMod.LOGGER.info("[GAI] Impounded ghast {} at {}", data.getGhastUuid(), center);

            if (data.getOwnerUuid() != null) {
                ServerPlayerEntity owner = server.getPlayerManager().getPlayer(data.getOwnerUuid());
                if (owner != null) {
                    owner.sendMessage(Text.literal(
                            "\u00a7c[\u0413\u0410\u0418] \u0412\u0430\u0448 \u0433\u0430\u0441\u0442 \u0431\u044B\u043B \u043A\u043E\u043D\u0444\u0438\u0441\u043A\u043E\u0432\u0430\u043D \u0438 \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u0435\u043D \u043D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0443!"), false);
                }
            }

            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                p.sendMessage(Text.literal(
                        "\u00a7e[\u0413\u0410\u0418] \u041D\u0435\u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u043D\u044B\u0439 \u0433\u0430\u0441\u0442 \u043A\u043E\u043D\u0444\u0438\u0441\u043A\u043E\u0432\u0430\u043D \u043D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0443."), false);
            }
        }
    }

    public static void releaseGhast(MinecraftServer server, GhastVehicleData data, GhastRegistryState state) {
        data.setImpounded(false);
        state.markDirty();

        ServerWorld homeWorld = getWorldByDimension(server, data.getHomeDimension());
        if (homeWorld == null) {
            HappyGhastGaiMod.LOGGER.error("[GAI] Home dimension not found: {}", data.getHomeDimension());
            return;
        }

        Entity ghastEntity = findGhastInWorlds(server, data.getGhastUuid());
        if (ghastEntity instanceof HappyGhastEntity ghast) {
            removePlatesAllWorlds(server, data);

            ghast.teleport(homeWorld,
                    data.getHomePos().getX() + 0.5,
                    data.getHomePos().getY(),
                    data.getHomePos().getZ() + 0.5,
                    Set.of(), ghast.getYaw(), ghast.getPitch(), false);

            if (data.isRegistered()) {
                PlateDisplayManager.createPlates(homeWorld, ghast, data);
                if (data.isGaiMode()) {
                    SirenManager.createSirens(homeWorld, ghast, data);
                }
                state.markDirty();
            }
            HappyGhastGaiMod.LOGGER.info("[GAI] Released ghast {} to home {}", data.getGhastUuid(), data.getHomePos());
        }
    }

    public static void removePlatesAllWorlds(MinecraftServer server, GhastVehicleData data) {
        for (ServerWorld world : server.getWorlds()) {
            PlateDisplayManager.removePlates(world, data);
        }
        if (data.isGaiMode()) {
            SirenManager.removeSirensAllWorlds(server, data);
        }
    }

    public static Entity findGhastInWorlds(MinecraftServer server, UUID ghastUuid) {
        for (ServerWorld world : server.getWorlds()) {
            Entity entity = world.getEntity(ghastUuid);
            if (entity != null) return entity;
        }
        return null;
    }

    private static ServerWorld getWorldByDimension(MinecraftServer server, String dimensionId) {
        Identifier id = Identifier.tryParse(dimensionId);
        if (id == null) return null;
        RegistryKey<net.minecraft.world.World> key = RegistryKey.of(RegistryKeys.WORLD, id);
        return server.getWorld(key);
    }
}
