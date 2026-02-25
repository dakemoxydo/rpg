package com.example.rpg.ability;

import net.minecraft.server.network.ServerPlayerEntity;
import org.lwjgl.glfw.GLFW;

public abstract class Ability {

    protected final String id;
    protected final int maxLevel;
    protected final int costPerLevel;
    protected final int resourceCost;
    protected final int cooldownSeconds;
    protected final boolean usesStamina;
    protected final int defaultKey; // НОВОЕ ПОЛЕ

    public Ability(Builder builder) {
        this.id = builder.id;
        this.maxLevel = builder.maxLevel;
        this.costPerLevel = builder.costPerLevel;
        this.resourceCost = builder.resourceCost;
        this.cooldownSeconds = builder.cooldownSeconds;
        this.usesStamina = builder.usesStamina;
        this.defaultKey = builder.defaultKey;
    }

    public abstract void execute(ServerPlayerEntity player, int abilityLevel);
    public abstract String getIcon();

    public String getId() { return id; }
    public int getMaxLevel() { return maxLevel; }
    public int getCostPerLevel() { return costPerLevel; }
    public int getResourceCost() { return resourceCost; }
    public int getResourceCost(int level) { return resourceCost + (level - 1) * 5; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public int getCooldownTicks() { return cooldownSeconds * 20; }
    public boolean usesStamina() { return usesStamina; }
    public int getDefaultKey() { return defaultKey; }

    public static class Builder {
        private final String id;
        private int maxLevel = 3;
        private int costPerLevel = 2;
        private int resourceCost = 20;
        private int cooldownSeconds = 5;
        private boolean usesStamina = false;
        private int defaultKey = GLFW.GLFW_KEY_UNKNOWN;

        public Builder(String id) { this.id = id; }
        public Builder maxLevel(int v) { maxLevel = v; return this; }
        public Builder costPerLevel(int v) { costPerLevel = v; return this; }
        public Builder resourceCost(int v) { resourceCost = v; return this; }
        public Builder cooldown(int v) { cooldownSeconds = v; return this; }
        public Builder usesStamina() { usesStamina = true; return this; }
        public Builder usesMana() { usesStamina = false; return this; }
        public Builder defaultKey(int key) { defaultKey = key; return this; } // НОВЫЙ МЕТОД
    }
}