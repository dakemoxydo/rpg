package com.example.rpg.ability.skills;

import com.example.rpg.ability.Ability;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class DashAbility extends Ability {

    private static final double MAX_DASH_SPEED = 2.5;

    public DashAbility() {
        super(new Builder("dash")
                .maxLevel(3)
                .costPerLevel(2)
                .resourceCost(20)
                .cooldown(5)
                .usesStamina()
                .defaultKey(GLFW.GLFW_KEY_R) // Дефолтный бинд: R
                .themeColor(0xFFFFAA00) // Оранжевый цвет стамины
        );
    }

    @Override
    public float getPower(int level) {
        return (float) (1.5 + (Math.max(1, level) * 0.5));
    }

    @Override
    public String getUpgradeDescription(int currentLevel) {
        if (currentLevel == 0) {
            return com.example.rpg.config.RpgLocale.get("upgrade.unlock_ability")
                    + com.example.rpg.config.RpgLocale.getAbilityName(getId());
        }
        if (currentLevel >= getMaxLevel()) {
            return com.example.rpg.config.RpgLocale.get("upgrade.max_level");
        }
        return String.format(com.example.rpg.config.RpgLocale.get("upgrade.dash"),
                getPower(currentLevel), getPower(currentLevel + 1),
                getCooldownSeconds(currentLevel), getCooldownSeconds(currentLevel + 1));
    }

    @Override
    public void execute(ServerPlayerEntity player, int abilityLevel) {
        double dashPower = getPower(abilityLevel);

        Vec3d lookDirection = player.getRotationVector();
        Vec3d velocity = lookDirection.multiply(dashPower);

        double currentSpeed = velocity.horizontalLength();
        if (currentSpeed > MAX_DASH_SPEED) {
            double scale = MAX_DASH_SPEED / currentSpeed;
            velocity = new Vec3d(velocity.x * scale, velocity.y, velocity.z * scale);
        }

        player.setVelocity(velocity.x, Math.max(0.3, velocity.y * 0.5), velocity.z);
        player.velocityModified = true;

        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                0.5f, 1.5f);
    }

    @Override
    public String getIcon() {
        return "»";
    }
}