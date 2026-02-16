package com.happyghast.gai.gui;

import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

public class PlayerGhastsGui {

    private static final int PAGE_SIZE = 45;

    public static void open(ServerPlayerEntity admin, String targetName, UUID targetUuid,
                            GhastRegistryState state, int page) {
        List<GhastVehicleData> ghasts = state.getGhastsByOwner(targetUuid);

        int totalPages = Math.max(1, (int) Math.ceil(ghasts.size() / (double) PAGE_SIZE));
        int actualPage = Math.max(0, Math.min(page, totalPages - 1));
        int startIndex = actualPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, ghasts.size());

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, admin, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0413\u0430\u0441\u0442\u044B " + targetName
                + " \u00a78(" + (actualPage + 1) + "/" + totalPages + ")"));

        if (ghasts.isEmpty()) {
            gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("\u00a7c\u0423 \u0438\u0433\u0440\u043E\u043A\u0430 \u043D\u0435\u0442 \u0433\u0430\u0441\u0442\u043E\u0432"))
                    .build());
        } else {
            for (int i = startIndex; i < endIndex; i++) {
                GhastVehicleData data = ghasts.get(i);
                int slot = i - startIndex;

                String name = data.getCustomName().isEmpty() ? "---" : data.getCustomName();
                String plate = data.getPlateNumber().isEmpty() ? "---" : data.getPlateNumber();
                boolean impounded = data.isImpounded();
                boolean registered = data.isRegistered();

                var icon = impounded ? Items.BARRIER : registered ? Items.GHAST_TEAR : Items.PAPER;
                String statusStr = impounded ? "\u00a7c\u0428\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0430"
                        : registered ? "\u00a7a\u0410\u043A\u0442\u0438\u0432\u0435\u043D" : "\u00a7e\u041D\u0435 \u0437\u0430\u0440\u0435\u0433.";

                int stage = data.getStage();
                String stageStr = stage > 0 ? SpeedStageManager.getStageName(stage) : "\u041D\u0435\u0442";

                GuiElementBuilder builder = new GuiElementBuilder(icon)
                        .setName(Text.literal("\u00a7f" + name + " [\u00a7e" + plate + "\u00a7f]"))
                        .addLoreLine(Text.literal("\u00a77\u0421\u0442\u0430\u0442\u0443\u0441: " + statusStr))
                        .addLoreLine(Text.literal("\u00a77\u0421\u0442\u0435\u0439\u0434\u0436: \u00a7f" + stageStr))
                        .addLoreLine(Text.literal("\u00a77\u0414\u043E\u043C: " + data.getHomePos().getX() + ", " + data.getHomePos().getY() + ", " + data.getHomePos().getZ()))
                        .addLoreLine(Text.literal(""))
                        .addLoreLine(Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u043F\u043E\u0434\u0440\u043E\u0431\u043D\u043E\u0441\u0442\u0435\u0439"));

                final int currentPage = actualPage;
                builder.setCallback((idx, type, action) -> {
                    gui.close();
                    GhastInfoGui.openAdminView(admin, data, state,
                            () -> open(admin, targetName, targetUuid, state, currentPage));
                });

                gui.setSlot(slot, builder.build());
            }
        }

        // Navigation row (slots 45-53)
        // Previous page
        if (actualPage > 0) {
            final int prevPage = actualPage - 1;
            gui.setSlot(45, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("\u00a7f\u2190 \u041D\u0430\u0437\u0430\u0434"))
                    .setCallback((index, type, action) -> {
                        gui.close();
                        open(admin, targetName, targetUuid, state, prevPage);
                    })
                    .build());
        }

        // Page indicator
        gui.setSlot(49, new GuiElementBuilder(Items.BOOK)
                .setName(Text.literal("\u00a7f\u0421\u0442\u0440\u0430\u043D\u0438\u0446\u0430 " + (actualPage + 1) + " / " + totalPages))
                .addLoreLine(Text.literal("\u00a77\u0412\u0441\u0435\u0433\u043E \u0433\u0430\u0441\u0442\u043E\u0432: " + ghasts.size()))
                .build());

        // Next page
        if (actualPage < totalPages - 1) {
            final int nextPage = actualPage + 1;
            gui.setSlot(53, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("\u00a7f\u0412\u043F\u0435\u0440\u0451\u0434 \u2192"))
                    .setCallback((index, type, action) -> {
                        gui.close();
                        open(admin, targetName, targetUuid, state, nextPage);
                    })
                    .build());
        }

        gui.open();
    }
}
