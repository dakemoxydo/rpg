package com.example.rpg.ability.magic.fire;

import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.stats.MagicElement;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.stats.RpgWorldData;
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
    public float getPower(int level, com.example.rpg.stats.PlayerStatsData data) {
        return (float) (1.0 + (Math.max(1, level) * 0.2));
    }

    @Override
    public String getUpgradeDescription(int currentLevel, com.example.rpg.stats.PlayerStatsData data) {
        if (currentLevel == 0) {
            return com.example.rpg.config.RpgLocale.get("upgrade.unlock_general");
        }
        if (currentLevel >= getMaxLevel()) {
            return com.example.rpg.config.RpgLocale.get("upgrade.max_level");
        }
        // fire_fireball / water_bolt / earth_rock_throw share a similar text structure
        return String.format(
                com.example.rpg.config.RpgLocale.get("upgrade.rock_fire_water"),
                getPower(currentLevel, data), getPower(currentLevel + 1, data),
                getCooldownSeconds(currentLevel, data), getCooldownSeconds(currentLevel + 1, data));
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

        PlayerStatsData data = RpgWorldData.get(player.getServer()).getPlayerData(player.getUuid());
        // Note: SmallFireballEntity does not have a direct setDamage method.
        // If you intend to modify its damage, you might need a custom entity or a
        // different approach.
        // For now, assuming 'projectile' was a placeholder for 'fireball' and
        // 'setDamage' is a conceptual method.
        // If SmallFireballEntity is extended or wrapped, this line might become valid.
        // fireball.setDamage(getPower(skillLevel, data)); // This line is commented out
        // as SmallFireballEntity doesn't have setDamage

        double speed = getPower(skillLevel, data); // Use the overloaded getPower with data
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