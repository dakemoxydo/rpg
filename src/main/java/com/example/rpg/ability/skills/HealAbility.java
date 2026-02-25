package com.example.rpg.ability.skills;

import com.example.rpg.ability.Ability;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import org.lwjgl.glfw.GLFW;

public class HealAbility extends Ability {

    public HealAbility() {
        super(new Builder("heal")
                .maxLevel(5)
                .costPerLevel(3)
                .resourceCost(30)
                .cooldown(15)
                .usesMana()
                .defaultKey(GLFW.GLFW_KEY_H) // Дефолтный бинд: H
        );
    }

    @Override
    public void execute(ServerPlayerEntity player, int abilityLevel) {
        float healAmount = 4.0f + (abilityLevel * 2.0f);
        player.heal(healAmount);

        ServerWorld world = (ServerWorld) player.getWorld();
        world.spawnParticles(
                ParticleTypes.HEART,
                player.getX(), player.getY() + 1, player.getZ(),
                5 + abilityLevel * 2,
                0.5, 0.5, 0.5,
                0.1
        );

        player.getWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS,
                0.5f, 1.5f
        );
    }

    @Override
    public String getIcon() {
        return "♥";
    }
}