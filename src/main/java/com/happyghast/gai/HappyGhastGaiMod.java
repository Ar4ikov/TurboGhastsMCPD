package com.happyghast.gai;

import com.happyghast.gai.command.GaiCommands;
import com.happyghast.gai.gui.GhastMenuLock;
import com.happyghast.gai.handler.GhastInteractionHandler;
import com.happyghast.gai.handler.GhastDeathHandler;
import com.happyghast.gai.handler.GhastTrackingHandler;
import com.happyghast.gai.handler.PlateTickHandler;
import com.happyghast.gai.handler.PlayerJoinHandler;
import com.happyghast.gai.handler.TickHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HappyGhastGaiMod implements DedicatedServerModInitializer {

    public static final String MOD_ID = "turboghasts-mcpd";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeServer() {
        LOGGER.info("[TurboGhasts] Initializing TurboGhasts & MCPD mod...");

        CommandRegistrationCallback.EVENT.register(GaiCommands::register);

        UseEntityCallback.EVENT.register(GhastInteractionHandler::onUseEntity);

        ServerTickEvents.END_SERVER_TICK.register(GhastTrackingHandler::onServerTick);
        ServerTickEvents.END_SERVER_TICK.register(TickHandler::onServerTick);
        ServerTickEvents.END_SERVER_TICK.register(PlateTickHandler::onServerTick);

        ServerPlayConnectionEvents.JOIN.register(PlayerJoinHandler::onPlayerJoin);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            GhastMenuLock.onPlayerDisconnect(handler.getPlayer().getUuid(), server);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register(GhastDeathHandler::onAfterDeath);

        LOGGER.info("[TurboGhasts] TurboGhasts & MCPD v{} initialized!", "1.0.11");
    }
}
