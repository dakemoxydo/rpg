package com.example.rpg.ability;

import com.example.rpg.network.StatsNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityCooldownManager {

    // UUID игрока -> (ID способности -> оставшееся КД в тиках)
    private static final Map<UUID, Map<String, Integer>> cooldowns = new HashMap<>();

    public static void setCooldown(UUID playerId, String abilityId, int ticks) {
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(abilityId, ticks);
    }

    public static int getCooldown(UUID playerId, String abilityId) {
        Map<String, Integer> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return 0;
        return playerCooldowns.getOrDefault(abilityId, 0);
    }

    public static boolean isOnCooldown(UUID playerId, String abilityId) {
        return getCooldown(playerId, abilityId) > 0;
    }

    public static float getCooldownProgress(UUID playerId, String abilityId) {
        Ability ability = AbilityRegistry.get(abilityId);
        if (ability == null) return 0;
        int remaining = getCooldown(playerId, abilityId);
        if (remaining <= 0) return 0;
        return (float) remaining / ability.getCooldownTicks();
    }

    public static void tick(UUID playerId) {
        Map<String, Integer> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return;

        playerCooldowns.replaceAll((k, v) -> Math.max(0, v - 1));
    }

    public static void clearPlayer(UUID playerId) {
        cooldowns.remove(playerId);
    }

    // Синхронизация всех активных кулдаунов игроку (при входе)
    public static void syncAllCooldowns(ServerPlayerEntity player) {
        Map<String, Integer> playerCooldowns = cooldowns.get(player.getUuid());
        if (playerCooldowns == null) return;

        for (Map.Entry<String, Integer> entry : playerCooldowns.entrySet()) {
            if (entry.getValue() > 0) {
                StatsNetworking.syncCooldown(player, entry.getKey(), entry.getValue());
            }
        }
    }
}