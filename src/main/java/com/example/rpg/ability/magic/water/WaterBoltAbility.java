package com.example.rpg.ability.magic.water;

import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.stats.MagicElement;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class WaterBoltAbility extends MagicAbility {

    public WaterBoltAbility() {
        super(new Builder("water_bolt", MagicElement.WATER)
                .tier(0)
                .branch(2)
                .maxLevel(5)
                .costPerLevel(3)
                .manaCost(20)
                .cooldown(4)
                .defaultKey(GLFW.GLFW_KEY_C));
    }

    @Override
    public float getPower(int level) {
        return 2.0f + Math.max(1, level);
    }

    @Override
    public String getUpgradeDescription(int currentLevel) {
        if (currentLevel == 0) {
            return com.example.rpg.config.RpgLocale.get("upgrade.unlock_ability")
                    + com.example.rpg.config.RpgLocale.getSkillName(getId());
        }
        if (currentLevel >= getMaxLevel()) {
            return com.example.rpg.config.RpgLocale.get("upgrade.max_level");
        }
        return String.format(com.example.rpg.config.RpgLocale.get("upgrade.water_bolt"),
                getPower(currentLevel), getPower(currentLevel + 1),
                getCooldownSeconds(currentLevel), getCooldownSeconds(currentLevel + 1));
    }

    @Override
    public void execute(ServerPlayerEntity player, int skillLevel) {
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d look = player.getRotationVector();
        Vec3d pos = player.getEyePos();

        // Визуальный луч воды
        for (int i = 0; i < 10 + skillLevel * 2; i++) {
            double distance = i * 0.5;
            world.spawnParticles(
                    ParticleTypes.SPLASH,
                    pos.x + look.x * distance,
                    pos.y + look.y * distance,
                    pos.z + look.z * distance,
                    3, 0.1, 0.1, 0.1, 0.05);
        }

        // Урон и замедление врагов в линии
        double range = 5 + skillLevel;
        Vec3d endPos = pos.add(look.multiply(range));
        Box hitBox = new Box(pos, endPos).expand(1.0);

        world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, hitBox,
                e -> e != player && e.isAlive()).forEach(entity -> {
                    entity.damage(player.getDamageSources().magic(), getPower(skillLevel));
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40 + skillLevel * 10, 0));
                });

        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_SPLASH,
                SoundCategory.PLAYERS,
                1.0f, 1.2f);
    }

    @Override
    public String getIcon() {
        return "💧";
    }
}