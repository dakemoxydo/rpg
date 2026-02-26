package com.example.rpg.ability.magic.wind;

import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.stats.MagicElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.stats.RpgWorldData;
import org.lwjgl.glfw.GLFW;

public class GustAbility extends MagicAbility {

    public GustAbility() {
        super(new Builder("wind_gust", MagicElement.WIND)
                .tier(0)
                .branch(2)
                .maxLevel(5)
                .costPerLevel(3)
                .manaCost(15)
                .cooldown(5)
                .defaultKey(GLFW.GLFW_KEY_C));
    }

    @Override
    public float getPower(int level, com.example.rpg.stats.PlayerStatsData data) {
        float base = (float) (1.0 + (Math.max(1, level) * 0.3)); // Original calculation
        if (data != null && data.getElement() == com.example.rpg.stats.MagicElement.WIND) {
            base *= 1.2f;
        }
        return base;
    }

    @Override
    public int getCooldownSeconds(int level, com.example.rpg.stats.PlayerStatsData data) {
        int base = super.getCooldownSeconds(level, data); // Assuming superclass has this signature or default
                                                          // implementation
        if (data != null && data.getElement() == com.example.rpg.stats.MagicElement.WIND) {
            return Math.max(1, base - 1);
        }
        return base;
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
                com.example.rpg.config.RpgLocale.get("upgrade.wind_gust"),
                getPower(currentLevel, data), getPower(currentLevel + 1, data),
                getCooldownSeconds(currentLevel, data), getCooldownSeconds(currentLevel + 1, data));
    }

    @Override
    public void execute(ServerPlayerEntity player, int skillLevel) {
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d look = player.getRotationVector();
        Vec3d playerPos = player.getPos();

        // Частицы ветра
        for (int i = 0; i < 20 + skillLevel * 5; i++) {
            world.spawnParticles(
                    ParticleTypes.CLOUD,
                    playerPos.x + look.x * i * 0.3,
                    playerPos.y + 1 + look.y * i * 0.3,
                    playerPos.z + look.z * i * 0.3,
                    2, 0.3, 0.3, 0.3, 0.02);
        }

        // Отталкиваем врагов
        double range = 4 + skillLevel;
        PlayerStatsData data = RpgWorldData.get(player.getServer()).getPlayerData(player.getUuid());
        float pushPower = getPower(skillLevel, data);
        Box hitBox = new Box(playerPos, playerPos.add(look.multiply(range))).expand(2.0);

        world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class, hitBox,
                e -> e != player && e.isAlive()).forEach(entity -> {
                    Vec3d push = look.multiply(pushPower);
                    entity.setVelocity(entity.getVelocity().add(push.x, 0.3, push.z));
                    entity.velocityModified = true;
                    entity.damage(player.getDamageSources().magic(), 1 + skillLevel * 0.5f);
                });

        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PHANTOM_FLAP,
                SoundCategory.PLAYERS,
                1.0f, 1.5f);
    }

    @Override
    public String getIcon() {
        return "🌪";
    }
}