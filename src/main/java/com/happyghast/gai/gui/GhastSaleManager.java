package com.happyghast.gai.gui;

import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.handler.PlateTickHandler;
import com.happyghast.gai.handler.TickHandler;
import com.happyghast.gai.plate.PlateDisplayManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GhastSaleManager {

    public record SaleOffer(UUID ghastUuid, UUID sellerUuid, UUID buyerUuid,
                            int priceEmeralds, int priceDiamonds, long createdAt) {}

    private static final Map<String, SaleOffer> PENDING_OFFERS = new ConcurrentHashMap<>();
    private static final long OFFER_EXPIRY_MS = 5 * 60 * 1000;

    public static String createOffer(UUID ghastUuid, UUID sellerUuid, UUID buyerUuid,
                                     int priceEmeralds, int priceDiamonds) {
        cleanExpired();
        // Remove any existing offer for this ghast
        PENDING_OFFERS.entrySet().removeIf(e -> e.getValue().ghastUuid().equals(ghastUuid));

        String offerId = UUID.randomUUID().toString().substring(0, 8);
        PENDING_OFFERS.put(offerId, new SaleOffer(ghastUuid, sellerUuid, buyerUuid,
                priceEmeralds, priceDiamonds, System.currentTimeMillis()));
        return offerId;
    }

    @Nullable
    public static SaleOffer getOffer(String offerId) {
        cleanExpired();
        return PENDING_OFFERS.get(offerId);
    }

    public static void removeOffer(String offerId) {
        PENDING_OFFERS.remove(offerId);
    }

    public static void sendOfferToBuyer(MinecraftServer server, SaleOffer offer, String offerId,
                                         GhastVehicleData data, String sellerName) {
        ServerPlayerEntity buyer = server.getPlayerManager().getPlayer(offer.buyerUuid());
        if (buyer == null) return;

        String ghastName = data.getCustomName().isEmpty() ? "Гаст" : data.getCustomName();
        String plate = data.getPlateNumber();
        String priceStr = formatPrice(offer.priceEmeralds(), offer.priceDiamonds());

        buyer.sendMessage(Text.literal(""), false);
        buyer.sendMessage(Text.literal(
                "\u00a76\u00a7l[\u0413\u0410\u0418] \u00a7e\u041F\u0440\u0435\u0434\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u043E \u043F\u043E\u043A\u0443\u043F\u043A\u0435 \u0433\u0430\u0441\u0442\u0430!"), false);
        buyer.sendMessage(Text.literal(
                "\u00a7f\u0418\u0433\u0440\u043E\u043A \u00a7b" + sellerName + "\u00a7f \u043F\u0440\u043E\u0434\u0430\u0451\u0442 \u0433\u0430\u0441\u0442\u0430 \u00a7a" + ghastName + " \u00a7f[\u00a7e" + plate + "\u00a7f]"), false);
        buyer.sendMessage(Text.literal(
                "\u00a7f\u0426\u0435\u043D\u0430: " + priceStr), false);

        Text clickText = Text.literal("\u00a7a\u00a7l\u00a7n[\u041D\u0430\u0436\u043C\u0438\u0442\u0435 \u0434\u043B\u044F \u043F\u043E\u043A\u0443\u043F\u043A\u0438]")
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent.RunCommand("/gai _acceptsale " + offerId))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Text.literal("\u00a7e\u041A\u043B\u0438\u043A\u043D\u0438\u0442\u0435, \u0447\u0442\u043E\u0431\u044B \u043E\u0442\u043A\u0440\u044B\u0442\u044C \u043C\u0435\u043D\u044E \u043F\u043E\u043A\u0443\u043F\u043A\u0438"))));

        buyer.sendMessage(clickText, false);
        buyer.sendMessage(Text.literal("\u00a78\u041F\u0440\u0435\u0434\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u0434\u0435\u0439\u0441\u0442\u0432\u0443\u0435\u0442 5 \u043C\u0438\u043D\u0443\u0442."), false);
        buyer.sendMessage(Text.literal(""), false);
    }

    public static boolean executeSale(MinecraftServer server, String offerId, GhastRegistryState state) {
        SaleOffer offer = getOffer(offerId);
        if (offer == null) return false;

        GhastVehicleData data = state.getGhast(offer.ghastUuid());
        if (data == null) return false;

        ServerPlayerEntity buyer = server.getPlayerManager().getPlayer(offer.buyerUuid());
        ServerPlayerEntity seller = server.getPlayerManager().getPlayer(offer.sellerUuid());
        if (buyer == null) return false;

        GaiConfig config = state.getConfig();

        // Check buyer has enough for the price
        if (!hasResources(buyer, offer.priceEmeralds(), offer.priceDiamonds())) {
            buyer.sendMessage(Text.literal(
                    "\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0440\u0435\u0441\u0443\u0440\u0441\u043E\u0432 \u0434\u043B\u044F \u043F\u043E\u043A\u0443\u043F\u043A\u0438!"), false);
            return false;
        }

        // Check seller has enough for disposal fee
        if (seller != null && !hasResources(seller, config.getDisposalFeeEmeralds(), config.getDisposalFeeDiamonds())) {
            seller.sendMessage(Text.literal(
                    "\u00a7c[\u0413\u0410\u0418] \u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0440\u0435\u0441\u0443\u0440\u0441\u043E\u0432 \u0434\u043B\u044F \u0443\u0442\u0438\u043B\u044C\u0441\u0431\u043E\u0440\u0430!"), false);
            buyer.sendMessage(Text.literal(
                    "\u00a7c[\u0413\u0410\u0418] \u041F\u0440\u043E\u0434\u0430\u0432\u0435\u0446 \u043D\u0435 \u043C\u043E\u0436\u0435\u0442 \u043E\u043F\u043B\u0430\u0442\u0438\u0442\u044C \u0443\u0442\u0438\u043B\u044C\u0441\u0431\u043E\u0440. \u0421\u0434\u0435\u043B\u043A\u0430 \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430."), false);
            removeOffer(offerId);
            return false;
        }
        if (seller == null) {
            buyer.sendMessage(Text.literal(
                    "\u00a7c[\u0413\u0410\u0418] \u041F\u0440\u043E\u0434\u0430\u0432\u0435\u0446 \u043D\u0435 \u0432 \u0441\u0435\u0442\u0438. \u0421\u0434\u0435\u043B\u043A\u0430 \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430."), false);
            removeOffer(offerId);
            return false;
        }

        // Consume resources
        consumeResources(buyer, offer.priceEmeralds(), offer.priceDiamonds());
        consumeResources(seller, config.getDisposalFeeEmeralds(), config.getDisposalFeeDiamonds());

        // Give price to seller
        giveResources(seller, offer.priceEmeralds(), offer.priceDiamonds());

        // Record transactions
        config.recordTransaction("sale", config.getDisposalFeeEmeralds(), config.getDisposalFeeDiamonds());

        // Transfer ownership
        data.setOwnerUuid(buyer.getUuid());
        state.markDirty();

        // Teleport ghast to buyer
        HappyGhastEntity ghast = findGhastEntity(server, data.getGhastUuid());
        if (ghast != null) {
            ServerWorld buyerWorld = (ServerWorld) buyer.getEntityWorld();
            ghast.removeAllPassengers();
            TickHandler.removePlatesAllWorlds(server, data);

            ghast.teleport(buyerWorld, buyer.getX(), buyer.getY() + 1, buyer.getZ(),
                    java.util.Set.of(), buyer.getYaw(), buyer.getPitch(), false);

            PlateDisplayManager.createPlates(buyerWorld, ghast, data);
            PlateTickHandler.setSaleFreeze(data.getGhastUuid());
        }

        // Notify
        String ghastName = data.getCustomName().isEmpty() ? "Гаст" : data.getCustomName();
        buyer.sendMessage(Text.literal(
                "\u00a7a[\u0413\u0410\u0418] \u0412\u044B \u043A\u0443\u043F\u0438\u043B\u0438 \u0433\u0430\u0441\u0442\u0430 \u00a7e" + ghastName + "\u00a7a! \u041E\u043D \u0442\u0435\u043B\u0435\u043F\u043E\u0440\u0442\u0438\u0440\u043E\u0432\u0430\u043D \u043A \u0432\u0430\u043C."), false);
        seller.sendMessage(Text.literal(
                "\u00a7a[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u00a7e" + ghastName + "\u00a7a \u043F\u0440\u043E\u0434\u0430\u043D \u0438\u0433\u0440\u043E\u043A\u0443 \u00a7b" + buyer.getName().getString() + "\u00a7a!"), false);

        removeOffer(offerId);
        return true;
    }

    @Nullable
    private static HappyGhastEntity findGhastEntity(MinecraftServer server, UUID ghastUuid) {
        for (ServerWorld world : server.getWorlds()) {
            Entity entity = world.getEntity(ghastUuid);
            if (entity instanceof HappyGhastEntity ghast) return ghast;
        }
        return null;
    }

    private static void cleanExpired() {
        long now = System.currentTimeMillis();
        PENDING_OFFERS.entrySet().removeIf(e -> now - e.getValue().createdAt() > OFFER_EXPIRY_MS);
    }

    private static String formatPrice(int emeralds, int diamonds) {
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

    private static void giveResources(ServerPlayerEntity player, int emeralds, int diamonds) {
        if (emeralds > 0) giveItem(player, Items.EMERALD, emeralds);
        if (diamonds > 0) giveItem(player, Items.DIAMOND, diamonds);
    }

    private static void giveItem(ServerPlayerEntity player, net.minecraft.item.Item item, int amount) {
        while (amount > 0) {
            int stack = Math.min(64, amount);
            ItemStack is = new ItemStack(item, stack);
            if (!player.getInventory().insertStack(is)) {
                player.dropItem(is, false);
            }
            amount -= stack;
        }
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
