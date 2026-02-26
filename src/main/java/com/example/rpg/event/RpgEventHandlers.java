package com.example.rpg.event;

import com.example.rpg.network.StatsNetworking;
import com.example.rpg.stats.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class RpgEventHandlers {

    // ==================== ORE DATA ====================

    // Храним ID источника для локализации на клиенте
    private record OreData(int xp, String sourceId, ItemStack drop) {
    }

    private static final Map<Block, OreData> ORE_DATA = new HashMap<>();

    static {
        // ID источников используются для локализации на клиенте
        registerOre(Blocks.COAL_ORE, 3, "coal", Items.COAL);
        registerOre(Blocks.DEEPSLATE_COAL_ORE, 3, "coal", Items.COAL);
        registerOre(Blocks.IRON_ORE, 5, "iron", Items.RAW_IRON);
        registerOre(Blocks.DEEPSLATE_IRON_ORE, 5, "iron", Items.RAW_IRON);
        registerOre(Blocks.COPPER_ORE, 4, "copper", Items.RAW_COPPER);
        registerOre(Blocks.DEEPSLATE_COPPER_ORE, 4, "copper", Items.RAW_COPPER);
        registerOre(Blocks.GOLD_ORE, 7, "gold", Items.RAW_GOLD);
        registerOre(Blocks.DEEPSLATE_GOLD_ORE, 7, "gold", Items.RAW_GOLD);
        registerOre(Blocks.LAPIS_ORE, 6, "lapis", Items.LAPIS_LAZULI);
        registerOre(Blocks.DEEPSLATE_LAPIS_ORE, 6, "lapis", Items.LAPIS_LAZULI);
        registerOre(Blocks.REDSTONE_ORE, 5, "redstone", Items.REDSTONE);
        registerOre(Blocks.DEEPSLATE_REDSTONE_ORE, 5, "redstone", Items.REDSTONE);
        registerOre(Blocks.DIAMOND_ORE, 15, "diamond", Items.DIAMOND);
        registerOre(Blocks.DEEPSLATE_DIAMOND_ORE, 15, "diamond", Items.DIAMOND);
        registerOre(Blocks.EMERALD_ORE, 20, "emerald", Items.EMERALD);
        registerOre(Blocks.DEEPSLATE_EMERALD_ORE, 20, "emerald", Items.EMERALD);
        registerOre(Blocks.NETHER_QUARTZ_ORE, 5, "quartz", Items.QUARTZ);
        registerOre(Blocks.NETHER_GOLD_ORE, 6, "nether_gold", Items.GOLD_NUGGET);
        registerOre(Blocks.ANCIENT_DEBRIS, 30, "ancient_debris", null);
    }

    private static void registerOre(Block block, int xp, String sourceId, net.minecraft.item.Item dropItem) {
        ORE_DATA.put(block, new OreData(xp, block.getTranslationKey(),
                dropItem != null ? new ItemStack(dropItem) : ItemStack.EMPTY));
    }

    // ==================== REGISTRATION ====================

    public static void register() {
        registerKillXp();
        registerMiningXp();
        registerFortuneBonus();
    }

    private static void registerKillXp() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (damageSource.getAttacker() instanceof ServerPlayerEntity player) {
                int xp = getKillXp(entity);
                if (xp > 0) {
                    String sourceId = entity.getType().getTranslationKey();
                    awardXp(player, xp, sourceId);
                }
            }
        });
    }

    private static void registerMiningXp() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                OreData data = ORE_DATA.get(state.getBlock());
                if (data != null) {
                    awardXp(serverPlayer, data.xp, data.sourceId);
                }
            }
        });
    }

    private static void registerFortuneBonus() {
        // Obsolete: Fortune stat removed
    }

    // ==================== XP LOGIC ====================

    private static int getKillXp(LivingEntity entity) {
        // Боссы
        if (entity instanceof EnderDragonEntity)
            return 500;
        if (entity instanceof WitherEntity)
            return 300;
        if (entity instanceof ElderGuardianEntity)
            return 100;
        if (entity instanceof WardenEntity)
            return 150;

        // Мини-боссы
        if (entity instanceof RavagerEntity)
            return 50;
        if (entity instanceof EvokerEntity)
            return 40;
        if (entity instanceof VindicatorEntity)
            return 25;
        if (entity instanceof WitherSkeletonEntity)
            return 20;
        if (entity instanceof BlazeEntity)
            return 15;
        if (entity instanceof EndermanEntity)
            return 15;
        if (entity instanceof GhastEntity)
            return 20;

        // Обычные враги
        if (entity instanceof HostileEntity)
            return 10;

        // Животные
        if (entity instanceof AnimalEntity animal) {
            return animal.isBaby() ? 0 : 3;
        }

        // Не даём XP за жителей
        if (entity instanceof VillagerEntity)
            return 0;

        return 5;
    }

    // ==================== PUBLIC API ====================

    public static void awardXp(ServerPlayerEntity player, int amount, String sourceId) {
        if (player.getServer() == null)
            return;

        RpgWorldData worldData = RpgWorldData.get(player.getServer());
        if (worldData == null)
            return;

        PlayerStatsData data = worldData.getPlayerData(player.getUuid());
        if (data == null)
            return;

        boolean leveledUp = data.addXp(amount);
        worldData.markDirty();

        // Отправляем ID источника - клиент сам переведёт
        StatsNetworking.sendXpPopup(player, amount, sourceId);

        if (leveledUp) {
            StatsManager.applyStats(player, data);
            StatsNetworking.sendLevelUpEffect(player, data.getCurrentLevel());
        }

        StatsNetworking.syncToClient(player);
    }

    public static void awardXp(ServerPlayerEntity player, int amount) {
        awardXp(player, amount, "rpg.xp.source.command");
    }
}