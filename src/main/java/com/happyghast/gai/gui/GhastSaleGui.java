package com.happyghast.gai.gui;

import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class GhastSaleGui {

    public static void openPlayerSelection(ServerPlayerEntity seller, HappyGhastEntity ghast,
                                            GhastVehicleData data, GhastRegistryState state) {
        MinecraftServer server = ((net.minecraft.server.world.ServerWorld) seller.getEntityWorld()).getServer();
        List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
        players.removeIf(p -> p.getUuid().equals(seller.getUuid()));

        if (players.isEmpty()) {
            seller.sendMessage(Text.literal(
                    "\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0442 \u0434\u0440\u0443\u0433\u0438\u0445 \u0438\u0433\u0440\u043E\u043A\u043E\u0432 \u043E\u043D\u043B\u0430\u0439\u043D!"), false);
            return;
        }

        int rows = Math.min(6, (int) Math.ceil((players.size() + 1) / 9.0) + 1);
        ScreenHandlerType<?> type = switch (rows) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };

        SimpleGui gui = new SimpleGui(type, seller, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u043F\u043E\u043A\u0443\u043F\u0430\u0442\u0435\u043B\u044F"));

        for (int i = 0; i < players.size() && i < rows * 9 - 1; i++) {
            ServerPlayerEntity target = players.get(i);
            gui.setSlot(i, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Text.literal("\u00a7b" + target.getName().getString()))
                    .addLoreLine(Text.literal("\u00a77\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u0432\u044B\u0431\u043E\u0440\u0430"))
                    .setCallback((index, clickType, action) -> {
                        gui.close();
                        openPriceInput(seller, ghast, data, state, target);
                    })
                    .build());
        }

        gui.open();
    }

    private static void openPriceInput(ServerPlayerEntity seller, HappyGhastEntity ghast,
                                        GhastVehicleData data, GhastRegistryState state,
                                        ServerPlayerEntity buyer) {
        AnvilInputGui gui = new AnvilInputGui(seller, false);
        gui.setTitle(Text.literal("\u0426\u0435\u043D\u0430 \u0432 \u0438\u0437\u0443\u043C\u0440\u0443\u0434\u0430\u0445 (0 = \u0431\u0435\u0441\u043F\u043B.)"));
        gui.setDefaultInputValue("0");

        gui.setSlot(2, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("\u00a7a\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    String input = gui.getInput();
                    int emeralds;
                    try {
                        emeralds = Math.max(0, Integer.parseInt(input.trim()));
                    } catch (NumberFormatException e) {
                        seller.sendMessage(Text.literal("\u00a7c\u041D\u0435\u0432\u0435\u0440\u043D\u043E\u0435 \u0447\u0438\u0441\u043B\u043E!"), false);
                        return;
                    }
                    gui.close();
                    openDiamondPriceInput(seller, ghast, data, state, buyer, emeralds);
                })
                .build());
        gui.open();
    }

    private static void openDiamondPriceInput(ServerPlayerEntity seller, HappyGhastEntity ghast,
                                               GhastVehicleData data, GhastRegistryState state,
                                               ServerPlayerEntity buyer, int emeraldPrice) {
        AnvilInputGui gui = new AnvilInputGui(seller, false);
        gui.setTitle(Text.literal("\u0426\u0435\u043D\u0430 \u0432 \u0430\u043B\u043C\u0430\u0437\u0430\u0445 (0 = \u0431\u0435\u0441\u043F\u043B.)"));
        gui.setDefaultInputValue("0");

        gui.setSlot(2, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("\u00a7b\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    String input = gui.getInput();
                    int diamonds;
                    try {
                        diamonds = Math.max(0, Integer.parseInt(input.trim()));
                    } catch (NumberFormatException e) {
                        seller.sendMessage(Text.literal("\u00a7c\u041D\u0435\u0432\u0435\u0440\u043D\u043E\u0435 \u0447\u0438\u0441\u043B\u043E!"), false);
                        return;
                    }
                    gui.close();
                    openSaleConfirmation(seller, ghast, data, state, buyer, emeraldPrice, diamonds);
                })
                .build());
        gui.open();
    }

    private static void openSaleConfirmation(ServerPlayerEntity seller, HappyGhastEntity ghast,
                                              GhastVehicleData data, GhastRegistryState state,
                                              ServerPlayerEntity buyer, int priceEmeralds, int priceDiamonds) {
        GaiConfig config = state.getConfig();
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, seller, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u0442\u0435 \u043F\u0440\u043E\u0434\u0430\u0436\u0443"));

        String ghastName = data.getCustomName().isEmpty() ? "\u0413\u0430\u0441\u0442" : data.getCustomName();
        gui.setSlot(4, new GuiElementBuilder(Items.GHAST_TEAR)
                .setName(Text.literal("\u00a7f" + ghastName + " [\u00a7e" + data.getPlateNumber() + "\u00a7f]"))
                .addLoreLine(Text.literal("\u00a77\u041F\u043E\u043A\u0443\u043F\u0430\u0442\u0435\u043B\u044C: \u00a7b" + buyer.getName().getString()))
                .build());

        String priceStr = priceEmeralds == 0 && priceDiamonds == 0
                ? "\u00a7a\u0411\u0435\u0441\u043F\u043B\u0430\u0442\u043D\u043E"
                : formatCost(priceEmeralds, priceDiamonds);
        gui.setSlot(11, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("\u00a7f\u0426\u0435\u043D\u0430: " + priceStr))
                .addLoreLine(Text.literal("\u00a77\u041F\u043E\u043B\u0443\u0447\u0438\u0442\u0435 \u043F\u043E\u0441\u043B\u0435 \u043F\u0440\u043E\u0434\u0430\u0436\u0438"))
                .build());

        String disposalStr = formatCost(config.getDisposalFeeEmeralds(), config.getDisposalFeeDiamonds());
        gui.setSlot(13, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("\u00a7c\u0423\u0442\u0438\u043B\u044C\u0441\u0431\u043E\u0440: " + disposalStr))
                .addLoreLine(Text.literal("\u00a77\u0421\u043F\u0438\u0441\u044B\u0432\u0430\u0435\u0442\u0441\u044F \u0441 \u0432\u0430\u0441 \u043F\u0440\u0438 \u043F\u0440\u043E\u0434\u0430\u0436\u0435"))
                .build());

        gui.setSlot(15, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7a\u0412\u044B\u0441\u0442\u0430\u0432\u0438\u0442\u044C \u043D\u0430 \u043F\u0440\u043E\u0434\u0430\u0436\u0443"))
                .setCallback((index, type, action) -> {
                    if (!hasResources(seller, config.getDisposalFeeEmeralds(), config.getDisposalFeeDiamonds())) {
                        seller.sendMessage(Text.literal(
                                "\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0440\u0435\u0441\u0443\u0440\u0441\u043E\u0432 \u0434\u043B\u044F \u0443\u0442\u0438\u043B\u044C\u0441\u0431\u043E\u0440\u0430!"), false);
                        gui.close();
                        return;
                    }

                    MinecraftServer server = ((net.minecraft.server.world.ServerWorld) seller.getEntityWorld()).getServer();
                    String offerId = GhastSaleManager.createOffer(
                            data.getGhastUuid(), seller.getUuid(), buyer.getUuid(),
                            priceEmeralds, priceDiamonds);

                    GhastSaleManager.sendOfferToBuyer(server,
                            GhastSaleManager.getOffer(offerId), offerId, data,
                            seller.getName().getString());

                    seller.sendMessage(Text.literal(
                            "\u00a7a[\u0413\u0410\u0418] \u041F\u0440\u0435\u0434\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u043E\u0442\u043F\u0440\u0430\u0432\u043B\u0435\u043D\u043E \u0438\u0433\u0440\u043E\u043A\u0443 \u00a7b" + buyer.getName().getString() + "\u00a7a!"), false);
                    gui.close();
                })
                .build());

        gui.setSlot(17, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7c\u041E\u0442\u043C\u0435\u043D\u0430"))
                .setCallback((index, type, action) -> gui.close())
                .build());

        gui.open();
    }

    public static void openBuyerConfirmation(ServerPlayerEntity buyer, String offerId,
                                              GhastSaleManager.SaleOffer offer,
                                              GhastVehicleData data, GhastRegistryState state) {
        MinecraftServer server = ((net.minecraft.server.world.ServerWorld) buyer.getEntityWorld()).getServer();
        GaiConfig config = state.getConfig();

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, buyer, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u041F\u043E\u043A\u0443\u043F\u043A\u0430 \u0433\u0430\u0441\u0442\u0430"));

        String ghastName = data.getCustomName().isEmpty() ? "\u0413\u0430\u0441\u0442" : data.getCustomName();
        ServerPlayerEntity sellerPlayer = server.getPlayerManager().getPlayer(offer.sellerUuid());
        String sellerName = sellerPlayer != null ? sellerPlayer.getName().getString() : "???";

        gui.setSlot(4, new GuiElementBuilder(Items.GHAST_TEAR)
                .setName(Text.literal("\u00a7f" + ghastName + " [\u00a7e" + data.getPlateNumber() + "\u00a7f]"))
                .addLoreLine(Text.literal("\u00a77\u041F\u0440\u043E\u0434\u0430\u0432\u0435\u0446: \u00a7b" + sellerName))
                .addLoreLine(Text.literal("\u00a77\u0421\u0442\u0435\u0439\u0434\u0436: \u00a7f" + SpeedStageManager.getStageName(data.getStage())))
                .addLoreLine(Text.literal("\u00a77\u041F\u0440\u043E\u0431\u0435\u0433: \u00a7f" + String.format("%.1f", data.getMileage()) + " \u0431\u043B."))
                .build());

        String priceStr = offer.priceEmeralds() == 0 && offer.priceDiamonds() == 0
                ? "\u00a7a\u0411\u0435\u0441\u043F\u043B\u0430\u0442\u043D\u043E"
                : formatCost(offer.priceEmeralds(), offer.priceDiamonds());
        gui.setSlot(12, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("\u00a7f\u0426\u0435\u043D\u0430: " + priceStr))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7a\u041A\u0443\u043F\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    gui.close();
                    GhastSaleManager.executeSale(server, offerId, state);
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7c\u041E\u0442\u043A\u043B\u043E\u043D\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    GhastSaleManager.removeOffer(offerId);
                    buyer.sendMessage(Text.literal(
                            "\u00a7e[\u0413\u0410\u0418] \u041F\u0440\u0435\u0434\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u043E\u0442\u043A\u043B\u043E\u043D\u0435\u043D\u043E."), false);
                    gui.close();
                })
                .build());

        gui.open();
    }

    static String formatCost(int emeralds, int diamonds) {
        StringBuilder sb = new StringBuilder();
        if (emeralds > 0) sb.append("\u00a7a").append(emeralds).append(" \u0438\u0437\u0443\u043C.");
        if (emeralds > 0 && diamonds > 0) sb.append(" \u00a77+ ");
        if (diamonds > 0) sb.append("\u00a7b").append(diamonds).append(" \u0430\u043B\u043C.");
        return sb.toString();
    }

    private static boolean hasResources(ServerPlayerEntity player, int emeralds, int diamonds) {
        int foundEmeralds = 0, foundDiamonds = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            var stack = player.getInventory().getStack(i);
            if (stack.isOf(Items.EMERALD)) foundEmeralds += stack.getCount();
            if (stack.isOf(Items.DIAMOND)) foundDiamonds += stack.getCount();
        }
        return foundEmeralds >= emeralds && foundDiamonds >= diamonds;
    }
}
