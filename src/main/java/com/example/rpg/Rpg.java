package com.example.rpg;

import com.example.rpg.ability.AbilityCooldownManager;
import com.example.rpg.ability.AbilityRegistry;
import com.example.rpg.ability.MagicSkillRegistry;
import com.example.rpg.command.RpgCommands;
import com.example.rpg.event.RpgEventHandlers;  // Изменено
import com.example.rpg.network.StatsNetworking;
import com.example.rpg.stats.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rpg implements ModInitializer {

    public static final String MOD_ID = "rpg";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static int regenTickCounter = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("RPG Mod loading...");

        AbilityRegistry.init();
        MagicSkillRegistry.init();
        StatsNetworking.registerServerHandlers();
        RpgEventHandlers.register();  // Изменено - теперь один вызов вместо двух
        RpgCommands.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            RpgWorldData worldData = RpgWorldData.get(server);
            PlayerStatsData data = worldData.getPlayerData(player.getUuid());
            StatsManager.applyStats(player, data);
            StatsNetworking.syncToClient(player);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            RpgWorldData worldData = RpgWorldData.get(newPlayer.getServer());
            PlayerStatsData data = worldData.getPlayerData(newPlayer.getUuid());
            data.setCurrentMana(data.getMaxMana());
            data.setCurrentStamina(data.getMaxStamina());
            worldData.markDirty();
            StatsManager.applyStats(newPlayer, data);
            StatsNetworking.syncToClient(newPlayer);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            regenTickCounter++;
            for (var player : server.getPlayerManager().getPlayerList()) {
                RpgWorldData worldData = RpgWorldData.get(server);
                PlayerStatsData data = worldData.getPlayerData(player.getUuid());

                if (regenTickCounter % 10 == 0) {
                    boolean changed = false;
                    if (data.getCurrentMana() < data.getMaxMana()) { data.regenMana(0.5f); changed = true; }
                    if (data.getCurrentStamina() < data.getMaxStamina()) { data.regenStamina(0.5f); changed = true; }
                    if (changed) { worldData.markDirty(); StatsNetworking.syncToClient(player); }
                }
                AbilityCooldownManager.tick(player.getUuid());
            }
            if (regenTickCounter >= 1000) regenTickCounter = 0;
        });

        LOGGER.info("RPG Mod loaded!");
    }
}