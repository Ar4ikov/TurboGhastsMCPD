package com.happyghast.gai.gui;

import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.handler.PlateTickHandler;
import com.happyghast.gai.handler.TickHandler;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GhastTpGui {

    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 5 * 60 * 1000;

    public static long getRemainingCooldown(UUID playerUuid) {
        Long until = COOLDOWNS.get(playerUuid);
        if (until == null) return 0;
        long remaining = until - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    public static void setCooldown(UUID playerUuid) {
        COOLDOWNS.put(playerUuid, System.currentTimeMillis() + COOLDOWN_MS);
    }

    public static boolean isOnCooldown(UUID playerUuid) {
        return getRemainingCooldown(playerUuid) > 0;
    }

    public static String formatCooldown(long ms) {
        long sec = ms / 1000;
        long min = sec / 60;
        sec = sec % 60;
        return String.format("%d мин. %02d сек.", min, sec);
    }

    public static void open(ServerPlayerEntity player, GhastRegistryState state, boolean isAdmin) {
        List<GhastVehicleData> ghasts;
        if (isAdmin) {
            ghasts = new ArrayList<>(state.getRegisteredGhasts());
        } else {
            ghasts = state.getGhastsByOwner(player.getUuid());
            ghasts.removeIf(d -> !d.isRegistered());
        }

        if (ghasts.isEmpty()) {
            player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u043D\u044B\u0445 \u0433\u0430\u0441\u0442\u043E\u0432 \u0434\u043B\u044F \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442\u0430."), false);
            return;
        }

        int rows = Math.min(6, (int) Math.ceil(ghasts.size() / 9.0) + 1);
        ScreenHandlerType<?> screenType = switch (rows) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };

        SimpleGui gui = new SimpleGui(screenType, player, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0422\u0435\u043B\u0435\u043F\u043E\u0440\u0442 \u0433\u0430\u0441\u0442\u0430"));

        MinecraftServer server = ((ServerWorld) player.getEntityWorld()).getServer();

        int slot = 0;
        for (GhastVehicleData data : ghasts) {
            if (slot >= rows * 9 - 1) break;

            String name = data.getCustomName().isEmpty() ? "\u0413\u0430\u0441\u0442" : data.getCustomName();
            String plate = data.getPlateNumber().isEmpty() ? "" : " [\u00a7e" + data.getPlateNumber() + "\u00a7f]";
            boolean isImpounded = data.isImpounded();

            GuiElementBuilder btn = new GuiElementBuilder(isImpounded ? Items.BARRIER : Items.GHAST_TEAR)
                    .setName(Text.literal("\u00a7f" + name + plate));

            if (isImpounded) {
                btn.addLoreLine(Text.literal("\u00a7c\u041D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0435"));
                btn.addLoreLine(Text.literal("\u00a78\u041D\u0435\u043B\u044C\u0437\u044F \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442\u0438\u0440\u043E\u0432\u0430\u0442\u044C"));
            } else {
                String ownerName = "";
                if (isAdmin && data.getOwnerUuid() != null) {
                    ServerPlayerEntity ownerPlayer = server.getPlayerManager().getPlayer(data.getOwnerUuid());
                    ownerName = ownerPlayer != null ? ownerPlayer.getName().getString()
                            : data.getOwnerUuid().toString().substring(0, 8) + "...";
                    btn.addLoreLine(Text.literal("\u00a77\u0412\u043B\u0430\u0434\u0435\u043B\u0435\u0446: \u00a7b" + ownerName));
                }
                btn.addLoreLine(Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442\u0430"));
                btn.setCallback((index, type, action) -> {
                    gui.close();
                    executeTp(player, data, state, isAdmin);
                });
            }

            gui.setSlot(slot, btn.build());
            slot++;
        }

        gui.open();
    }

    public static void executeTp(ServerPlayerEntity player, GhastVehicleData data,
                                  GhastRegistryState state, boolean isAdmin) {
        if (data.isImpounded()) {
            player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0435! \u0422\u0435\u043B\u0435\u043F\u043E\u0440\u0442 \u043D\u0435\u0432\u043E\u0437\u043C\u043E\u0436\u0435\u043D."), false);
            return;
        }

        if (!isAdmin) {
            if (data.getOwnerUuid() == null || !data.getOwnerUuid().equals(player.getUuid())) {
                player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u042D\u0442\u043E \u043D\u0435 \u0432\u0430\u0448 \u0433\u0430\u0441\u0442!"), false);
                return;
            }
        }

        if (!isAdmin && isOnCooldown(player.getUuid())) {
            long remaining = getRemainingCooldown(player.getUuid());
            player.sendMessage(Text.literal(
                    "\u00a7c[\u0413\u0410\u0418] \u041A\u0443\u043B\u0434\u0430\u0443\u043D! \u041F\u043E\u0434\u043E\u0436\u0434\u0438\u0442\u0435: " + formatCooldown(remaining)), false);
            return;
        }

        MinecraftServer server = ((ServerWorld) player.getEntityWorld()).getServer();
        Entity ghastEntity = TickHandler.findGhastInWorlds(server, data.getGhastUuid());
        if (!(ghastEntity instanceof HappyGhastEntity ghast)) {
            player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D \u0432 \u043C\u0438\u0440\u0435!"), false);
            return;
        }

        TickHandler.removePlatesAllWorlds(server, data);

        ServerWorld playerWorld = (ServerWorld) player.getEntityWorld();
        double x = player.getX() + 2;
        double y = player.getY() + 1;
        double z = player.getZ() + 2;
        ghast.teleport(playerWorld, x, y, z, java.util.Set.of(), player.getYaw(), 0, false);

        if (data.isRegistered()) {
            PlateTickHandler.setTpFreeze(data.getGhastUuid());
            if (ghast instanceof MobEntity mob) {
                mob.setAiDisabled(true);
            }
        }

        if (data.isRegistered()) {
            com.happyghast.gai.plate.PlateDisplayManager.createPlates(playerWorld, ghast, data);
            state.markDirty();
        }

        if (!isAdmin) {
            setCooldown(player.getUuid());
        }

        String ghastName = data.getCustomName().isEmpty() ? "\u0413\u0430\u0441\u0442" : data.getCustomName();
        player.sendMessage(Text.literal(
                "\u00a7a[\u0413\u0410\u0418] " + ghastName + " \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442\u0438\u0440\u043E\u0432\u0430\u043D \u043A \u0432\u0430\u043C! \u0417\u0430\u043C\u043E\u0440\u043E\u0436\u0435\u043D \u043D\u0430 3 \u0441\u0435\u043A."), false);
    }
}
