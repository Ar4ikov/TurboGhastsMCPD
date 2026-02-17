package com.happyghast.gai.handler;

import com.happyghast.gai.HappyGhastGaiMod;
import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.gui.SpeedStageManager;
import com.happyghast.gai.plate.PlateDisplayManager;
import com.happyghast.gai.plate.SirenManager;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class GhastTrackingHandler {

    private static int tickCounter = 0;
    private static final int SCAN_INTERVAL = 100;

    public static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < SCAN_INTERVAL) return;
        tickCounter = 0;

        GhastRegistryState state = GhastRegistryState.get(server);
        GaiConfig config = state.getConfig();

        if (config.getGraceStartTimestamp() == 0) {
            config.setGraceStartTimestamp(System.currentTimeMillis());
            state.saveConfig();
        }

        for (ServerWorld world : server.getWorlds()) {
            String dimKey = world.getRegistryKey().getValue().toString();

            Box scanBox = new Box(-30000000, -64, -30000000, 30000000, 320, 30000000);
            for (HappyGhastEntity ghast : world.getEntitiesByClass(
                    HappyGhastEntity.class, scanBox, e -> !e.isBaby())) {

                if (!state.hasGhast(ghast.getUuid())) {
                    long now = System.currentTimeMillis();
                    long deadline = now + config.getDeadlinePeriodForNewGhast();
                    BlockPos homePos = ghast.getBlockPos();

                    GhastVehicleData data = new GhastVehicleData(
                            ghast.getUuid(), homePos, dimKey, now, deadline
                    );
                    state.addGhast(data);
                    HappyGhastGaiMod.LOGGER.info("[GAI] Registered new Happy Ghast {} at {}",
                            ghast.getUuid(), homePos);
                } else {
                    GhastVehicleData data = state.getGhast(ghast.getUuid());
                    if (data != null && data.isRegistered()) {
                        PlateDisplayManager.validatePlates(world, ghast, data, state);
                        if (data.isGaiMode()) {
                            SpeedStageManager.applyGaiSpeed(ghast);
                            SirenManager.validateSirens(world, ghast, data);
                        } else if (data.getStage() > 0) {
                            SpeedStageManager.applyStageSpeed(ghast, data.getStage());
                        }
                    }
                }
            }
        }
    }
}
