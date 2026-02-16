package com.happyghast.gai.gui;

import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.handler.TickHandler;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ImpoundReleaseGui {

    public static void open(ServerPlayerEntity player, HappyGhastEntity ghast,
                            GhastVehicleData data, GhastRegistryState state) {
        GaiConfig config = state.getConfig();
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0412\u044B\u043A\u0443\u043F \u0433\u0430\u0441\u0442\u0430"));

        int totalEmeralds = config.getImpoundReleaseCostEmeralds();
        int totalDiamonds = config.getImpoundReleaseCostDiamonds();
        if (!data.isRegistered()) {
            totalEmeralds += config.getRegistrationCostEmeralds();
            totalDiamonds += config.getRegistrationCostDiamonds();
        }

        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("\u00a7a\u0418\u0437\u0443\u043C\u0440\u0443\u0434\u044B: " + totalEmeralds))
                .setCount(Math.max(1, Math.min(64, totalEmeralds)))
                .build());
        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("\u00a7b\u0410\u043B\u043C\u0430\u0437\u044B: " + totalDiamonds))
                .setCount(Math.max(1, Math.min(64, totalDiamonds)))
                .build());

        final int costEmeralds = totalEmeralds;
        final int costDiamonds = totalDiamonds;

        gui.setSlot(14, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7a\u041E\u043F\u043B\u0430\u0442\u0438\u0442\u044C \u0438 \u0437\u0430\u0431\u0440\u0430\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    if (!hasResources(player, costEmeralds, costDiamonds)) {
                        player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0440\u0435\u0441\u0443\u0440\u0441\u043E\u0432!"), false);
                        gui.close();
                        return;
                    }
                    consumeResources(player, costEmeralds, costDiamonds);
                    config.recordTransaction("release", costEmeralds, costDiamonds);
                    state.markDirty();

                    MinecraftServer server = ((net.minecraft.server.world.ServerWorld) player.getEntityWorld()).getServer();
                    TickHandler.releaseGhast(server, data, state);

                    if (!data.isRegistered()) {
                        data.setOwnerUuid(player.getUuid());
                        data.setRegistered(true);
                        state.markDirty();
                    }

                    player.sendMessage(Text.literal(
                            "\u00a7a[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u0432\u044B\u043A\u0443\u043F\u043B\u0435\u043D \u0438 \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u0435\u043D \u0434\u043E\u043C\u043E\u0439!"), false);
                    gui.close();
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7c\u041E\u0442\u043C\u0435\u043D\u0430"))
                .setCallback((index, type, action) -> gui.close())
                .build());

        gui.open();
    }

    private static boolean hasResources(ServerPlayerEntity player, int emeralds, int diamonds) {
        return countItem(player, Items.EMERALD) >= emeralds && countItem(player, Items.DIAMOND) >= diamonds;
    }

    private static void consumeResources(ServerPlayerEntity player, int emeralds, int diamonds) {
        removeItems(player, Items.EMERALD, emeralds);
        removeItems(player, Items.DIAMOND, diamonds);
    }

    private static int countItem(ServerPlayerEntity player, net.minecraft.item.Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) count += stack.getCount();
        }
        return count;
    }

    private static void removeItems(ServerPlayerEntity player, net.minecraft.item.Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.decrement(take);
                remaining -= take;
            }
        }
    }
}
