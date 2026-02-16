package com.happyghast.gai.gui;

import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.data.PlateGenerator;
import com.happyghast.gai.plate.PlateDisplayManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;

public class RegistrationGui {

    public static void openNameInput(ServerPlayerEntity player, HappyGhastEntity ghast,
                                      GhastVehicleData data, GhastRegistryState state) {
        AnvilInputGui gui = new AnvilInputGui(player, false);
        gui.setTitle(Text.literal("\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0438\u043C\u044F \u0433\u0430\u0441\u0442\u0430"));
        gui.setDefaultInputValue("\u0413\u0430\u0441\u0442\u0438\u043A");

        gui.setSlot(2, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Text.literal("\u00a7a\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    String name = gui.getInput();
                    if (name == null || name.isBlank()) name = "\u0413\u0430\u0441\u0442\u0438\u043A";
                    gui.close();
                    openColorSelection(player, ghast, data, state, name);
                })
                .build());
        gui.open();
    }

    private static void openColorSelection(ServerPlayerEntity player, HappyGhastEntity ghast,
                                            GhastVehicleData data, GhastRegistryState state, String customName) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u0446\u0432\u0435\u0442 \u0441\u0431\u0440\u0443\u0438"));

        for (int i = 0; i < 16; i++) {
            final int colorIndex = i;
            gui.setSlot(i, new GuiElementBuilder(HarnessManager.getWoolItem(i))
                    .setName(Text.literal("\u00a7f" + HarnessManager.getColorName(i)))
                    .setCallback((index, type, action) -> {
                        gui.close();
                        openPlateSelection(player, ghast, data, state, customName, colorIndex);
                    })
                    .build());
        }
        gui.open();
    }

    private static void openPlateSelection(ServerPlayerEntity player, HappyGhastEntity ghast,
                                            GhastVehicleData data, GhastRegistryState state,
                                            String customName, int harnessColor) {
        List<String> plates = PlateGenerator.generateChoices(state.getUsedPlateNumbers(), 7);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u043D\u043E\u043C\u0435\u0440"));

        for (int i = 0; i < plates.size() && i < 7; i++) {
            final String plate = plates.get(i);
            gui.setSlot(i + 1, new GuiElementBuilder(Items.NAME_TAG)
                    .setName(Text.literal("\u00a7e" + plate))
                    .addLoreLine(Text.literal("\u00a77\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u0432\u044B\u0431\u043E\u0440\u0430"))
                    .setCallback((index, type, action) -> {
                        gui.close();
                        openPaymentConfirmation(player, ghast, data, state, customName, harnessColor, plate);
                    })
                    .build());
        }

        gui.setSlot(8, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7c\u041E\u0442\u043C\u0435\u043D\u0430"))
                .setCallback((index, type, action) -> gui.close())
                .build());

        gui.open();
    }

    private static void openPaymentConfirmation(ServerPlayerEntity player, HappyGhastEntity ghast,
                                                  GhastVehicleData data, GhastRegistryState state,
                                                  String customName, int harnessColor, String chosenPlate) {
        GaiConfig config = state.getConfig();
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u041E\u043F\u043B\u0430\u0442\u0430 \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438"));

        gui.setSlot(4, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Text.literal("\u00a7f\u041D\u043E\u043C\u0435\u0440: \u00a7e" + chosenPlate))
                .build());

        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("\u00a7a\u0418\u0437\u0443\u043C\u0440\u0443\u0434\u044B: " + config.getRegistrationCostEmeralds()))
                .setCount(Math.max(1, Math.min(64, config.getRegistrationCostEmeralds())))
                .build());
        gui.setSlot(12, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("\u00a7b\u0410\u043B\u043C\u0430\u0437\u044B: " + config.getRegistrationCostDiamonds()))
                .setCount(Math.max(1, Math.min(64, config.getRegistrationCostDiamonds())))
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7a\u041E\u043F\u043B\u0430\u0442\u0438\u0442\u044C \u0438 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    if (!hasResources(player, config.getRegistrationCostEmeralds(), config.getRegistrationCostDiamonds())) {
                        player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0440\u0435\u0441\u0443\u0440\u0441\u043E\u0432!"), false);
                        gui.close();
                        return;
                    }
                    if (state.getUsedPlateNumbers().contains(chosenPlate)) {
                        player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u041D\u043E\u043C\u0435\u0440 \u0443\u0436\u0435 \u0437\u0430\u043D\u044F\u0442! \u041F\u043E\u043F\u0440\u043E\u0431\u0443\u0439\u0442\u0435 \u0441\u043D\u043E\u0432\u0430."), false);
                        gui.close();
                        return;
                    }
                    consumeResources(player, config.getRegistrationCostEmeralds(), config.getRegistrationCostDiamonds());
                    config.recordTransaction("registration", config.getRegistrationCostEmeralds(), config.getRegistrationCostDiamonds());

                    state.registerGhast(ghast.getUuid(), player.getUuid(), customName, harnessColor, chosenPlate);
                    HarnessManager.equipHarness(ghast, harnessColor);
                    SpeedStageManager.applyStageSpeed(ghast, 1);

                    ServerWorld world = (ServerWorld) ghast.getEntityWorld();
                    PlateDisplayManager.createPlates(world, ghast, data);
                    state.markDirty();

                    player.sendMessage(Text.literal(
                            "\u00a7a[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D! \u041D\u043E\u043C\u0435\u0440: \u00a7e" + chosenPlate), false);
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
        int foundEmeralds = countItem(player, Items.EMERALD);
        int foundDiamonds = countItem(player, Items.DIAMOND);
        return foundEmeralds >= emeralds && foundDiamonds >= diamonds;
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
