package com.example.rpg.ability.magic.earth;

import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.stats.MagicElement;
import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.stats.RpgWorldData;
import net.minecraft.block.Blocks;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class RockThrowAbility extends MagicAbility {

    public RockThrowAbility() {
        super(new Builder("earth_rock_throw", MagicElement.EARTH)
                .tier(0)
                .branch(2)
                .maxLevel(5)
                .costPerLevel(3)
                .manaCost(20)
                .cooldown(4)
                .defaultKey(GLFW.GLFW_KEY_C));
    }

    @Override
    public float getPower(int level, com.example.rpg.stats.PlayerStatsData data) {
        return 10.0f + (Math.max(1, level) * 5.0f);
    }

    @Override
    public String getUpgradeDescription(int currentLevel, com.example.rpg.stats.PlayerStatsData data) {
        if (currentLevel == 0) {
            return com.example.rpg.config.RpgLocale.get("upgrade.unlock_general");
        }
        if (currentLevel >= getMaxLevel()) {
            return com.example.rpg.config.RpgLocale.get("upgrade.max_level");
        }
        return String.format(
                com.example.rpg.config.RpgLocale.get("upgrade.rock_fire_water"),
                getPower(currentLevel, data), getPower(currentLevel + 1, data),
                getCooldownSeconds(currentLevel, data), getCooldownSeconds(currentLevel + 1, data));
    }

    @Override
    public void execute(ServerPlayerEntity player, int skillLevel) {
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d look = player.getRotationVector();
        Vec3d pos = player.getEyePos();

        // Создаём падающий блок как снаряд
        FallingBlockEntity rock = FallingBlockEntity.spawnFromBlock(
                world,
                player.getBlockPos().up(2),
                Blocks.COBBLESTONE.getDefaultState());

        if (rock != null) {
            double speed = 1.5 + skillLevel * 0.3;
            rock.setVelocity(look.x * speed, look.y * speed + 0.2, look.z * speed);
            rock.dropItem = false;

            // ИСПРАВЛЕНО: Аргументы (урон за блок падения, максимальный урон)
            // 2.0f урона за блок, макс урон зависит от уровня скилла
            PlayerStatsData data = RpgWorldData.get(player.getServer()).getPlayerData(player.getUuid());
            rock.setHurtEntities(2.0f, (int) getPower(skillLevel, data));
        }

        // Частицы земли
        world.spawnParticles(
                new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.DIRT.getDefaultState()),
                pos.x, pos.y - 0.5, pos.z,
                15 + skillLevel * 5,
                0.5, 0.3, 0.5, 0.1);

        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_IRON_GOLEM_ATTACK,
                SoundCategory.PLAYERS,
                1.0f, 0.8f);
    }

    @Override
    public String getIcon() {
        return "🪨";
    }
}