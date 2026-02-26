package com.example.rpg.ability.skills;

import com.example.rpg.ability.Ability;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import org.lwjgl.glfw.GLFW;

import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.stats.RpgWorldData;

public class HealAbility extends Ability {

    public HealAbility() {
        super(new Builder("heal")
                .maxLevel(5)
                .costPerLevel(3)
                .resourceCost(30)
                .cooldown(15)
                .usesMana()
                .defaultKey(GLFW.GLFW_KEY_H) // Дефолтный бинд: H
                .themeColor(0xFF55FF55) // Зелёный цвет лечения
        );
    }

    @Override
    public float getPower(int level, com.example.rpg.stats.PlayerStatsData data) {
        return super.getPower(level, data);
    }

    // getCooldownSeconds and getCooldownTicks are not present in the original code,
    // but the instruction implies they should be modified if they were.
    // Assuming they are inherited from Ability and need to be overridden with
    // PlayerStatsData.
    @Override
    public int getCooldownSeconds(int level, PlayerStatsData data) {
        return super.getCooldownSeconds(level, data);
    }

    @Override
    public int getCooldownTicks(int level, PlayerStatsData data) {
        return super.getCooldownTicks(level, data);
    }

    @Override
    public String getUpgradeDescription(int currentLevel, com.example.rpg.stats.PlayerStatsData data) {
        if (currentLevel == 0) {
            return com.example.rpg.config.RpgLocale.get("upgrade.unlock_general");
        }
        if (currentLevel >= getMaxLevel()) {
            return com.example.rpg.config.RpgLocale.get("upgrade.max_level");
        }
        // getUpgradeDescription gets PlayerStatsData to correctly preview scaling
        // values
        return String.format(com.example.rpg.config.RpgLocale.get("upgrade.heal"),
                getPower(currentLevel, data), getPower(currentLevel + 1, data),
                getCooldownSeconds(currentLevel, data), getCooldownSeconds(currentLevel + 1, data));
    }

    @Override
    public void execute(ServerPlayerEntity player, int level) {
        // У Healing нет снаряда/Raycast, он лечит сразу
        PlayerStatsData data = RpgWorldData.get(player.getServer()).getPlayerData(player.getUuid());
        float healAmount = getPower(level, data);

        player.heal(healAmount);

        ServerWorld world = (ServerWorld) player.getWorld();
        world.spawnParticles(
                ParticleTypes.HEART,
                player.getX(), player.getY() + 1, player.getZ(),
                5 + level * 2,
                0.5, 0.5, 0.5,
                0.1);

        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS,
                0.5f, 1.5f);
    }

    @Override
    public String getIcon() {
        return "♥";
    }
}