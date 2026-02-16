package com.happyghast.gai.gui;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages exclusive edit locks on ghasts.
 * Only one editor (owner or admin) at a time. Viewers (read-only) don't need a lock.
 * Admin can forcibly take the lock from an owner.
 */
public class GhastMenuLock {

    private static final Map<UUID, EditSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    public record EditSession(UUID playerUuid, boolean isAdmin) {}

    /**
     * Try to acquire an edit lock on a ghast.
     * @return true if lock was acquired, false if denied.
     */
    public static boolean tryLock(UUID ghastUuid, ServerPlayerEntity player, boolean isAdmin,
                                   @Nullable HappyGhastEntity ghast, @Nullable MinecraftServer server) {
        EditSession existing = ACTIVE_SESSIONS.get(ghastUuid);

        if (existing != null) {
            if (existing.playerUuid().equals(player.getUuid())) {
                return true;
            }

            if (isAdmin) {
                if (server != null) {
                    ServerPlayerEntity currentHolder = server.getPlayerManager().getPlayer(existing.playerUuid());
                    if (currentHolder != null) {
                        currentHolder.closeHandledScreen();
                        currentHolder.sendMessage(Text.literal(
                                "\u00a7c[\u0413\u0410\u0418] \u0410\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440 \u0437\u0430\u043D\u044F\u043B \u043C\u0435\u043D\u044E \u0432\u0430\u0448\u0435\u0433\u043E \u0433\u0430\u0441\u0442\u0430."), false);
                    }
                }
            } else {
                player.sendMessage(Text.literal(
                        "\u00a7c[\u0413\u0410\u0418] \u0413\u0430\u0441\u0442 \u0441\u0435\u0439\u0447\u0430\u0441 \u0440\u0435\u0434\u0430\u043A\u0442\u0438\u0440\u0443\u0435\u0442\u0441\u044F \u0434\u0440\u0443\u0433\u0438\u043C \u0438\u0433\u0440\u043E\u043A\u043E\u043C!"), false);
                return false;
            }
        }

        ACTIVE_SESSIONS.put(ghastUuid, new EditSession(player.getUuid(), isAdmin));

        if (ghast != null) {
            freezeGhast(ghast);
        }

        return true;
    }

    /**
     * Release the edit lock on a ghast.
     */
    public static void unlock(UUID ghastUuid, UUID playerUuid, @Nullable MinecraftServer server) {
        EditSession session = ACTIVE_SESSIONS.get(ghastUuid);
        if (session != null && session.playerUuid().equals(playerUuid)) {
            ACTIVE_SESSIONS.remove(ghastUuid);

            if (server != null) {
                unfreezeGhastByUuid(server, ghastUuid);
            }
        }
    }

    /**
     * Force-release a lock (e.g. player disconnect).
     */
    public static void forceUnlock(UUID ghastUuid, @Nullable MinecraftServer server) {
        ACTIVE_SESSIONS.remove(ghastUuid);
        if (server != null) {
            unfreezeGhastByUuid(server, ghastUuid);
        }
    }

    /**
     * Check if a ghast is currently locked for editing.
     */
    public static boolean isLocked(UUID ghastUuid) {
        return ACTIVE_SESSIONS.containsKey(ghastUuid);
    }

    /**
     * Check if a specific player holds the lock.
     */
    public static boolean isLockedBy(UUID ghastUuid, UUID playerUuid) {
        EditSession session = ACTIVE_SESSIONS.get(ghastUuid);
        return session != null && session.playerUuid().equals(playerUuid);
    }

    /**
     * Clean up sessions for a disconnecting player.
     */
    public static void onPlayerDisconnect(UUID playerUuid, @Nullable MinecraftServer server) {
        ACTIVE_SESSIONS.entrySet().removeIf(entry -> {
            if (entry.getValue().playerUuid().equals(playerUuid)) {
                if (server != null) {
                    unfreezeGhastByUuid(server, entry.getKey());
                }
                return true;
            }
            return false;
        });
    }

    private static void freezeGhast(HappyGhastEntity ghast) {
        if (ghast instanceof MobEntity mob) {
            mob.setAiDisabled(true);
        }
    }

    private static void unfreezeGhastByUuid(MinecraftServer server, UUID ghastUuid) {
        for (ServerWorld world : server.getWorlds()) {
            var entity = world.getEntity(ghastUuid);
            if (entity instanceof HappyGhastEntity ghast && ghast instanceof MobEntity mob) {
                mob.setAiDisabled(false);
                return;
            }
        }
    }
}
