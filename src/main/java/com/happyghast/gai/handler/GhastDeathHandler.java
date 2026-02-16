package com.happyghast.gai.handler;

import com.happyghast.gai.HappyGhastGaiMod;
import com.happyghast.gai.config.GaiConfig;
import com.happyghast.gai.data.GhastRegistryState;
import com.happyghast.gai.data.GhastVehicleData;
import com.happyghast.gai.plate.PlateDisplayManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class GhastDeathHandler {

    public static void onAfterDeath(LivingEntity entity, DamageSource damageSource) {
        if (!(entity instanceof HappyGhastEntity ghast)) return;
        if (!(entity.getEntityWorld() instanceof ServerWorld world)) return;

        MinecraftServer server = world.getServer();
        GhastRegistryState state = GhastRegistryState.get(server);
        GhastVehicleData data = state.getGhast(ghast.getUuid());
        if (data == null) return;

        GaiConfig config = state.getConfig();
        String ghastName = data.getCustomName().isEmpty() ? "Гаст" : data.getCustomName();
        String plate = data.getPlateNumber().isEmpty() ? "" : " [" + data.getPlateNumber() + "]";

        PlateDisplayManager.removePlates(world, data);

        if (data.isRegistered() && data.getOwnerUuid() != null) {
            int totalEmeralds = config.getRegistrationCostEmeralds();
            int totalDiamonds = config.getRegistrationCostDiamonds();

            if (data.getMaxStage() >= 2) {
                totalEmeralds += config.getStage2CostEmeralds();
                totalDiamonds += config.getStage2CostDiamonds();
            }
            if (data.getMaxStage() >= 3) {
                totalEmeralds += config.getStage3CostEmeralds();
                totalDiamonds += config.getStage3CostDiamonds();
            }

            int refundEmeralds = Math.max(1, (int) Math.ceil(totalEmeralds * 0.15));
            int refundDiamonds = Math.max(1, (int) Math.ceil(totalDiamonds * 0.15));

            ServerPlayerEntity owner = server.getPlayerManager().getPlayer(data.getOwnerUuid());
            if (owner != null) {
                owner.sendMessage(Text.literal(""), false);
                owner.sendMessage(Text.literal("§c§l☠ §4§lГАИ | Соболезнования §c§l☠"), false);
                owner.sendMessage(Text.literal(""), false);
                owner.sendMessage(Text.literal("§7Ваш верный гаст §f" + ghastName + plate + " §7погиб..."), false);
                owner.sendMessage(Text.literal("§7Он был хорошим другом и надёжным транспортом."), false);
                owner.sendMessage(Text.literal("§7Покойся с миром, маленький призрак. §c❤"), false);
                owner.sendMessage(Text.literal(""), false);
                owner.sendMessage(Text.literal("§eСтраховая компенсация (15%):"), false);

                StringBuilder refundInfo = new StringBuilder("§7  ");
                if (refundEmeralds > 0) refundInfo.append("§a").append(refundEmeralds).append(" изум. ");
                if (refundDiamonds > 0) refundInfo.append("§b").append(refundDiamonds).append(" алм.");
                owner.sendMessage(Text.literal(refundInfo.toString()), false);
                owner.sendMessage(Text.literal(""), false);

                giveItems(owner, Items.EMERALD, refundEmeralds);
                giveItems(owner, Items.DIAMOND, refundDiamonds);
            }

            HappyGhastGaiMod.LOGGER.info("[GAI] Ghast {} ({}) died. Owner: {}. Refund: {} emeralds, {} diamonds.",
                    data.getGhastUuid(), ghastName, data.getOwnerUuid(), refundEmeralds, refundDiamonds);
        } else {
            HappyGhastGaiMod.LOGGER.info("[GAI] Unregistered ghast {} died. Removing from database.",
                    data.getGhastUuid());
        }

        state.removeGhast(ghast.getUuid());
    }

    private static void giveItems(ServerPlayerEntity player, net.minecraft.item.Item item, int amount) {
        while (amount > 0) {
            int stackSize = Math.min(amount, 64);
            ItemStack stack = new ItemStack(item, stackSize);
            if (!player.getInventory().insertStack(stack)) {
                player.dropItem(stack, false);
            }
            amount -= stackSize;
        }
    }
}
