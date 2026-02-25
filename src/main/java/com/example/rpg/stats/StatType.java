package com.example.rpg.stats;

public enum StatType {
    HEALTH("Health", "Adds +2 HP per level", 10, 3, 0, 2.0),
    STRENGTH("Strength", "Adds +0.5 attack damage per level", 10, 2, 0, 0.5),
    SPEED("Speed", "Adds +5% move speed per level", 8, 1, 0, 0.05),
    JUMP("Jump Height", "Adds Jump Boost effect", 5, 1, 0, 1.0),
    MANA("Mana", "+20 max mana, +1 regen/sec", 10, 2, 0, 20.0),
    STAMINA("Stamina", "+15 max stamina, +0.5 regen/sec", 10, 2, 0, 15.0),
    FORTUNE("Fortune", "Adds +1 fortune level on ores", 3, 5, 0, 1.0),
    LOOTING("Looting", "Adds +1 looting level on kills", 3, 5, 0, 1.0);

    private final String displayName;
    private final String description;
    private final int maxLevel;
    private final int costPerPoint;
    private final double baseValue;
    private final double valuePerLevel;

    StatType(String displayName, String description, int maxLevel,
             int costPerPoint, double baseValue, double valuePerLevel) {
        this.displayName = displayName;
        this.description = description;
        this.maxLevel = maxLevel;
        this.costPerPoint = costPerPoint;
        this.baseValue = baseValue;
        this.valuePerLevel = valuePerLevel;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getMaxLevel() { return maxLevel; }
    public int getCostPerPoint() { return costPerPoint; }
    public double getBaseValue() { return baseValue; }
    public double getValuePerLevel() { return valuePerLevel; }

    public double getValueAtLevel(int level) {
        return baseValue + (valuePerLevel * level);
    }

    public String getNextLevelDescription(int currentLevel) {
        if (currentLevel >= maxLevel) return "MAX LEVEL";

        if (this == JUMP) {
            return "Jump Boost " + (currentLevel + 1);
        }
        if (this == FORTUNE || this == LOOTING) {
            return "+" + (currentLevel + 1) + " total level";
        }
        if (this == MANA) {
            int nextMax = 100 + (int)((currentLevel + 1) * 20);
            double nextRegen = 1 + (currentLevel + 1) * 1.0;
            return "Max: " + nextMax + ", Regen: " + String.format("%.1f", nextRegen) + "/s";
        }
        if (this == STAMINA) {
            int nextMax = 100 + (int)((currentLevel + 1) * 15);
            double nextRegen = 2 + (currentLevel + 1) * 0.5;
            return "Max: " + nextMax + ", Regen: " + String.format("%.1f", nextRegen) + "/s";
        }

        double currentValue = getValueAtLevel(currentLevel);
        double nextValue = getValueAtLevel(currentLevel + 1);
        return String.format("+%.1f (%.1f -> %.1f)", valuePerLevel, currentValue, nextValue);
    }
}