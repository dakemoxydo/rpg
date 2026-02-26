package com.example.rpg.ability.magic.fire;

import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.stats.MagicElement;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class FireballAbility extends MagicAbility {

    public FireballAbility() {
        super(new Builder("fire_fireball", MagicElement.FIRE)
                .tier(0)
                .branch(2)
                .maxLevel(5)
                .costPerLevel(3)
                .manaCost(25)
                .cooldown(3)
                .defaultKey(GLFW.GLFW_KEY_C) // Дефолтный бинд: C
        );
    }

    @Override
    public float getPower(int level) {
        return (float) (1.0 + (Math.max(1, level) * 0.2));
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
        return String.format(com.example.rpg.config.RpgLocale.get("upgrade.fireball"),
                getPower(currentLevel), getPower(currentLevel + 1),
                getCooldownSeconds(currentLevel), getCooldownSeconds(currentLevel + 1));
    }

    @Override
    public void execute(ServerPlayerEntity player, int skillLevel) {
        Vec3d look = player.getRotationVector();
        Vec3d pos = player.getEyePos();

        SmallFireballEntity fireball = new SmallFireballEntity(
                player.getWorld(),
                player,
                look.x, look.y, look.z);

        fireball.setPosition(pos.x + look.x * 0.5, pos.y, pos.z + look.z * 0.5);

        double speed = getPower(skillLevel);
        fireball.setVelocity(look.x * speed, look.y * speed, look.z * speed);

        player.getWorld().spawnEntity(fireball);

        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_BLAZE_SHOOT,
                SoundCategory.PLAYERS,
                1.0f, 1.0f + (skillLevel * 0.1f));
    }

    @Override
    public String getIcon() {
        return "🔥";
    }
}