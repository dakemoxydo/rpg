package com.example.rpg.ability.magic.lightning;

import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.stats.MagicElement;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

public class ShockAbility extends MagicAbility {

    public ShockAbility() {
        super(new Builder("lightning_shock", MagicElement.LIGHTNING)
                .tier(0)
                .branch(2)
                .maxLevel(5)
                .costPerLevel(4)
                .manaCost(35)
                .cooldown(8)
                .defaultKey(GLFW.GLFW_KEY_C));
    }

    @Override
    public float getPower(int level, com.example.rpg.stats.PlayerStatsData data) {
        return level < 3 ? 0.0f : 5.0f; // Vanilla lightning deals 5 damage
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
        Vec3d eyePos = player.getEyePos();

        // Рейкаст для нахождения точки удара
        double range = 15 + skillLevel * 5;
        Vec3d endPos = eyePos.add(look.multiply(range));

        BlockHitResult hitResult = world.raycast(new RaycastContext(
                eyePos, endPos, RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE, player));

        Vec3d strikePos;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            strikePos = hitResult.getPos();
        } else {
            strikePos = endPos;
        }

        // Спавним молнию
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            lightning.setPosition(strikePos);
            lightning.setCosmetic(skillLevel < 3); // Реальный урон на высоком уровне
            world.spawnEntity(lightning);
        }

        // Электрические частицы
        world.spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                strikePos.x, strikePos.y, strikePos.z,
                20 + skillLevel * 10,
                1.0, 1.0, 1.0, 0.5);

        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                SoundCategory.PLAYERS,
                0.5f, 1.5f);
    }

    @Override
    public String getIcon() {
        return "⚡";
    }
}