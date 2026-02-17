package com.happyghast.gai.command;

import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.gui.AdminConfigGui;
import com.happyghast.gai.gui.GhastInfoGui;
import com.happyghast.gai.gui.GhastSaleGui;
import com.happyghast.gai.gui.GhastSaleManager;
import com.happyghast.gai.gui.GhastTpGui;
import com.happyghast.gai.gui.PlayerGhastsGui;
import com.happyghast.gai.gui.ServerStatsGui;
import com.happyghast.gai.handler.TickHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.permission.Permission;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public class GaiCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal("gai")

                .then(CommandManager.literal("tp")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            MinecraftServer server = ctx.getSource().getServer();
                            GhastRegistryState state = GhastRegistryState.get(server);
                            boolean isAdmin = player.getCommandSource().getPermissions()
                                    .hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS));
                            GhastTpGui.open(player, state, isAdmin);
                            return 1;
                        })
                        .then(CommandManager.argument("target", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                    String target = StringArgumentType.getString(ctx, "target");
                                    MinecraftServer server = ctx.getSource().getServer();
                                    GhastRegistryState state = GhastRegistryState.get(server);
                                    boolean isAdmin = player.getCommandSource().getPermissions()
                                            .hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS));

                                    GhastVehicleData data = findGhastForTp(state, player, target, isAdmin);
                                    if (data == null) {
                                        ctx.getSource().sendError(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D: " + target));
                                        return 0;
                                    }
                                    GhastTpGui.executeTp(player, data, state, isAdmin);
                                    return 1;
                                })))

                .then(CommandManager.literal("_acceptsale")
                        .then(CommandManager.argument("offerid", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                    String offerId = StringArgumentType.getString(ctx, "offerid");
                                    MinecraftServer server = ctx.getSource().getServer();
                                    GhastRegistryState state = GhastRegistryState.get(server);
                                    GhastSaleManager.SaleOffer offer = GhastSaleManager.getOffer(offerId);
                                    if (offer == null) {
                                        player.sendMessage(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u041F\u0440\u0435\u0434\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u0438\u0441\u0442\u0435\u043A\u043B\u043E \u0438\u043B\u0438 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E."), false);
                                        return 0;
                                    }
                                    if (!offer.buyerUuid().equals(player.getUuid())) {
                                        player.sendMessage(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u042D\u0442\u043E \u043F\u0440\u0435\u0434\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u043D\u0435 \u0434\u043B\u044F \u0432\u0430\u0441."), false);
                                        return 0;
                                    }
                                    GhastVehicleData data = state.getGhast(offer.ghastUuid());
                                    if (data == null) {
                                        player.sendMessage(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u0431\u043E\u043B\u044C\u0448\u0435 \u043D\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442."), false);
                                        GhastSaleManager.removeOffer(offerId);
                                        return 0;
                                    }
                                    GhastSaleGui.openBuyerConfirmation(player, offerId, offer, data, state);
                                    return 1;
                                })))

                .then(CommandManager.literal("zone")
                        .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                        .then(CommandManager.literal("set")
                                .then(CommandManager.literal("corner1")
                                        .executes(ctx -> {
                                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                            ServerWorld world = (ServerWorld) player.getEntityWorld();
                                            GhastRegistryState state = GhastRegistryState.get(world.getServer());
                                            GaiConfig config = state.getConfig();

                                            BlockPos pos = player.getBlockPos();
                                            config.setImpoundCorner1(pos);
                                            config.setImpoundDimension(world.getRegistryKey().getValue().toString());
                                            state.saveConfig();

                                            ctx.getSource().sendFeedback(() -> Text.literal(
                                                    "\u00a7a[\u0413\u0410\u0418] \u0423\u0433\u043E\u043B 1 \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D: " + pos.toShortString()), false);
                                            return 1;
                                        }))
                                .then(CommandManager.literal("corner2")
                                        .executes(ctx -> {
                                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                            GhastRegistryState state = GhastRegistryState.get(((ServerWorld) player.getEntityWorld()).getServer());
                                            GaiConfig config = state.getConfig();

                                            BlockPos pos = player.getBlockPos();
                                            config.setImpoundCorner2(pos);
                                            state.saveConfig();

                                            ctx.getSource().sendFeedback(() -> Text.literal(
                                                    "\u00a7a[\u0413\u0410\u0418] \u0423\u0433\u043E\u043B 2 \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D: " + pos.toShortString()), false);
                                            return 1;
                                        }))))

                .then(CommandManager.literal("impound")
                        .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                        .then(CommandManager.argument("target", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String target = StringArgumentType.getString(ctx, "target");
                                    GhastRegistryState state = GhastRegistryState.get(
                                            ctx.getSource().getServer());
                                    GaiConfig config = state.getConfig();

                                    GhastVehicleData data = findGhastData(state, target);
                                    if (data == null) {
                                        ctx.getSource().sendError(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D: " + target));
                                        return 0;
                                    }
                                    if (data.isImpounded()) {
                                        ctx.getSource().sendError(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u0443\u0436\u0435 \u043D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0435."));
                                        return 0;
                                    }
                                    if (!config.isImpoundZoneConfigured()) {
                                        ctx.getSource().sendError(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u0417\u043E\u043D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0438 \u043D\u0435 \u043D\u0430\u0441\u0442\u0440\u043E\u0435\u043D\u0430!"));
                                        return 0;
                                    }
                                    TickHandler.impoundGhast(ctx.getSource().getServer(), data, state, config);
                                    ctx.getSource().sendFeedback(() -> Text.literal(
                                            "\u00a7a[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043A\u043E\u043D\u0444\u0438\u0441\u043A\u043E\u0432\u0430\u043D."), true);
                                    return 1;
                                })))

                .then(CommandManager.literal("release")
                        .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                        .then(CommandManager.argument("target", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String target = StringArgumentType.getString(ctx, "target");
                                    GhastRegistryState state = GhastRegistryState.get(
                                            ctx.getSource().getServer());

                                    GhastVehicleData data = findGhastData(state, target);
                                    if (data == null) {
                                        ctx.getSource().sendError(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D: " + target));
                                        return 0;
                                    }
                                    if (!data.isImpounded()) {
                                        ctx.getSource().sendError(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043D\u0435 \u043D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0435."));
                                        return 0;
                                    }
                                    TickHandler.releaseGhast(ctx.getSource().getServer(), data, state);
                                    ctx.getSource().sendFeedback(() -> Text.literal(
                                            "\u00a7a[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043E\u0441\u0432\u043E\u0431\u043E\u0436\u0434\u0451\u043D."), true);
                                    return 1;
                                })))

                .then(CommandManager.literal("config")
                        .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            GhastRegistryState state = GhastRegistryState.get(ctx.getSource().getServer());
                            AdminConfigGui.open(player, state);
                            return 1;
                        }))

                .then(CommandManager.literal("stats")
                        .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            GhastRegistryState state = GhastRegistryState.get(ctx.getSource().getServer());
                            ServerStatsGui.open(player, state);
                            return 1;
                        }))

                .then(CommandManager.literal("list")
                        .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                        .executes(ctx -> {
                            MinecraftServer server = ctx.getSource().getServer();
                            GhastRegistryState state = GhastRegistryState.get(server);
                            ctx.getSource().sendFeedback(() -> Text.literal(
                                    "\u00a76[\u0413\u0410\u0418] \u0412\u0441\u0435\u0433\u043E \u0433\u0430\u0441\u0442\u043E\u0432: " + state.getAllGhasts().size() +
                                            " | \u0417\u0430\u0440\u0435\u0433.: " + state.getRegisteredGhasts().size() +
                                            " | \u041D\u0430 \u0448\u0442\u0440\u0430\u0444\u0441\u0442\u043E\u044F\u043D\u043A\u0435: " + state.getImpoundedGhasts().size()), false);

                            for (GhastVehicleData data : state.getAllGhasts()) {
                                String status = data.isImpounded() ? "\u00a7c\u0428\u0442\u0440\u0430\u0444" : data.isRegistered()
                                        ? "\u00a7a\u0420\u0435\u0433" : "\u00a7e\u041D\u043E\u0432\u044B\u0439";
                                String name = data.getCustomName().isEmpty() ? "---" : data.getCustomName();
                                String plate = data.getPlateNumber().isEmpty() ? "---" : data.getPlateNumber();
                                String shortId = data.getGhastUuid().toString().split("-")[0];
                                String ownerDisplay = "\u00a78\u043D\u0435\u0442";
                                if (data.getOwnerUuid() != null) {
                                    ServerPlayerEntity ownerPlayer = server.getPlayerManager().getPlayer(data.getOwnerUuid());
                                    ownerDisplay = ownerPlayer != null
                                            ? "\u00a7b" + ownerPlayer.getName().getString()
                                            : "\u00a78" + data.getOwnerUuid().toString().substring(0, 8) + "...";
                                }
                                final String ownerStr = ownerDisplay;
                                ctx.getSource().sendFeedback(() -> Text.literal(
                                        "  " + status + " \u00a7f" + name + " [\u00a7e" + plate + "\u00a7f] \u00a77" + shortId + " \u00a7f\u0412\u043B.: " + ownerStr), false);
                            }
                            return 1;
                        }))

                .then(CommandManager.literal("info")
                        .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                        .then(CommandManager.argument("target", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String target = StringArgumentType.getString(ctx, "target");
                                    MinecraftServer server = ctx.getSource().getServer();
                                    GhastRegistryState state = GhastRegistryState.get(server);
                                    GhastVehicleData data = findGhastData(state, target);
                                    if (data == null) {
                                        ctx.getSource().sendError(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D: " + target));
                                        return 0;
                                    }
                                    ServerPlayerEntity admin = ctx.getSource().getPlayerOrThrow();
                                    GhastInfoGui.openAdminView(admin, data, state, null);
                                    return 1;
                                })))

                .then(CommandManager.literal("player")
                        .requires(source -> source.getPermissions().hasPermission(new Permission.Level(PermissionLevel.GAMEMASTERS)))
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    String playerName = StringArgumentType.getString(ctx, "name");
                                    MinecraftServer server = ctx.getSource().getServer();
                                    GhastRegistryState state = GhastRegistryState.get(server);
                                    ServerPlayerEntity admin = ctx.getSource().getPlayerOrThrow();

                                    UUID targetUuid = null;
                                    String displayName = playerName;

                                    ServerPlayerEntity onlineTarget = server.getPlayerManager().getPlayer(playerName);
                                    if (onlineTarget != null) {
                                        targetUuid = onlineTarget.getUuid();
                                        displayName = onlineTarget.getName().getString();
                                    } else {
                                        for (GhastVehicleData d : state.getAllGhasts()) {
                                            if (d.getOwnerUuid() != null) {
                                                ServerPlayerEntity cached = server.getPlayerManager().getPlayer(d.getOwnerUuid());
                                                if (cached != null && cached.getName().getString().equalsIgnoreCase(playerName)) {
                                                    targetUuid = d.getOwnerUuid();
                                                    displayName = cached.getName().getString();
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if (targetUuid == null) {
                                        ctx.getSource().sendError(Text.literal(
                                                "\u00a7c[\u0413\u0410\u0418] \u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D \u0438\u043B\u0438 \u043D\u0435 \u0432 \u0441\u0435\u0442\u0438: " + playerName));
                                        return 0;
                                    }

                                    List<GhastVehicleData> ghasts = state.getGhastsByOwner(targetUuid);
                                    if (ghasts.isEmpty()) {
                                        final String dn = displayName;
                                        ctx.getSource().sendFeedback(() -> Text.literal(
                                                "\u00a7e[\u0413\u0410\u0418] \u0423 \u0438\u0433\u0440\u043E\u043A\u0430 " + dn + " \u043D\u0435\u0442 \u0433\u0430\u0441\u0442\u043E\u0432."), false);
                                        return 0;
                                    }

                                    PlayerGhastsGui.open(admin, displayName, targetUuid, state, 0);
                                    return 1;
                                })))
        );
    }

    private static GhastVehicleData findGhastForTp(GhastRegistryState state, ServerPlayerEntity player,
                                                      String target, boolean isAdmin) {
        GhastVehicleData data = findGhastData(state, target);
        if (data == null) return null;
        if (!data.isRegistered()) return null;
        if (!isAdmin && (data.getOwnerUuid() == null || !data.getOwnerUuid().equals(player.getUuid()))) {
            return null;
        }
        return data;
    }

    private static GhastVehicleData findGhastData(GhastRegistryState state, String target) {
        GhastVehicleData byPlate = state.findByPlateNumber(target);
        if (byPlate != null) return byPlate;

        try {
            UUID uuid = UUID.fromString(target);
            return state.getGhast(uuid);
        } catch (IllegalArgumentException ignored) {}

        for (GhastVehicleData data : state.getAllGhasts()) {
            if (data.getCustomName().equalsIgnoreCase(target)) return data;
        }

        String lowerTarget = target.toLowerCase();
        GhastVehicleData partialMatch = null;
        int matches = 0;
        for (GhastVehicleData data : state.getAllGhasts()) {
            if (data.getGhastUuid().toString().toLowerCase().startsWith(lowerTarget)) {
                partialMatch = data;
                matches++;
            }
        }
        if (matches == 1) return partialMatch;

        return null;
    }
}
