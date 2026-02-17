package com.happyghast.gai.gui;

import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AdminConfigGui {

    public static void open(ServerPlayerEntity player, GhastRegistryState state) {
        GaiConfig config = state.getConfig();
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u041D\u0430\u0441\u0442\u0440\u043E\u0439\u043A\u0438"));

        gui.setSlot(10, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("\u00a7a\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F: \u0438\u0437\u0443\u043C\u0440\u0443\u0434\u044B = " + config.getRegistrationCostEmeralds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0418\u0437\u0443\u043C\u0440\u0443\u0434\u044B \u0437\u0430 \u0440\u0435\u0433.",
                            String.valueOf(config.getRegistrationCostEmeralds()), value -> {
                                config.setRegistrationCostEmeralds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(11, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("\u00a7b\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F: \u0430\u043B\u043C\u0430\u0437\u044B = " + config.getRegistrationCostDiamonds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0410\u043B\u043C\u0430\u0437\u044B \u0437\u0430 \u0440\u0435\u0433.",
                            String.valueOf(config.getRegistrationCostDiamonds()), value -> {
                                config.setRegistrationCostDiamonds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(13, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("\u00a7e\u0412\u044B\u043A\u0443\u043F: \u0438\u0437\u0443\u043C\u0440\u0443\u0434\u044B = " + config.getImpoundReleaseCostEmeralds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0418\u0437\u0443\u043C\u0440\u0443\u0434\u044B \u0437\u0430 \u0432\u044B\u043A\u0443\u043F",
                            String.valueOf(config.getImpoundReleaseCostEmeralds()), value -> {
                                config.setImpoundReleaseCostEmeralds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(14, new GuiElementBuilder(Items.GOLD_INGOT)
                .setName(Text.literal("\u00a7e\u0412\u044B\u043A\u0443\u043F: \u0430\u043B\u043C\u0430\u0437\u044B = " + config.getImpoundReleaseCostDiamonds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0410\u043B\u043C\u0430\u0437\u044B \u0437\u0430 \u0432\u044B\u043A\u0443\u043F",
                            String.valueOf(config.getImpoundReleaseCostDiamonds()), value -> {
                                config.setImpoundReleaseCostDiamonds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(16, new GuiElementBuilder(Items.CLOCK)
                .setName(Text.literal("\u00a7f\u0421\u0440\u043E\u043A \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u0438: " + (config.getRegistrationPeriodMs() / 3600000) + " \u0447"))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0421\u0440\u043E\u043A \u0440\u0435\u0433. (\u0447\u0430\u0441\u044B)",
                            String.valueOf(config.getRegistrationPeriodMs() / 3600000), value -> {
                                config.setRegistrationPeriodMs(value * 3600000L);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(22, new GuiElementBuilder(Items.CLOCK)
                .setName(Text.literal("\u00a7f\u042E\u0440\u044C\u0435\u0432 \u0434\u0435\u043D\u044C: " + (config.getGracePeriodMs() / 3600000) + " \u0447"))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u042E\u0440\u044C\u0435\u0432 \u0434\u0435\u043D\u044C (\u0447\u0430\u0441\u044B)",
                            String.valueOf(config.getGracePeriodMs() / 3600000), value -> {
                                config.setGracePeriodMs(value * 3600000L);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(28, new GuiElementBuilder(Items.EMERALD)
                .setName(Text.literal("\u00a7a\u041F\u0435\u0440\u0435\u0438\u043C\u0435\u043D\u043E\u0432\u0430\u043D\u0438\u0435: \u0438\u0437\u0443\u043C\u0440\u0443\u0434\u044B = " + config.getRenameCostEmeralds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0418\u0437\u0443\u043C\u0440\u0443\u0434\u044B \u0437\u0430 \u0440\u0435\u043D\u0435\u0439\u043C",
                            String.valueOf(config.getRenameCostEmeralds()), value -> {
                                config.setRenameCostEmeralds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(29, new GuiElementBuilder(Items.DIAMOND)
                .setName(Text.literal("\u00a7b\u041F\u0435\u0440\u0435\u0438\u043C\u0435\u043D\u043E\u0432\u0430\u043D\u0438\u0435: \u0430\u043B\u043C\u0430\u0437\u044B = " + config.getRenameCostDiamonds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0410\u043B\u043C\u0430\u0437\u044B \u0437\u0430 \u0440\u0435\u043D\u0435\u0439\u043C",
                            String.valueOf(config.getRenameCostDiamonds()), value -> {
                                config.setRenameCostDiamonds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(37, new GuiElementBuilder(Items.IRON_HORSE_ARMOR)
                .setName(Text.literal("\u00a7e\u0421\u0442\u0435\u0439\u0434\u0436 2: \u0438\u0437\u0443\u043C\u0440\u0443\u0434\u044B = " + config.getStage2CostEmeralds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0418\u0437\u0443\u043C\u0440. \u0437\u0430 \u0441\u0442\u0435\u0439\u0434\u0436 2",
                            String.valueOf(config.getStage2CostEmeralds()), value -> {
                                config.setStage2CostEmeralds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(38, new GuiElementBuilder(Items.IRON_HORSE_ARMOR)
                .setName(Text.literal("\u00a7e\u0421\u0442\u0435\u0439\u0434\u0436 2: \u0430\u043B\u043C\u0430\u0437\u044B = " + config.getStage2CostDiamonds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0410\u043B\u043C\u0430\u0437\u044B \u0437\u0430 \u0441\u0442\u0435\u0439\u0434\u0436 2",
                            String.valueOf(config.getStage2CostDiamonds()), value -> {
                                config.setStage2CostDiamonds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(40, new GuiElementBuilder(Items.DIAMOND_HORSE_ARMOR)
                .setName(Text.literal("\u00a7b\u0421\u0442\u0435\u0439\u0434\u0436 3: \u0438\u0437\u0443\u043C\u0440\u0443\u0434\u044B = " + config.getStage3CostEmeralds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0418\u0437\u0443\u043C\u0440. \u0437\u0430 \u0441\u0442\u0435\u0439\u0434\u0436 3",
                            String.valueOf(config.getStage3CostEmeralds()), value -> {
                                config.setStage3CostEmeralds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(41, new GuiElementBuilder(Items.DIAMOND_HORSE_ARMOR)
                .setName(Text.literal("\u00a7b\u0421\u0442\u0435\u0439\u0434\u0436 3: \u0430\u043B\u043C\u0430\u0437\u044B = " + config.getStage3CostDiamonds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0410\u043B\u043C\u0430\u0437\u044B \u0437\u0430 \u0441\u0442\u0435\u0439\u0434\u0436 3",
                            String.valueOf(config.getStage3CostDiamonds()), value -> {
                                config.setStage3CostDiamonds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(46, new GuiElementBuilder(Items.BLAZE_POWDER)
                .setName(Text.literal("\u00a7d\u0427\u0430\u0441\u0442\u0438\u0446\u044B: \u0438\u0437\u0443\u043C\u0440\u0443\u0434\u044B = " + config.getParticleCostEmeralds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0418\u0437\u0443\u043C\u0440. \u0437\u0430 \u0447\u0430\u0441\u0442\u0438\u0446\u044B",
                            String.valueOf(config.getParticleCostEmeralds()), value -> {
                                config.setParticleCostEmeralds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(47, new GuiElementBuilder(Items.BLAZE_POWDER)
                .setName(Text.literal("\u00a7d\u0427\u0430\u0441\u0442\u0438\u0446\u044B: \u0430\u043B\u043C\u0430\u0437\u044B = " + config.getParticleCostDiamonds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0410\u043B\u043C\u0430\u0437\u044B \u0437\u0430 \u0447\u0430\u0441\u0442\u0438\u0446\u044B",
                            String.valueOf(config.getParticleCostDiamonds()), value -> {
                                config.setParticleCostDiamonds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(49, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("\u00a7d\u0421\u043C\u0435\u043D\u0430 \u043D\u043E\u043C\u0435\u0440\u0430: \u0437\u0432\u0451\u0437\u0434\u044B = " + config.getReplateCostNetherStars()))
                .addLoreLine(Text.literal("\u00a77\u041C\u0438\u043D\u0438\u043C\u0443\u043C: 1"))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0417\u0432\u0451\u0437\u0434\u044B \u0437\u0430 \u0440\u0435\u043F\u043B\u0435\u0439\u0442 (\u043C\u0438\u043D. 1)",
                            String.valueOf(config.getReplateCostNetherStars()), value -> {
                                config.setReplateCostNetherStars(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(51, new GuiElementBuilder(Items.ANVIL)
                .setName(Text.literal("\u00a76\u0423\u0442\u0438\u043B\u044C\u0441\u0431\u043E\u0440: \u0438\u0437\u0443\u043C\u0440\u0443\u0434\u044B = " + config.getDisposalFeeEmeralds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0418\u0437\u0443\u043C\u0440. \u0443\u0442\u0438\u043B\u044C\u0441\u0431\u043E\u0440",
                            String.valueOf(config.getDisposalFeeEmeralds()), value -> {
                                config.setDisposalFeeEmeralds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.setSlot(52, new GuiElementBuilder(Items.ANVIL)
                .setName(Text.literal("\u00a76\u0423\u0442\u0438\u043B\u044C\u0441\u0431\u043E\u0440: \u0430\u043B\u043C\u0430\u0437\u044B = " + config.getDisposalFeeDiamonds()))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openNumberInput(player, state, "\u0410\u043B\u043C\u0430\u0437\u044B \u0443\u0442\u0438\u043B\u044C\u0441\u0431\u043E\u0440",
                            String.valueOf(config.getDisposalFeeDiamonds()), value -> {
                                config.setDisposalFeeDiamonds(value);
                                state.saveConfig();
                                open(player, state);
                            });
                })
                .build());

        gui.open();
    }

    private static void openNumberInput(ServerPlayerEntity player, GhastRegistryState state,
                                         String title, String defaultValue,
                                         java.util.function.IntConsumer onConfirm) {
        AnvilInputGui gui = new AnvilInputGui(player, false);
        gui.setTitle(Text.literal(title));
        gui.setDefaultInputValue(defaultValue);

        gui.setSlot(2, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7a\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    String input = gui.getInput();
                    try {
                        int value = Integer.parseInt(input.trim());
                        if (value < 0) value = 0;
                        gui.close();
                        onConfirm.accept(value);
                    } catch (NumberFormatException e) {
                        player.sendMessage(Text.literal("\u00a7c\u041D\u0435\u0432\u0435\u0440\u043D\u043E\u0435 \u0447\u0438\u0441\u043B\u043E!"), false);
                    }
                })
                .build());
        gui.open();
    }
}
