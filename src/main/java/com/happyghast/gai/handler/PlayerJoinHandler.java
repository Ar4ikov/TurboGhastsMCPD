package com.happyghast.gai.handler;

import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class PlayerJoinHandler {

    public static void onPlayerJoin(ServerPlayNetworkHandler handler,
                                     net.fabricmc.fabric.api.networking.v1.PacketSender sender,
                                     MinecraftServer server) {
        ServerPlayerEntity player = handler.player;

        server.execute(() -> {
            GhastRegistryState state = GhastRegistryState.get(server);

            List<GhastVehicleData> unregistered = state.getUnregisteredGhasts();
            List<GhastVehicleData> playerGhasts = state.getGhastsByOwner(player.getUuid());

            long unregPlayerGhasts = playerGhasts.stream()
                    .filter(d -> !d.isRegistered() && !d.isImpounded()).count();

            if (unregPlayerGhasts > 0) {
                GhastVehicleData soonest = playerGhasts.stream()
                        .filter(d -> !d.isRegistered() && !d.isImpounded())
                        .min((a, b) -> Long.compare(a.getRegistrationDeadline(), b.getRegistrationDeadline()))
                        .orElse(null);
                if (soonest != null) {
                    player.sendMessage(Text.literal(
                            "\u00a7e[\u0413\u0410\u0418] \u0423 \u0432\u0430\u0441 \u0435\u0441\u0442\u044C " + unregPlayerGhasts +
                                    " \u043D\u0435\u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u043D\u044B\u0445 \u0433\u0430\u0441\u0442\u043E\u0432! " +
                                    "\u0411\u043B\u0438\u0436\u0430\u0439\u0448\u0438\u0439 \u0441\u0440\u043E\u043A: " + soonest.getFormattedRemainingTime()), false);
                }
            }

            long totalUnregistered = unregistered.stream().filter(d -> !d.isImpounded()).count();
            if (totalUnregistered > 0) {
                player.sendMessage(Text.literal(
                        "\u00a76[\u0413\u0410\u0418] \u041D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435 " + totalUnregistered +
                                " \u043D\u0435\u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u043D\u044B\u0445 \u0433\u0430\u0441\u0442\u043E\u0432. " +
                                "\u0418\u0441\u043F\u043E\u043B\u044C\u0437\u0443\u0439\u0442\u0435 SHIFT+\u041F\u041A\u041C \u043F\u043E \u0433\u0430\u0441\u0442\u0443 \u0434\u043B\u044F \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438."), false);
            }

            long impoundedCount = state.getImpoundedGhasts().size();
            if (impoundedCount > 0) {
                player.sendMessage(Text.literal(
                        "\u00a7c[\u0413\u0410\u0418] \u041D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0435: " + impoundedCount + " \u0433\u0430\u0441\u0442\u043E\u0432."), false);
            }
        });
    }
}
