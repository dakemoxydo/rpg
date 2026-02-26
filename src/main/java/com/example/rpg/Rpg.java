package com.example.rpg;

import com.example.rpg.ability.AbilityCooldownManager;
import com.example.rpg.ability.AbilityRegistry;
import com.example.rpg.command.RpgCommands;
import com.example.rpg.event.RpgEventHandlers;
import com.example.rpg.network.StatsNetworking;
import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.stats.RpgWorldData;
import com.example.rpg.stats.StatsManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rpg implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("rpg");

    @Override
    public void onInitialize() {
        LOGGER.info("RPG Stats Mod initializing...");

        // Единый реестр способностей (грузит static-блок)
        AbilityRegistry.init();

        // Регистрация сетевых обработчиков
        StatsNetworking.registerServerHandlers();

        // Команды
        RpgCommands.register();

        // Обработчики событий (XP за убийства, добычу)
        RpgEventHandlers.register();

        // Подключение игрока: загрузка данных, применение статов
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            RpgWorldData worldData = RpgWorldData.get(server);
            PlayerStatsData data = worldData.getPlayerData(player.getUuid());

            StatsManager.applyStats(player, data);
            StatsNetworking.syncToClient(player);
            StatsNetworking.syncResources(player);
            AbilityCooldownManager.syncAllCooldowns(player);
        });

        // Серверный тик: регенерация ресурсов, тик кулдаунов
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                RpgWorldData worldData = RpgWorldData.get(server);
                PlayerStatsData data = worldData.getPlayerData(player.getUuid());

                // Регенерация маны и стамины каждый тик (1/20 секунды)
                data.regenMana(0.05f);
                data.regenStamina(0.05f);

                // Тик кулдаунов
                AbilityCooldownManager.tick(player.getUuid());

                // Синхронизация ресурсов каждую секунду
                if (server.getTicks() % 20 == 0) {
                    StatsNetworking.syncResources(player);
                }
            }
        });

        LOGGER.info("RPG Stats Mod initialized!");
    }
}