package com.example.rpg.ability;

import org.lwjgl.glfw.GLFW;

/**
 * Базовый абстрактный класс для всех способностей (физических и магических).
 * Содержит общие поля и Builder для конфигурации.
 * Конкретные способности наследуют этот класс и реализуют execute() и
 * getIcon().
 */
public abstract class AbstractAbility implements IAbility {

    protected final String id;
    protected final int maxLevel;
    protected final int costPerLevel;
    protected final int resourceCost;
    protected final int cooldownSeconds;
    protected final boolean usesStamina;
    protected final int defaultKey;
    protected final int themeColor;

    protected AbstractAbility(Builder<?> builder) {
        this.id = builder.id;
        this.maxLevel = builder.maxLevel;
        this.costPerLevel = builder.costPerLevel;
        this.resourceCost = builder.resourceCost;
        this.cooldownSeconds = builder.cooldownSeconds;
        this.usesStamina = builder.usesStamina;
        this.defaultKey = builder.defaultKey;
        this.themeColor = builder.themeColor;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getCostPerLevel() {
        return costPerLevel;
    }

    @Override
    public int getResourceCost(int level) {
        return resourceCost + (level - 1) * 5;
    }

    public int getResourceCost() {
        return resourceCost;
    }

    @Override
    public int getCooldownSeconds(int level) {
        // По умолчанию кулдаун снижается на 0.5с за каждый уровень после первого
        return Math.max(1, cooldownSeconds - (int) ((level - 1) * 0.5));
    }

    @Override
    public int getCooldownTicks(int level) {
        return getCooldownSeconds(level) * 20;
    }

    @Override
    public float getPower(int level) {
        // Базовая логика для силы, если дочерний класс не переопределил.
        // Скажем, 5 + уровень * 2
        return 5.0f + level * 2.0f;
    }

    @Override
    public String getUpgradeDescription(int currentLevel) {
        if (currentLevel == 0) {
            return com.example.rpg.config.RpgLocale.get("upgrade.unlock_general");
        }
        if (currentLevel >= getMaxLevel()) {
            return com.example.rpg.config.RpgLocale.get("upgrade.max_level");
        }
        return com.example.rpg.config.RpgLocale.get("upgrade.default");
    }

    @Override
    public boolean usesStamina() {
        return usesStamina;
    }

    @Override
    public int getDefaultKey() {
        return defaultKey;
    }

    @Override
    public int getThemeColor() {
        return themeColor;
    }

    // ===========================================
    // Универсальный Builder с fluent-наследованием
    // ===========================================

    @SuppressWarnings("unchecked")
    public static abstract class Builder<T extends Builder<T>> {
        private final String id;
        private int maxLevel = 3;
        private int costPerLevel = 2;
        private int resourceCost = 20;
        private int cooldownSeconds = 5;
        private boolean usesStamina = false;
        private int defaultKey = GLFW.GLFW_KEY_UNKNOWN;
        private int themeColor = 0xFFFFFFFF; // Белый по умолчанию

        protected Builder(String id) {
            this.id = id;
        }

        public T maxLevel(int v) {
            maxLevel = v;
            return (T) this;
        }

        public T costPerLevel(int v) {
            costPerLevel = v;
            return (T) this;
        }

        public T resourceCost(int v) {
            resourceCost = v;
            return (T) this;
        }

        public T cooldown(int v) {
            cooldownSeconds = v;
            return (T) this;
        }

        public T usesStamina() {
            usesStamina = true;
            return (T) this;
        }

        public T usesMana() {
            usesStamina = false;
            return (T) this;
        }

        public T defaultKey(int key) {
            defaultKey = key;
            return (T) this;
        }

        public T themeColor(int color) {
            themeColor = color;
            return (T) this;
        }
    }
}
