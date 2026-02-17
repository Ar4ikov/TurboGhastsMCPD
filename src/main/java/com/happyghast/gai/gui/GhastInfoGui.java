package com.happyghast.gai.gui;

import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.data.PlateGenerator;
import com.happyghast.gai.handler.TickHandler;
import com.happyghast.gai.plate.PlateDisplayManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GhastInfoGui {

    private static final java.util.Set<UUID> NAVIGATING = java.util.Collections.newSetFromMap(
            new java.util.concurrent.ConcurrentHashMap<>());

    @SuppressWarnings("unchecked")
    private static SimpleGui createLockedGui(ScreenHandlerType<?> type, ServerPlayerEntity player, HappyGhastEntity ghast) {
        return new SimpleGui(type, player, false) {
            @Override
            public void onClose() {
                if (!NAVIGATING.remove(player.getUuid())) {
                    MinecraftServer srv = ((ServerWorld) player.getEntityWorld()).getServer();
                    GhastMenuLock.unlock(ghast.getUuid(), player.getUuid(), srv);
                }
            }
        };
    }

    private static AnvilInputGui createLockedAnvilGui(ServerPlayerEntity player, HappyGhastEntity ghast) {
        return new AnvilInputGui(player, false) {
            @Override
            public void onClose() {
                if (!NAVIGATING.remove(player.getUuid())) {
                    MinecraftServer srv = ((ServerWorld) player.getEntityWorld()).getServer();
                    GhastMenuLock.unlock(ghast.getUuid(), player.getUuid(), srv);
                }
            }
        };
    }

    private static void navigateClose(SimpleGui gui, ServerPlayerEntity player) {
        NAVIGATING.add(player.getUuid());
        gui.close();
    }

    private static void navigateCloseAnvil(AnvilInputGui gui, ServerPlayerEntity player) {
        NAVIGATING.add(player.getUuid());
        gui.close();
    }

    /**
     * Read-only view for non-owners/non-admins clicking on a registered ghast.
     */
    public static void openReadOnly(ServerPlayerEntity player, HappyGhastEntity ghast,
                                     GhastVehicleData data, GhastRegistryState state) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F \u043E \u0433\u0430\u0441\u0442\u0435"));

        GaiConfig config = state.getConfig();
        fillInfoSlots(gui, player, data, config);

        int currentStage = data.getStage();
        String stageColor = SpeedStageManager.getStageColor(currentStage);
        gui.setSlot(29, new GuiElementBuilder(SpeedStageManager.getStageIcon(currentStage))
                .setName(Text.literal("\u00a7f\u0421\u0442\u0435\u0439\u0434\u0436: " + stageColor + SpeedStageManager.getStageName(currentStage)))
                .addLoreLine(Text.literal("\u00a77\u0421\u043A\u043E\u0440\u043E\u0441\u0442\u044C: \u00a7f" + SpeedStageManager.getSpeedForStage(currentStage)))
                .build());

        ParticleTrailManager.ParticlePreset preset = ParticleTrailManager.getPreset(data.getParticleId());
        String pName = preset.name();
        if (preset.isDust()) pName += " (" + ParticleTrailManager.getDustColorName(data.getParticleColor()) + ")";
        gui.setSlot(33, new GuiElementBuilder(preset.icon())
                .setName(Text.literal("\u00a7f\u0427\u0430\u0441\u0442\u0438\u0446\u044B: \u00a7d" + pName))
                .build());

        if (data.isImpounded()) {
            gui.setSlot(40, new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("\u00a7c\u0413\u0430\u0441\u0442 \u043D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0435"))
                    .addLoreLine(Text.literal("\u00a77\u0420\u0435\u0434\u0430\u043A\u0442\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u0435 \u0437\u0430\u0431\u043B\u043E\u043A\u0438\u0440\u043E\u0432\u0430\u043D\u043E"))
                    .build());
        }

        gui.open();
    }

    public static void open(ServerPlayerEntity player, HappyGhastEntity ghast,
                            GhastVehicleData data, GhastRegistryState state) {
        SimpleGui gui = createLockedGui(ScreenHandlerType.GENERIC_9X5, player, ghast);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F \u043E \u0433\u0430\u0441\u0442\u0435"));

        boolean isOwner = player.getUuid().equals(data.getOwnerUuid());
        boolean isAdmin = player.getCommandSource().getPermissions()
                .hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS));
        GaiConfig config = state.getConfig();

        fillInfoSlots(gui, player, data, config);

        boolean canEdit = !data.isImpounded();
        boolean canModify = (isOwner || isAdmin) && canEdit;

        // Rename
        if (canModify) {
            String costStr = isAdmin ? "\u00a7a\u0411\u0435\u0441\u043F\u043B\u0430\u0442\u043D\u043E (\u0430\u0434\u043C\u0438\u043D)"
                    : formatCost(config.getRenameCostEmeralds(), config.getRenameCostDiamonds());
            gui.setSlot(10, new GuiElementBuilder(Items.GHAST_TEAR)
                    .setName(Text.literal("\u00a7f\u0418\u043C\u044F: \u00a7a" + data.getCustomName()))
                    .addLoreLine(Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u0441\u043C\u0435\u043D\u044B \u0438\u043C\u0435\u043D\u0438"))
                    .addLoreLine(Text.literal("\u00a77\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: " + costStr))
                    .setCallback((index, type, action) -> {
                        navigateClose(gui, player);
                        openRenameInput(player, ghast, data, state);
                    })
                    .build());
        }

        // Harness color
        if (canModify) {
            gui.setSlot(16, new GuiElementBuilder(HarnessManager.getWoolItem(data.getHarnessColor()))
                    .setName(Text.literal("\u00a7f\u0421\u0431\u0440\u0443\u044F: \u00a7e" + HarnessManager.getColorName(data.getHarnessColor())))
                    .addLoreLine(Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u0441\u043C\u0435\u043D\u044B \u0446\u0432\u0435\u0442\u0430"))
                    .addLoreLine(Text.literal("\u00a7a\u0411\u0435\u0441\u043F\u043B\u0430\u0442\u043D\u043E"))
                    .setCallback((index, type, action) -> {
                        navigateClose(gui, player);
                        openColorSelection(player, ghast, data, state);
                    })
                    .build());
        }

        // Stage (clickable to open stage selection)
        int currentStage = data.getStage();
        int maxStage = data.getMaxStage();
        String stageColor = SpeedStageManager.getStageColor(currentStage);
        GuiElementBuilder stageBuilder = new GuiElementBuilder(SpeedStageManager.getStageIcon(currentStage))
                .setName(Text.literal("\u00a7f\u0421\u0442\u0435\u0439\u0434\u0436: " + stageColor + SpeedStageManager.getStageName(currentStage)))
                .addLoreLine(Text.literal("\u00a77\u0421\u043A\u043E\u0440\u043E\u0441\u0442\u044C: \u00a7f" + SpeedStageManager.getSpeedForStage(currentStage)));

        if (canModify && maxStage >= 1) {
            if (maxStage >= 3) {
                stageBuilder.addLoreLine(Text.literal("\u00a7a\u0412\u0441\u0435 \u0441\u0442\u0435\u0439\u0434\u0436\u0438 \u043E\u0442\u043A\u0440\u044B\u0442\u044B!"));
                stageBuilder.glow();
            }
            stageBuilder.addLoreLine(Text.literal(""));
            stageBuilder.addLoreLine(Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u0432\u044B\u0431\u043E\u0440\u0430 \u0441\u0442\u0435\u0439\u0434\u0436\u0430"));
            stageBuilder.setCallback((index, type, action) -> {
                navigateClose(gui, player);
                openStageSelection(player, ghast, data, state);
            });
        }
        gui.setSlot(29, stageBuilder.build());

        // Particle trail
        if (canModify) {
            ParticleTrailManager.ParticlePreset currentPreset = ParticleTrailManager.getPreset(data.getParticleId());
            String particleName = currentPreset.name();
            if (currentPreset.isDust()) {
                particleName += " (" + ParticleTrailManager.getDustColorName(data.getParticleColor()) + ")";
            }
            String particleCostStr = isAdmin ? "\u00a7a\u0411\u0435\u0441\u043F\u043B\u0430\u0442\u043D\u043E (\u0430\u0434\u043C\u0438\u043D)"
                    : formatCost(config.getParticleCostEmeralds(), config.getParticleCostDiamonds());
            gui.setSlot(33, new GuiElementBuilder(currentPreset.icon())
                    .setName(Text.literal("\u00a7f\u0427\u0430\u0441\u0442\u0438\u0446\u044B: \u00a7d" + particleName))
                    .addLoreLine(Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u0441\u043C\u0435\u043D\u044B"))
                    .addLoreLine(Text.literal("\u00a77\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: " + particleCostStr))
                    .setCallback((index, type, action) -> {
                        navigateClose(gui, player);
                        openParticleSelection(player, ghast, data, state);
                    })
                    .build());
        }

        // Replate (owner only, costs nether stars)
        if (isOwner && canEdit) {
            int starCost = config.getReplateCostNetherStars();
            gui.setSlot(31, new GuiElementBuilder(Items.NETHER_STAR)
                    .setName(Text.literal("\u00a7f\u0421\u043C\u0435\u043D\u0438\u0442\u044C \u043D\u043E\u043C\u0435\u0440"))
                    .addLoreLine(Text.literal("\u00a77\u0422\u0435\u043A\u0443\u0449\u0438\u0439: \u00a7e" + data.getPlateNumber()))
                    .addLoreLine(Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u0441\u043C\u0435\u043D\u044B"))
                    .addLoreLine(Text.literal("\u00a77\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: \u00a7d" + starCost + " \u0437\u0432\u0435\u0437\u0434 \u041D\u0435\u0437\u0435\u0440\u0430"))
                    .setCallback((index, type, action) -> {
                        navigateClose(gui, player);
                        openReplateInput(player, ghast, data, state);
                    })
                    .build());
        }

        // Sell (owner only, not impounded)
        if (isOwner && canEdit) {
            String disposalStr = formatCost(config.getDisposalFeeEmeralds(), config.getDisposalFeeDiamonds());
            gui.setSlot(35, new GuiElementBuilder(Items.EMERALD_BLOCK)
                    .setName(Text.literal("\u00a7a\u041F\u0440\u043E\u0434\u0430\u0442\u044C \u0433\u0430\u0441\u0442\u0430"))
                    .addLoreLine(Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u043F\u0440\u043E\u0434\u0430\u0436\u0438"))
                    .addLoreLine(Text.literal("\u00a77\u0423\u0442\u0438\u043B\u044C\u0441\u0431\u043E\u0440: " + disposalStr))
                    .setCallback((index, type, action) -> {
                        gui.close();
                        GhastSaleGui.openPlayerSelection(player, ghast, data, state);
                    })
                    .build());
        }

        // Admin row (row 4, slots 36-44)
        if (isAdmin) {
            fillAdminButtons(gui, player, data, state, null);
        }

        gui.open();
    }

    /**
     * Admin-only view for when the ghast entity is not available (from command or player list GUI).
     * Shows read-only info + impound/release buttons.
     */
    public static void openAdminView(ServerPlayerEntity admin, GhastVehicleData data,
                                      GhastRegistryState state, @Nullable Runnable onBack) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, admin, false);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0410\u0434\u043C\u0438\u043D-\u043F\u0440\u043E\u0441\u043C\u043E\u0442\u0440"));

        GaiConfig config = state.getConfig();
        fillInfoSlots(gui, admin, data, config);

        // Stage (read-only)
        int adminCurrentStage = data.getStage();
        int adminMaxStage = data.getMaxStage();
        String adminStageColor = SpeedStageManager.getStageColor(adminCurrentStage);
        GuiElementBuilder adminStageBuilder = new GuiElementBuilder(SpeedStageManager.getStageIcon(adminCurrentStage))
                .setName(Text.literal("\u00a7f\u0421\u0442\u0435\u0439\u0434\u0436: " + adminStageColor + SpeedStageManager.getStageName(adminCurrentStage)))
                .addLoreLine(Text.literal("\u00a77\u0421\u043A\u043E\u0440\u043E\u0441\u0442\u044C: \u00a7f" + SpeedStageManager.getSpeedForStage(adminCurrentStage)))
                .addLoreLine(Text.literal("\u00a77\u041C\u0430\u043A\u0441. \u043E\u0442\u043A\u0440\u044B\u0442\u044B\u0439: " + SpeedStageManager.getStageName(adminMaxStage)));
        if (adminMaxStage >= 3) {
            adminStageBuilder.addLoreLine(Text.literal("\u00a7a\u0412\u0441\u0435 \u0441\u0442\u0435\u0439\u0434\u0436\u0438 \u043E\u0442\u043A\u0440\u044B\u0442\u044B!"));
            adminStageBuilder.glow();
        }
        gui.setSlot(29, adminStageBuilder.build());

        // Admin buttons
        fillAdminButtons(gui, admin, data, state, onBack);

        gui.open();
    }

    private static void fillInfoSlots(SimpleGui gui, ServerPlayerEntity viewer,
                                       GhastVehicleData data, GaiConfig config) {
        // Plate number
        gui.setSlot(4, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Text.literal("\u00a7f\u041D\u043E\u043C\u0435\u0440: \u00a7e" + data.getPlateNumber()))
                .addLoreLine(Text.literal("\u00a77\u0413\u043E\u0441. \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u043E\u043D\u043D\u044B\u0439 \u043D\u043E\u043C\u0435\u0440"))
                .build());

        // Name (read-only default, overridden in open() for owner)
        gui.setSlot(10, new GuiElementBuilder(Items.GHAST_TEAR)
                .setName(Text.literal("\u00a7f\u0418\u043C\u044F: \u00a7a" + data.getCustomName()))
                .build());

        // Owner
        String ownerName = "\u041D\u0435\u0442";
        if (data.getOwnerUuid() != null) {
            ServerPlayerEntity ownerPlayer = ((ServerWorld) viewer.getEntityWorld()).getServer()
                    .getPlayerManager().getPlayer(data.getOwnerUuid());
            ownerName = ownerPlayer != null ? ownerPlayer.getName().getString()
                    : data.getOwnerUuid().toString().substring(0, 8) + "...";
        }
        gui.setSlot(12, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(Text.literal("\u00a7f\u0412\u043B\u0430\u0434\u0435\u043B\u0435\u0446: \u00a7b" + ownerName))
                .build());

        // Status
        String statusText = data.isImpounded()
                ? "\u00a7c\u041D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0435"
                : data.isRegistered() ? "\u00a7a\u0410\u043A\u0442\u0438\u0432\u0435\u043D" : "\u00a7e\u041D\u0435 \u0437\u0430\u0440\u0435\u0433.";
        gui.setSlot(14, new GuiElementBuilder(data.isImpounded() ? Items.BARRIER : data.isRegistered() ? Items.EMERALD : Items.PAPER)
                .setName(Text.literal("\u00a7f\u0421\u0442\u0430\u0442\u0443\u0441: " + statusText))
                .build());

        // Harness color (read-only default, overridden in open() for owner)
        gui.setSlot(16, new GuiElementBuilder(HarnessManager.getWoolItem(data.getHarnessColor()))
                .setName(Text.literal("\u00a7f\u0421\u0431\u0440\u0443\u044F: \u00a7e" + HarnessManager.getColorName(data.getHarnessColor())))
                .build());

        // Home
        gui.setSlot(22, new GuiElementBuilder(Items.COMPASS)
                .setName(Text.literal("\u00a7f\u0414\u043E\u043C: \u00a77" +
                        data.getHomePos().getX() + ", " + data.getHomePos().getY() + ", " + data.getHomePos().getZ()))
                .addLoreLine(Text.literal("\u00a78" + data.getHomeDimension()))
                .build());

        // UUID
        gui.setSlot(20, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("\u00a7fUUID: \u00a77" + data.getGhastUuid().toString().split("-")[0] + "..."))
                .addLoreLine(Text.literal("\u00a78" + data.getGhastUuid().toString()))
                .build());

        // Mileage
        String mileageStr = String.format("%.1f", data.getMileage());
        gui.setSlot(24, new GuiElementBuilder(Items.CLOCK)
                .setName(Text.literal("\u00a7f\u041F\u0440\u043E\u0431\u0435\u0433: \u00a7a" + mileageStr + " \u0431\u043B\u043E\u043A\u043E\u0432"))
                .addLoreLine(Text.literal("\u00a77\u041E\u0431\u0449\u0435\u0435 \u0440\u0430\u0441\u0441\u0442\u043E\u044F\u043D\u0438\u0435 \u0441 \u043F\u0430\u0441\u0441\u0430\u0436\u0438\u0440\u0430\u043C\u0438"))
                .build());
    }

    private static void fillAdminButtons(SimpleGui gui, ServerPlayerEntity admin,
                                          GhastVehicleData data, GhastRegistryState state,
                                          @Nullable Runnable onBack) {
        MinecraftServer server = ((ServerWorld) admin.getEntityWorld()).getServer();
        GaiConfig config = state.getConfig();

        if (!data.isImpounded()) {
            gui.setSlot(37, new GuiElementBuilder(Items.IRON_BARS)
                    .setName(Text.literal("\u00a7c[\u0410\u0414\u041C\u0418\u041D] \u041A\u043E\u043D\u0444\u0438\u0441\u043A\u043E\u0432\u0430\u0442\u044C"))
                    .addLoreLine(Text.literal("\u00a77\u041E\u0442\u043F\u0440\u0430\u0432\u0438\u0442\u044C \u043D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0443"))
                    .setCallback((index, type, action) -> {
                        if (!config.isImpoundZoneConfigured()) {
                            admin.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u0417\u043E\u043D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0438 \u043D\u0435 \u043D\u0430\u0441\u0442\u0440\u043E\u0435\u043D\u0430!"), false);
                            gui.close();
                            return;
                        }
                        TickHandler.impoundGhast(server, data, state, config);
                        admin.sendMessage(Text.literal("\u00a7a[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043A\u043E\u043D\u0444\u0438\u0441\u043A\u043E\u0432\u0430\u043D."), false);
                        gui.close();
                        if (onBack != null) onBack.run();
                    })
                    .build());
        } else {
            gui.setSlot(37, new GuiElementBuilder(Items.GOLDEN_APPLE)
                    .setName(Text.literal("\u00a7a[\u0410\u0414\u041C\u0418\u041D] \u041E\u0441\u0432\u043E\u0431\u043E\u0434\u0438\u0442\u044C"))
                    .addLoreLine(Text.literal("\u00a77\u0412\u0435\u0440\u043D\u0443\u0442\u044C \u0434\u043E\u043C\u043E\u0439"))
                    .setCallback((index, type, action) -> {
                        TickHandler.releaseGhast(server, data, state);
                        admin.sendMessage(Text.literal("\u00a7a[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043E\u0441\u0432\u043E\u0431\u043E\u0436\u0434\u0451\u043D."), false);
                        gui.close();
                        if (onBack != null) onBack.run();
                    })
                    .build());
        }

        if (onBack != null) {
            gui.setSlot(44, new GuiElementBuilder(Items.ARROW)
                    .setName(Text.literal("\u00a7f\u041D\u0430\u0437\u0430\u0434"))
                    .setCallback((index, type, action) -> {
                        gui.close();
                        onBack.run();
                    })
                    .build());
        }
    }

    // === Owner features (require entity) ===

    private static void openRenameInput(ServerPlayerEntity player, HappyGhastEntity ghast,
                                         GhastVehicleData data, GhastRegistryState state) {
        AnvilInputGui gui = createLockedAnvilGui(player, ghast);
        gui.setTitle(Text.literal("\u041D\u043E\u0432\u043E\u0435 \u0438\u043C\u044F \u0433\u0430\u0441\u0442\u0430"));
        gui.setDefaultInputValue(data.getCustomName());

        gui.setSlot(2, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Text.literal("\u00a7a\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    String newName = gui.getInput();
                    if (newName == null || newName.isBlank()) {
                        player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u0418\u043C\u044F \u043D\u0435 \u043C\u043E\u0436\u0435\u0442 \u0431\u044B\u0442\u044C \u043F\u0443\u0441\u0442\u044B\u043C!"), false);
                        gui.close();
                        return;
                    }
                    navigateCloseAnvil(gui, player);
                    openRenamePayment(player, ghast, data, state, newName.trim());
                })
                .build());
        gui.open();
    }

    private static void openRenamePayment(ServerPlayerEntity player, HappyGhastEntity ghast,
                                            GhastVehicleData data, GhastRegistryState state, String newName) {
        GaiConfig config = state.getConfig();
        boolean adminFree = !player.getUuid().equals(data.getOwnerUuid())
                && player.getCommandSource().getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS));
        int emeralds = adminFree ? 0 : config.getRenameCostEmeralds();
        int diamonds = adminFree ? 0 : config.getRenameCostDiamonds();

        if (emeralds == 0 && diamonds == 0) {
            applyRename(player, ghast, data, state, newName);
            return;
        }

        SimpleGui gui = createLockedGui(ScreenHandlerType.GENERIC_9X1, player, ghast);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u041E\u043F\u043B\u0430\u0442\u0430 \u0441\u043C\u0435\u043D\u044B \u0438\u043C\u0435\u043D\u0438"));

        gui.setSlot(2, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal("\u00a7f\u041D\u043E\u0432\u043E\u0435 \u0438\u043C\u044F: \u00a7a" + newName))
                .build());

        if (emeralds > 0) {
            gui.setSlot(4, new GuiElementBuilder(Items.EMERALD)
                    .setName(Text.literal("\u00a7a\u0418\u0437\u0443\u043C\u0440\u0443\u0434\u044B: " + emeralds))
                    .setCount(Math.max(1, Math.min(64, emeralds)))
                    .build());
        }
        if (diamonds > 0) {
            gui.setSlot(5, new GuiElementBuilder(Items.DIAMOND)
                    .setName(Text.literal("\u00a7b\u0410\u043B\u043C\u0430\u0437\u044B: " + diamonds))
                    .setCount(Math.max(1, Math.min(64, diamonds)))
                    .build());
        }

        gui.setSlot(7, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7a\u041E\u043F\u043B\u0430\u0442\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    if (!hasResources(player, emeralds, diamonds)) {
                        player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0440\u0435\u0441\u0443\u0440\u0441\u043E\u0432!"), false);
                        gui.close();
                        return;
                    }
                    consumeResources(player, emeralds, diamonds);
                    state.getConfig().recordTransaction("rename", emeralds, diamonds);
                    state.markDirty();
                    navigateClose(gui, player);
                    applyRename(player, ghast, data, state, newName);
                })
                .build());

        gui.setSlot(8, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7c\u041E\u0442\u043C\u0435\u043D\u0430"))
                .setCallback((index, type, action) -> {
                    navigateClose(gui, player);
                    open(player, ghast, data, state);
                })
                .build());

        gui.open();
    }

    private static void applyRename(ServerPlayerEntity player, HappyGhastEntity ghast,
                                     GhastVehicleData data, GhastRegistryState state, String newName) {
        data.setCustomName(newName);
        state.markDirty();
        player.sendMessage(Text.literal(
                "\u00a7a[\u0413\u0410\u0418] \u0418\u043C\u044F \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u043E \u043D\u0430: \u00a7e" + newName), false);
        open(player, ghast, data, state);
    }

    private static void openStageSelection(ServerPlayerEntity player, HappyGhastEntity ghast,
                                           GhastVehicleData data, GhastRegistryState state) {
        GaiConfig config = state.getConfig();
        int currentStage = data.getStage();
        int maxStage = data.getMaxStage();

        SimpleGui gui = createLockedGui(ScreenHandlerType.GENERIC_9X1, player, ghast);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0412\u044B\u0431\u043E\u0440 \u0441\u0442\u0435\u0439\u0434\u0436\u0430"));

        for (int s = 1; s <= 3; s++) {
            final int stageNum = s;
            GuiElementBuilder btn = new GuiElementBuilder(SpeedStageManager.getStageIcon(s))
                    .setName(Text.literal(SpeedStageManager.getStageColor(s) + SpeedStageManager.getStageName(s)))
                    .addLoreLine(Text.literal("\u00a77\u0421\u043A\u043E\u0440\u043E\u0441\u0442\u044C: \u00a7f" + SpeedStageManager.getSpeedForStage(s)));

            if (s == currentStage) {
                btn.addLoreLine(Text.literal(""));
                btn.addLoreLine(Text.literal("\u00a7a\u2714 \u0422\u0435\u043A\u0443\u0449\u0438\u0439 \u0441\u0442\u0435\u0439\u0434\u0436"));
                btn.glow();
            } else if (s <= maxStage) {
                btn.addLoreLine(Text.literal(""));
                btn.addLoreLine(Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u043F\u0435\u0440\u0435\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u044F"));
                btn.addLoreLine(Text.literal("\u00a7a\u0411\u0435\u0441\u043F\u043B\u0430\u0442\u043D\u043E"));
                btn.setCallback((index, type, action) -> {
                    data.setStage(stageNum);
                    SpeedStageManager.applyStageSpeed(ghast, stageNum);
                    state.markDirty();
                    player.sendMessage(Text.literal(
                            "\u00a7a[\u0413\u0410\u0418] \u0421\u0442\u0435\u0439\u0434\u0436 \u0438\u0437\u043C\u0435\u043D\u0451\u043D \u043D\u0430: " + SpeedStageManager.getStageName(stageNum)
                                    + " | \u0421\u043A\u043E\u0440\u043E\u0441\u0442\u044C: \u00a7e" + SpeedStageManager.getSpeedForStage(stageNum)), false);
                    navigateClose(gui, player);
                    open(player, ghast, data, state);
                });
            } else if (s == maxStage + 1) {
                int emeralds = s == 2 ? config.getStage2CostEmeralds() : config.getStage3CostEmeralds();
                int diamonds = s == 2 ? config.getStage2CostDiamonds() : config.getStage3CostDiamonds();
                String costStr = formatCost(emeralds, diamonds);
                btn.addLoreLine(Text.literal(""));
                btn.addLoreLine(Text.literal("\u00a7c\u0417\u0430\u043A\u0440\u044B\u0442"));
                btn.addLoreLine(Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435 \u0434\u043B\u044F \u043F\u043E\u043A\u0443\u043F\u043A\u0438"));
                btn.addLoreLine(Text.literal("\u00a77\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: " + costStr));
                btn.setCallback((index, type, action) -> {
                    navigateClose(gui, player);
                    openStagePurchaseConfirm(player, ghast, data, state, stageNum);
                });
            } else {
                btn.addLoreLine(Text.literal(""));
                btn.addLoreLine(Text.literal("\u00a78\u0417\u0430\u043A\u0440\u044B\u0442"));
                btn.addLoreLine(Text.literal("\u00a78\u0421\u043D\u0430\u0447\u0430\u043B\u0430 \u043E\u0442\u043A\u0440\u043E\u0439\u0442\u0435 \u043F\u0440\u0435\u0434\u044B\u0434\u0443\u0449\u0438\u0439"));
            }

            gui.setSlot(s + 1, btn.build());
        }

        gui.setSlot(8, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("\u00a7f\u041D\u0430\u0437\u0430\u0434"))
                .setCallback((index, type, action) -> {
                    navigateClose(gui, player);
                    open(player, ghast, data, state);
                })
                .build());

        gui.open();
    }

    private static void openStagePurchaseConfirm(ServerPlayerEntity player, HappyGhastEntity ghast,
                                                  GhastVehicleData data, GhastRegistryState state, int targetStage) {
        GaiConfig config = state.getConfig();
        boolean adminFree = !player.getUuid().equals(data.getOwnerUuid())
                && player.getCommandSource().getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS));
        int emeralds = adminFree ? 0 : (targetStage == 2 ? config.getStage2CostEmeralds() : config.getStage3CostEmeralds());
        int diamonds = adminFree ? 0 : (targetStage == 2 ? config.getStage2CostDiamonds() : config.getStage3CostDiamonds());

        SimpleGui gui = createLockedGui(ScreenHandlerType.GENERIC_9X1, player, ghast);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u041F\u043E\u043A\u0443\u043F\u043A\u0430 \u0421\u0442\u0435\u0439\u0434\u0436 " + targetStage));

        gui.setSlot(1, new GuiElementBuilder(SpeedStageManager.getStageIcon(targetStage))
                .setName(Text.literal(SpeedStageManager.getStageColor(targetStage) + SpeedStageManager.getStageName(targetStage)))
                .addLoreLine(Text.literal("\u00a77\u0421\u043A\u043E\u0440\u043E\u0441\u0442\u044C: \u00a7f" + SpeedStageManager.getSpeedForStage(targetStage)))
                .build());

        if (emeralds > 0) {
            gui.setSlot(3, new GuiElementBuilder(Items.EMERALD)
                    .setName(Text.literal("\u00a7a\u0418\u0437\u0443\u043C\u0440\u0443\u0434\u044B: " + emeralds))
                    .setCount(Math.max(1, Math.min(64, emeralds)))
                    .build());
        }
        if (diamonds > 0) {
            gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND)
                    .setName(Text.literal("\u00a7b\u0410\u043B\u043C\u0430\u0437\u044B: " + diamonds))
                    .setCount(Math.max(1, Math.min(64, diamonds)))
                    .build());
        }

        gui.setSlot(6, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7a\u041E\u043F\u043B\u0430\u0442\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    if (data.getMaxStage() >= targetStage) {
                        player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u0421\u0442\u0435\u0439\u0434\u0436 \u0443\u0436\u0435 \u043E\u0442\u043A\u0440\u044B\u0442!"), false);
                        gui.close();
                        return;
                    }
                    if (!hasResources(player, emeralds, diamonds)) {
                        player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0440\u0435\u0441\u0443\u0440\u0441\u043E\u0432!"), false);
                        gui.close();
                        return;
                    }
                    consumeResources(player, emeralds, diamonds);
                    state.getConfig().recordTransaction("stage", emeralds, diamonds);
                    data.setMaxStage(targetStage);
                    data.setStage(targetStage);
                    SpeedStageManager.applyStageSpeed(ghast, targetStage);
                    state.markDirty();
                    player.sendMessage(Text.literal(
                            "\u00a7a[\u0413\u0410\u0418] " + SpeedStageManager.getStageName(targetStage)
                                    + " \u043E\u0442\u043A\u0440\u044B\u0442! \u0421\u043A\u043E\u0440\u043E\u0441\u0442\u044C: \u00a7e" + SpeedStageManager.getSpeedForStage(targetStage)), false);
                    navigateClose(gui, player);
                    open(player, ghast, data, state);
                })
                .build());

        gui.setSlot(8, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7c\u041E\u0442\u043C\u0435\u043D\u0430"))
                .setCallback((index, type, action) -> {
                    navigateClose(gui, player);
                    openStageSelection(player, ghast, data, state);
                })
                .build());

        gui.open();
    }

    private static void openColorSelection(ServerPlayerEntity player, HappyGhastEntity ghast,
                                            GhastVehicleData data, GhastRegistryState state) {
        SimpleGui gui = createLockedGui(ScreenHandlerType.GENERIC_9X2, player, ghast);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0421\u043C\u0435\u043D\u0430 \u0446\u0432\u0435\u0442\u0430 \u0441\u0431\u0440\u0443\u0438"));

        for (int i = 0; i < 16; i++) {
            final int colorIndex = i;
            GuiElementBuilder btn = new GuiElementBuilder(HarnessManager.getWoolItem(i))
                    .setName(Text.literal("\u00a7f" + HarnessManager.getColorName(i)));
            if (i == data.getHarnessColor()) {
                btn.addLoreLine(Text.literal("\u00a7a\u2714 \u0422\u0435\u043A\u0443\u0449\u0438\u0439"));
                btn.glow();
            }
            btn.setCallback((index, type, action) -> {
                data.setHarnessColor(colorIndex);
                HarnessManager.equipHarness(ghast, colorIndex);
                state.markDirty();
                player.sendMessage(Text.literal(
                        "\u00a7a[\u0413\u0410\u0418] \u0426\u0432\u0435\u0442 \u0441\u0431\u0440\u0443\u0438: \u00a7e" + HarnessManager.getColorName(colorIndex)), false);
                navigateClose(gui, player);
                open(player, ghast, data, state);
            });
            gui.setSlot(i, btn.build());
        }
        gui.open();
    }

    // === Particle selection ===

    private static void openParticleSelection(ServerPlayerEntity player, HappyGhastEntity ghast,
                                               GhastVehicleData data, GhastRegistryState state) {
        GaiConfig config = state.getConfig();
        var presets = ParticleTrailManager.getPresets();

        SimpleGui gui = createLockedGui(ScreenHandlerType.GENERIC_9X2, player, ghast);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0412\u044B\u0431\u043E\u0440 \u0447\u0430\u0441\u0442\u0438\u0446"));

        int slot = 0;
        for (var entry : presets.entrySet()) {
            if (slot >= 16) break;
            final String pid = entry.getKey();
            final ParticleTrailManager.ParticlePreset preset = entry.getValue();

            GuiElementBuilder btn = new GuiElementBuilder(preset.icon())
                    .setName(Text.literal("\u00a7f" + preset.name()));

            boolean isCurrent = pid.equals(data.getParticleId());
            if (isCurrent) {
                btn.addLoreLine(Text.literal("\u00a7a\u2714 \u0422\u0435\u043A\u0443\u0449\u0438\u0435"));
                btn.glow();
            }

            if (pid.equals("none")) {
                btn.addLoreLine(Text.literal("\u00a7a\u0411\u0435\u0441\u043F\u043B\u0430\u0442\u043D\u043E"));
                btn.setCallback((index, type, action) -> {
                    data.setParticleId("none");
                    state.markDirty();
                    player.sendMessage(Text.literal("\u00a7a[\u0413\u0410\u0418] \u0427\u0430\u0441\u0442\u0438\u0446\u044B \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u044B."), false);
                    navigateClose(gui, player);
                    open(player, ghast, data, state);
                });
            } else if (preset.isDust()) {
                btn.addLoreLine(Text.literal("\u00a77\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: " + formatCost(config.getParticleCostEmeralds(), config.getParticleCostDiamonds())));
                btn.addLoreLine(Text.literal("\u00a7e\u0412\u044B\u0431\u0435\u0440\u0438\u0442\u0435 \u0446\u0432\u0435\u0442"));
                btn.setCallback((index, type, action) -> {
                    navigateClose(gui, player);
                    openDustColorSelection(player, ghast, data, state);
                });
            } else {
                btn.addLoreLine(Text.literal("\u00a77\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C: " + formatCost(config.getParticleCostEmeralds(), config.getParticleCostDiamonds())));
                btn.setCallback((index, type, action) -> {
                    navigateClose(gui, player);
                    openParticlePayment(player, ghast, data, state, pid);
                });
            }

            gui.setSlot(slot, btn.build());
            slot++;
        }

        gui.setSlot(17, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("\u00a7f\u041D\u0430\u0437\u0430\u0434"))
                .setCallback((index, type, action) -> {
                    navigateClose(gui, player);
                    open(player, ghast, data, state);
                })
                .build());

        gui.open();
    }

    private static void openDustColorSelection(ServerPlayerEntity player, HappyGhastEntity ghast,
                                                GhastVehicleData data, GhastRegistryState state) {
        ParticleTrailManager.DustColor[] colors = ParticleTrailManager.getDustColors();
        SimpleGui gui = createLockedGui(ScreenHandlerType.GENERIC_9X2, player, ghast);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0426\u0432\u0435\u0442 \u043F\u044B\u043B\u0438"));

        for (int i = 0; i < colors.length && i < 16; i++) {
            final ParticleTrailManager.DustColor dc = colors[i];
            GuiElementBuilder btn = new GuiElementBuilder(dc.icon())
                    .setName(Text.literal("\u00a7f" + dc.name()));
            if ("dust".equals(data.getParticleId()) && data.getParticleColor() == dc.color()) {
                btn.addLoreLine(Text.literal("\u00a7a\u2714 \u0422\u0435\u043A\u0443\u0449\u0438\u0439"));
                btn.glow();
            }
            btn.setCallback((index, type, action) -> {
                navigateClose(gui, player);
                openParticlePayment(player, ghast, data, state, "dust", dc.color());
            });
            gui.setSlot(i, btn.build());
        }

        gui.setSlot(17, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("\u00a7f\u041D\u0430\u0437\u0430\u0434"))
                .setCallback((index, type, action) -> {
                    navigateClose(gui, player);
                    openParticleSelection(player, ghast, data, state);
                })
                .build());

        gui.open();
    }

    private static void openParticlePayment(ServerPlayerEntity player, HappyGhastEntity ghast,
                                             GhastVehicleData data, GhastRegistryState state, String particleId) {
        openParticlePayment(player, ghast, data, state, particleId, data.getParticleColor());
    }

    private static void openParticlePayment(ServerPlayerEntity player, HappyGhastEntity ghast,
                                             GhastVehicleData data, GhastRegistryState state,
                                             String particleId, int dustColor) {
        GaiConfig config = state.getConfig();
        boolean adminFree = !player.getUuid().equals(data.getOwnerUuid())
                && player.getCommandSource().getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS));
        int emeralds = adminFree ? 0 : config.getParticleCostEmeralds();
        int diamonds = adminFree ? 0 : config.getParticleCostDiamonds();

        if (emeralds == 0 && diamonds == 0) {
            applyParticle(player, ghast, data, state, particleId, dustColor);
            return;
        }

        ParticleTrailManager.ParticlePreset preset = ParticleTrailManager.getPreset(particleId);
        String displayName = preset.name();
        if (preset.isDust()) {
            displayName += " (" + ParticleTrailManager.getDustColorName(dustColor) + ")";
        }

        SimpleGui gui = createLockedGui(ScreenHandlerType.GENERIC_9X1, player, ghast);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u041E\u043F\u043B\u0430\u0442\u0430 \u0447\u0430\u0441\u0442\u0438\u0446"));

        gui.setSlot(1, new GuiElementBuilder(preset.icon())
                .setName(Text.literal("\u00a7d" + displayName))
                .build());

        if (emeralds > 0) {
            gui.setSlot(3, new GuiElementBuilder(Items.EMERALD)
                    .setName(Text.literal("\u00a7a\u0418\u0437\u0443\u043C\u0440\u0443\u0434\u044B: " + emeralds))
                    .setCount(Math.max(1, Math.min(64, emeralds)))
                    .build());
        }
        if (diamonds > 0) {
            gui.setSlot(4, new GuiElementBuilder(Items.DIAMOND)
                    .setName(Text.literal("\u00a7b\u0410\u043B\u043C\u0430\u0437\u044B: " + diamonds))
                    .setCount(Math.max(1, Math.min(64, diamonds)))
                    .build());
        }

        final String fParticleId = particleId;
        final int fDustColor = dustColor;
        gui.setSlot(6, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7a\u041E\u043F\u043B\u0430\u0442\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    if (!hasResources(player, emeralds, diamonds)) {
                        player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0440\u0435\u0441\u0443\u0440\u0441\u043E\u0432!"), false);
                        gui.close();
                        return;
                    }
                    consumeResources(player, emeralds, diamonds);
                    state.getConfig().recordTransaction("particle", emeralds, diamonds);
                    state.markDirty();
                    navigateClose(gui, player);
                    applyParticle(player, ghast, data, state, fParticleId, fDustColor);
                })
                .build());

        gui.setSlot(8, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7c\u041E\u0442\u043C\u0435\u043D\u0430"))
                .setCallback((index, type, action) -> {
                    navigateClose(gui, player);
                    openParticleSelection(player, ghast, data, state);
                })
                .build());

        gui.open();
    }

    private static void applyParticle(ServerPlayerEntity player, HappyGhastEntity ghast,
                                       GhastVehicleData data, GhastRegistryState state,
                                       String particleId, int dustColor) {
        data.setParticleId(particleId);
        data.setParticleColor(dustColor);
        state.markDirty();
        ParticleTrailManager.ParticlePreset preset = ParticleTrailManager.getPreset(particleId);
        String displayName = preset.name();
        if (preset.isDust()) {
            displayName += " (" + ParticleTrailManager.getDustColorName(dustColor) + ")";
        }
        player.sendMessage(Text.literal(
                "\u00a7a[\u0413\u0410\u0418] \u0427\u0430\u0441\u0442\u0438\u0446\u044B \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u044B \u043D\u0430: \u00a7d" + displayName), false);
        open(player, ghast, data, state);
    }

    // === Replate ===

    private static void openReplateInput(ServerPlayerEntity player, HappyGhastEntity ghast,
                                          GhastVehicleData data, GhastRegistryState state) {
        AnvilInputGui gui = createLockedAnvilGui(player, ghast);
        gui.setTitle(Text.literal("\u041D\u043E\u0432\u044B\u0439 \u043D\u043E\u043C\u0435\u0440 (\u0410000\u0410\u0410)"));
        gui.setDefaultInputValue(data.getPlateNumber().replace(" 52", ""));

        gui.setSlot(2, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("\u00a7a\u041F\u043E\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    String input = gui.getInput();
                    if (input == null || input.isBlank()) {
                        player.sendMessage(Text.literal("\u00a7c[\u0413\u0410\u0418] \u041D\u043E\u043C\u0435\u0440 \u043D\u0435 \u043C\u043E\u0436\u0435\u0442 \u0431\u044B\u0442\u044C \u043F\u0443\u0441\u0442\u044B\u043C!"), false);
                        gui.close();
                        return;
                    }
                    String normalized = PlateGenerator.normalize(input.trim().toUpperCase());
                    if (!PlateGenerator.isValidFormat(normalized)) {
                        player.sendMessage(Text.literal(
                                "\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u0444\u043E\u0440\u043C\u0430\u0442! \u0424\u043E\u0440\u043C\u0430\u0442: \u0411000\u0411\u0411 (\u0411 = \u0410\u0412\u0415\u041A\u041C\u041D\u041E\u0420\u0421\u0422\u0423\u0425)"), false);
                        gui.close();
                        return;
                    }
                    if (state.getUsedPlateNumbers().contains(normalized) && !normalized.equals(data.getPlateNumber())) {
                        player.sendMessage(Text.literal(
                                "\u00a7c[\u0413\u0410\u0418] \u041D\u043E\u043C\u0435\u0440 " + normalized + " \u0443\u0436\u0435 \u0437\u0430\u043D\u044F\u0442!"), false);
                        gui.close();
                        return;
                    }
                    if (normalized.equals(data.getPlateNumber())) {
                        player.sendMessage(Text.literal(
                                "\u00a7e[\u0413\u0410\u0418] \u042D\u0442\u043E \u0443\u0436\u0435 \u0432\u0430\u0448 \u0442\u0435\u043A\u0443\u0449\u0438\u0439 \u043D\u043E\u043C\u0435\u0440!"), false);
                        gui.close();
                        return;
                    }
                    navigateCloseAnvil(gui, player);
                    openReplatePayment(player, ghast, data, state, normalized);
                })
                .build());
        gui.open();
    }

    private static void openReplatePayment(ServerPlayerEntity player, HappyGhastEntity ghast,
                                            GhastVehicleData data, GhastRegistryState state,
                                            String newPlate) {
        GaiConfig config = state.getConfig();
        int starCost = config.getReplateCostNetherStars();

        SimpleGui gui = createLockedGui(ScreenHandlerType.GENERIC_9X1, player, ghast);
        gui.setTitle(Text.literal("\u00a76\u0413\u0410\u0418 | \u0421\u043C\u0435\u043D\u0430 \u043D\u043E\u043C\u0435\u0440\u0430"));

        gui.setSlot(2, new GuiElementBuilder(Items.NAME_TAG)
                .setName(Text.literal("\u00a7f\u041D\u043E\u0432\u044B\u0439: \u00a7e" + newPlate))
                .addLoreLine(Text.literal("\u00a77\u0421\u0442\u0430\u0440\u044B\u0439: \u00a78" + data.getPlateNumber()))
                .build());

        gui.setSlot(4, new GuiElementBuilder(Items.NETHER_STAR)
                .setName(Text.literal("\u00a7d\u0417\u0432\u0435\u0437\u0434\u044B \u041D\u0435\u0437\u0435\u0440\u0430: " + starCost))
                .setCount(Math.max(1, Math.min(64, starCost)))
                .build());

        gui.setSlot(6, new GuiElementBuilder(Items.LIME_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7a\u041E\u043F\u043B\u0430\u0442\u0438\u0442\u044C"))
                .setCallback((index, type, action) -> {
                    if (countItem(player, Items.NETHER_STAR) < starCost) {
                        player.sendMessage(Text.literal(
                                "\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0437\u0432\u0451\u0437\u0434 \u041D\u0435\u0437\u0435\u0440\u0430!"), false);
                        gui.close();
                        return;
                    }
                    if (state.getUsedPlateNumbers().contains(newPlate) && !newPlate.equals(data.getPlateNumber())) {
                        player.sendMessage(Text.literal(
                                "\u00a7c[\u0413\u0410\u0418] \u041D\u043E\u043C\u0435\u0440 " + newPlate + " \u0443\u0436\u0435 \u0437\u0430\u043D\u044F\u0442!"), false);
                        gui.close();
                        return;
                    }
                    removeItems(player, Items.NETHER_STAR, starCost);
                    config.recordTransaction("replate", starCost, 0);

                    // Remove old plates
                    ServerWorld world = (ServerWorld) ghast.getEntityWorld();
                    MinecraftServer server = world.getServer();
                    TickHandler.removePlatesAllWorlds(server, data);

                    // Update plate in registry
                    state.replateGhast(data, newPlate);

                    // Recreate plates
                    PlateDisplayManager.createPlates(world, ghast, data);

                    player.sendMessage(Text.literal(
                            "\u00a7a[\u0413\u0410\u0418] \u041D\u043E\u043C\u0435\u0440 \u0438\u0437\u043C\u0435\u043D\u0451\u043D: \u00a7e" + newPlate), false);
                    gui.close();
                })
                .build());

        gui.setSlot(8, new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
                .setName(Text.literal("\u00a7c\u041E\u0442\u043C\u0435\u043D\u0430"))
                .setCallback((index, type, action) -> gui.close())
                .build());

        gui.open();
    }

    // === Utility ===

    static String formatCost(int emeralds, int diamonds) {
        if (emeralds == 0 && diamonds == 0) return "\u00a7a\u0411\u0435\u0441\u043F\u043B\u0430\u0442\u043D\u043E";
        StringBuilder sb = new StringBuilder();
        if (emeralds > 0) sb.append("\u00a7a").append(emeralds).append(" \u0438\u0437\u0443\u043C.");
        if (emeralds > 0 && diamonds > 0) sb.append(" \u00a77+ ");
        if (diamonds > 0) sb.append("\u00a7b").append(diamonds).append(" \u0430\u043B\u043C.");
        return sb.toString();
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
