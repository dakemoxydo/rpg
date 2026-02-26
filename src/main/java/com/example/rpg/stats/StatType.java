package com.example.rpg.stats;

public enum StatType {
    HEALTH("Health", "Adds +2 HP per level", 10, 3, 0, 2.0, 0, 0, 0xFFe85050,
            (level, stat) -> "+2.0 (To " + ((level + 1) * 2.0) + ")"),
    STRENGTH("Strength", "Adds +0.5 attack damage per level", 10, 2, 0, 0.5, 0, 0, 0xFFe88050,
            (level, stat) -> "+0.5 (To " + ((level + 1) * 0.5) + ")"),
    SPEED("Speed", "Adds +5% move speed per level", 8, 1, 0, 0.05, 0, 0, 0xFF50b8e8,
            (level, stat) -> "+0.05 (To " + String.format("%.2f", (level + 1) * 0.05) + ")"),
    JUMP("Jump Height", "Adds Jump Boost effect", 5, 1, 0, 1.0, 0, 0, 0xFF50e8a0,
            (level, stat) -> "Jump Boost " + (level + 1)),
    MANA("Mana", "+20 max mana, +1 regen/sec", 10, 2, 100.0, 20.0, 1.0, 1.0, 0xFF4169E1,
            (level, stat) -> String.format("Max: %d, Regen: %.1f/s", (int) stat.getValueAtLevel(level + 1),
                    stat.getRegenAtLevel(level + 1))),
    STAMINA("Stamina", "+15 max stamina, +0.5 regen/sec", 10, 2, 100.0, 15.0, 2.0, 0.5, 0xFFDAA520,
            (level, stat) -> String.format("Max: %d, Regen: %.1f/s", (int) stat.getValueAtLevel(level + 1),
                    stat.getRegenAtLevel(level + 1))),
    FORTUNE("Fortune", "Adds +1 fortune level on ores", 3, 5, 0, 1.0, 0, 0, 0xFF50e850,
            (level, stat) -> "+" + (level + 1) + " total level"),
    LOOTING("Looting", "Adds +1 looting level on kills", 3, 5, 0, 1.0, 0, 0, 0xFFe8e850,
            (level, stat) -> "+" + (level + 1) + " total level");

    private final String displayName;
    private final String description;
    private final int maxLevel;
    private final int costPerPoint;
    private final double baseValue;
    private final double valuePerLevel;
    private final double baseRegen;
    private final double regenPerLevel;
    private final int defaultColor;
    private final java.util.function.BiFunction<Integer, StatType, String> nextLevelDescription;

    StatType(String displayName, String description, int maxLevel,
            int costPerPoint, double baseValue, double valuePerLevel,
            double baseRegen, double regenPerLevel,
            int defaultColor, java.util.function.BiFunction<Integer, StatType, String> nextLevelDescription) {
        this.displayName = displayName;
        this.description = description;
        this.maxLevel = maxLevel;
        this.costPerPoint = costPerPoint;
        this.baseValue = baseValue;
        this.valuePerLevel = valuePerLevel;
        this.baseRegen = baseRegen;
        this.regenPerLevel = regenPerLevel;
        this.defaultColor = defaultColor;
        this.nextLevelDescription = nextLevelDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getCostPerPoint() {
        return costPerPoint;
    }

    public double getBaseValue() {
        return baseValue;
    }

    public double getValuePerLevel() {
        return valuePerLevel;
    }

    public double getValueAtLevel(int level) {
        return baseValue + (valuePerLevel * level);
    }

    public double getRegenAtLevel(int level) {
        return baseRegen + (regenPerLevel * level);
    }

    public int getColor(MagicElement element) {
        if (this == MANA)
            return element != null && element != MagicElement.NONE ? element.manaColor : defaultColor;
        if (this == STAMINA)
            return element != null && element != MagicElement.NONE ? element.staminaColor : defaultColor;
        return defaultColor;
    }

    public String getNextLevelDescription(int currentLevel) {
        if (currentLevel >= maxLevel)
            return "MAX LEVEL";
        return nextLevelDescription.apply(currentLevel, this);
    }
}