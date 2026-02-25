package com.example.rpg.ability.magic;

import com.example.rpg.stats.MagicElement;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lwjgl.glfw.GLFW;

public abstract class MagicAbility {

    protected final String id;
    protected final MagicElement element;
    protected final int tier;
    protected final int branch;
    protected final int maxLevel;
    protected final int costPerLevel;
    protected final int manaCost;
    protected final int cooldownSeconds;
    protected final String[] requiredSkills;
    protected final int defaultKey; // НОВОЕ ПОЛЕ

    public MagicAbility(Builder builder) {
        this.id = builder.id;
        this.element = builder.element;
        this.tier = builder.tier;
        this.branch = builder.branch;
        this.maxLevel = builder.maxLevel;
        this.costPerLevel = builder.costPerLevel;
        this.manaCost = builder.manaCost;
        this.cooldownSeconds = builder.cooldownSeconds;
        this.requiredSkills = builder.requiredSkills;
        this.defaultKey = builder.defaultKey;
    }

    public abstract void execute(ServerPlayerEntity player, int skillLevel);
    public abstract String getIcon();

    public String getId() { return id; }
    public MagicElement getElement() { return element; }
    public int getTier() { return tier; }
    public int getBranch() { return branch; }
    public int getMaxLevel() { return maxLevel; }
    public int getCostPerLevel() { return costPerLevel; }
    public int getManaCost() { return manaCost; }
    public int getManaCost(int level) { return manaCost + (level - 1) * 5; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public int getCooldownTicks() { return cooldownSeconds * 20; }
    public String[] getRequiredSkills() { return requiredSkills; }
    public int getDefaultKey() { return defaultKey; }

    public static class Builder {
        private final String id;
        private final MagicElement element;
        private int tier = 0;
        private int branch = 0;
        private int maxLevel = 3;
        private int costPerLevel = 2;
        private int manaCost = 20;
        private int cooldownSeconds = 5;
        private String[] requiredSkills = new String[0];
        private int defaultKey = GLFW.GLFW_KEY_UNKNOWN;

        public Builder(String id, MagicElement element) { this.id = id; this.element = element; }
        public Builder tier(int v) { tier = v; return this; }
        public Builder branch(int v) { branch = v; return this; }
        public Builder maxLevel(int v) { maxLevel = v; return this; }
        public Builder costPerLevel(int v) { costPerLevel = v; return this; }
        public Builder manaCost(int v) { manaCost = v; return this; }
        public Builder cooldown(int v) { cooldownSeconds = v; return this; }
        public Builder requires(String... v) { requiredSkills = v; return this; }
        public Builder defaultKey(int key) { defaultKey = key; return this; } // НОВЫЙ МЕТОД
    }
}