package com.example.rpg.stats;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RpgWorldData extends PersistentState {

    private static final Logger LOGGER = LoggerFactory.getLogger("rpg");
    private final Map<UUID, PlayerStatsData> playerDataMap = new HashMap<>();

    public PlayerStatsData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, id -> new PlayerStatsData());
    }

    public void setPlayerData(UUID uuid, PlayerStatsData data) {
        playerDataMap.put(uuid, data);
        markDirty();
    }

    // --- Сохранение ---

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        for (Map.Entry<UUID, PlayerStatsData> entry : playerDataMap.entrySet()) {
            playersNbt.put(entry.getKey().toString(), entry.getValue().toNbt());
        }
        nbt.put("players", playersNbt);
        return nbt;
    }

    // --- Загрузка ---

    public static RpgWorldData fromNbt(NbtCompound nbt) {
        RpgWorldData data = new RpgWorldData();
        NbtCompound playersNbt = nbt.getCompound("players");
        for (String key : playersNbt.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                PlayerStatsData playerData = PlayerStatsData.fromNbt(playersNbt.getCompound(key));
                data.playerDataMap.put(uuid, playerData);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid UUID in RPG data: {}", key);
            }
        }
        return data;
    }

    // --- Получение из мира ---

    public static RpgWorldData get(MinecraftServer server) {
        if (server == null) {
            LOGGER.error("Attempted to get RpgWorldData with null server!");
            return new RpgWorldData();
        }

        ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world == null) {
            LOGGER.error("Overworld is null, cannot get RpgWorldData!");
            return new RpgWorldData();
        }

        return world.getPersistentStateManager().getOrCreate(
                RpgWorldData::fromNbt,
                RpgWorldData::new,
                "rpg_stats_data"
        );
    }
}