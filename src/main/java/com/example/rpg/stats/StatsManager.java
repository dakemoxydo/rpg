package com.example.rpg.stats;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class StatsManager {

    private static final UUID HEALTH_MODIFIER_UUID =
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SPEED_MODIFIER_UUID =
            UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID STRENGTH_MODIFIER_UUID =
            UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");

    public static void applyStats(ServerPlayerEntity player, PlayerStatsData data) {
        applyHealth(player, data);
        applySpeed(player, data);
        applyStrength(player, data);
        applyJump(player, data);
    }

    private static void applyHealth(ServerPlayerEntity player, PlayerStatsData data) {
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (attr == null) return;

        // Удаляем старый модификатор
        attr.removeModifier(HEALTH_MODIFIER_UUID);

        int level = data.getStatLevel(StatType.HEALTH);
        if (level > 0) {
            double bonus = StatType.HEALTH.getValueAtLevel(level);
            attr.addPersistentModifier(new EntityAttributeModifier(
                    HEALTH_MODIFIER_UUID, "rpg_health_bonus",
                    bonus, EntityAttributeModifier.Operation.ADDITION));
        }

        // Всегда клампим текущее здоровье к максимуму
        float maxHealth = player.getMaxHealth();
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private static void applySpeed(ServerPlayerEntity player, PlayerStatsData data) {
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (attr == null) return;

        attr.removeModifier(SPEED_MODIFIER_UUID);

        int level = data.getStatLevel(StatType.SPEED);
        if (level > 0) {
            double bonus = StatType.SPEED.getValueAtLevel(level);
            attr.addPersistentModifier(new EntityAttributeModifier(
                    SPEED_MODIFIER_UUID, "rpg_speed_bonus",
                    bonus, EntityAttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    private static void applyStrength(ServerPlayerEntity player, PlayerStatsData data) {
        EntityAttributeInstance attr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attr == null) return;

        attr.removeModifier(STRENGTH_MODIFIER_UUID);

        int level = data.getStatLevel(StatType.STRENGTH);
        if (level > 0) {
            double bonus = StatType.STRENGTH.getValueAtLevel(level);
            attr.addPersistentModifier(new EntityAttributeModifier(
                    STRENGTH_MODIFIER_UUID, "rpg_strength_bonus",
                    bonus, EntityAttributeModifier.Operation.ADDITION));
        }
    }

    private static void applyJump(ServerPlayerEntity player, PlayerStatsData data) {
        int level = data.getStatLevel(StatType.JUMP);

        // Убираем старый эффект
        player.removeStatusEffect(StatusEffects.JUMP_BOOST);

        if (level > 0) {
            // Jump Boost с бесконечной длительностью
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.JUMP_BOOST,
                    StatusEffectInstance.INFINITE,
                    level - 1, // Уровень эффекта (0 = Jump Boost I)
                    false,  // ambient
                    false,  // show particles
                    true    // show icon
            ));
        }
    }

    // Метод для полного переприменения после респавна
    public static void reapplyAllStats(ServerPlayerEntity player, PlayerStatsData data) {
        // Удаляем Jump Boost отдельно, так как applyJump сначала удаляет его
        player.removeStatusEffect(StatusEffects.JUMP_BOOST);

        // Применяем все статы (внутри каждого метода есть removeModifier)
        applyStats(player, data);
    }
}