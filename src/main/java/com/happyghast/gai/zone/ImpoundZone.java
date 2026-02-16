package com.happyghast.gai.zone;

import com.happyghast.gai.config.GaiConfig;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ImpoundZone {

    public static boolean isPlayerInZone(ServerPlayerEntity player, GaiConfig config) {
        if (!config.isImpoundZoneConfigured()) return false;
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        String playerDim = world.getRegistryKey().getValue().toString();
        if (!playerDim.equals(config.getImpoundDimension())) return false;

        BlockPos c1 = config.getImpoundCorner1();
        BlockPos c2 = config.getImpoundCorner2();
        if (c1 == null || c2 == null) return false;

        Box zone = createZoneBox(c1, c2);
        return zone.contains(player.getX(), player.getY(), player.getZ());
    }

    public static Vec3d getZoneCenter(GaiConfig config) {
        BlockPos c1 = config.getImpoundCorner1();
        BlockPos c2 = config.getImpoundCorner2();
        if (c1 == null || c2 == null) return Vec3d.ZERO;
        return new Vec3d(
                (c1.getX() + c2.getX()) / 2.0,
                Math.min(c1.getY(), c2.getY()) + 1,
                (c1.getZ() + c2.getZ()) / 2.0
        );
    }

    public static boolean isEntityInZone(Entity entity, GaiConfig config, ServerWorld world) {
        if (!config.isImpoundZoneConfigured()) return false;
        String entityDim = world.getRegistryKey().getValue().toString();
        if (!entityDim.equals(config.getImpoundDimension())) return false;

        BlockPos c1 = config.getImpoundCorner1();
        BlockPos c2 = config.getImpoundCorner2();
        if (c1 == null || c2 == null) return false;

        Box zone = createZoneBox(c1, c2);
        return zone.contains(entity.getX(), entity.getY(), entity.getZ());
    }

    @Nullable
    public static ServerWorld getImpoundWorld(MinecraftServer server, GaiConfig config) {
        Identifier id = Identifier.tryParse(config.getImpoundDimension());
        if (id == null) return null;
        RegistryKey<net.minecraft.world.World> key = RegistryKey.of(RegistryKeys.WORLD, id);
        return server.getWorld(key);
    }

    private static Box createZoneBox(BlockPos c1, BlockPos c2) {
        return new Box(
                Math.min(c1.getX(), c2.getX()),
                Math.min(c1.getY(), c2.getY()),
                Math.min(c1.getZ(), c2.getZ()),
                Math.max(c1.getX(), c2.getX()) + 1,
                Math.max(c1.getY(), c2.getY()) + 1,
                Math.max(c1.getZ(), c2.getZ()) + 1
        );
    }
}
