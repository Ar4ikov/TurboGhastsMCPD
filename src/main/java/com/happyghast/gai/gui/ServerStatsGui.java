package com.happyghast.gai.gui;

import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ServerStatsGui {

    public static void open(ServerPlayerEntity admin, GhastRegistryState state) {
        GaiConfig config = state.getConfig();
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X4, admin, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0421\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043A\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0430"));

        int totalE = config.getTotalEmeraldsSpent();
        int totalD = config.getTotalDiamondsSpent();
        int totalTx = config.getTotalTransactions();

        gui.setSlot(4, new GuiElementBuilder(Items.GOLDEN_APPLE)
                .setName(Text.literal("\u00a76\u00a7l\u041E\u0431\u0449\u0438\u0435 \u0438\u0442\u043E\u0433\u0438"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("\u00a7a\u0418\u0437\u0443\u043C\u0440\u0443\u0434\u043E\u0432 \u043F\u043E\u0442\u0440\u0430\u0447\u0435\u043D\u043E: \u00a7f" + totalE))
                .addLoreLine(Text.literal("\u00a7b\u0410\u043B\u043C\u0430\u0437\u043E\u0432 \u043F\u043E\u0442\u0440\u0430\u0447\u0435\u043D\u043E: \u00a7f" + totalD))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("\u00a77\u0412\u0441\u0435\u0433\u043E \u0442\u0440\u0430\u043D\u0437\u0430\u043A\u0446\u0438\u0439: \u00a7f" + totalTx))
                .glow()
                .build());

        gui.setSlot(10, buildCategoryItem(
                Items.WRITABLE_BOOK, "\u00a7a\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F",
                config.getStatRegEmeralds(), config.getStatRegDiamonds(), config.getStatRegCount()));

        gui.setSlot(12, buildCategoryItem(
                Items.NAME_TAG, "\u00a7e\u041F\u0435\u0440\u0435\u0438\u043C\u0435\u043D\u043E\u0432\u0430\u043D\u0438\u0435",
                config.getStatRenameEmeralds(), config.getStatRenameDiamonds(), config.getStatRenameCount()));

        gui.setSlot(14, buildCategoryItem(
                Items.FEATHER, "\u00a7d\u0421\u0442\u0435\u0439\u0434\u0436\u0438",
                config.getStatStageEmeralds(), config.getStatStageDiamonds(), config.getStatStageCount()));

        gui.setSlot(16, buildCategoryItem(
                Items.BLAZE_POWDER, "\u00a7c\u0427\u0430\u0441\u0442\u0438\u0446\u044B",
                config.getStatParticleEmeralds(), config.getStatParticleDiamonds(), config.getStatParticleCount()));

        gui.setSlot(22, buildCategoryItem(
                Items.IRON_BARS, "\u00a76\u0412\u044B\u043A\u0443\u043F \u0441\u043E \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0438",
                config.getStatReleaseEmeralds(), config.getStatReleaseDiamonds(), config.getStatReleaseCount()));

        int regGhasts = state.getRegisteredGhasts().size();
        int impounded = state.getImpoundedGhasts().size();
        int total = state.getAllGhasts().size();

        gui.setSlot(31, new GuiElementBuilder(Items.GHAST_TEAR)
                .setName(Text.literal("\u00a7f\u0413\u0430\u0441\u0442\u044B \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("\u00a77\u0412\u0441\u0435\u0433\u043E \u043E\u0442\u0441\u043B\u0435\u0436\u0438\u0432\u0430\u0435\u0442\u0441\u044F: \u00a7f" + total))
                .addLoreLine(Text.literal("\u00a7a\u0417\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u043E: \u00a7f" + regGhasts))
                .addLoreLine(Text.literal("\u00a7c\u041D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0435: \u00a7f" + impounded))
                .build());

        double totalMileage = 0;
        for (var d : state.getRegisteredGhasts()) {
            totalMileage += d.getMileage();
        }
        gui.setSlot(33, new GuiElementBuilder(Items.CLOCK)
                .setName(Text.literal("\u00a7f\u041E\u0431\u0449\u0438\u0439 \u043F\u0440\u043E\u0431\u0435\u0433"))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("\u00a7a" + String.format("%.1f", totalMileage) + " \u0431\u043B\u043E\u043A\u043E\u0432"))
                .build());

        gui.open();
    }

    private static GuiElementBuilder buildCategoryItem(net.minecraft.item.Item icon, String name,
                                                        int emeralds, int diamonds, int count) {
        return new GuiElementBuilder(icon)
                .setName(Text.literal(name))
                .addLoreLine(Text.literal(""))
                .addLoreLine(Text.literal("\u00a7a\u0418\u0437\u0443\u043C\u0440\u0443\u0434\u043E\u0432: \u00a7f" + emeralds))
                .addLoreLine(Text.literal("\u00a7b\u0410\u043B\u043C\u0430\u0437\u043E\u0432: \u00a7f" + diamonds))
                .addLoreLine(Text.literal("\u00a77\u0422\u0440\u0430\u043D\u0437\u0430\u043A\u0446\u0438\u0439: \u00a7f" + count));
    }
}
