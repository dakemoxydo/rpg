package com.example.rpg.stats;

import java.util.function.BiFunction;

public abstract class AbstractStat implements IStat {
    private final String id;
    private final String displayName;
    private final String description;
    private final int maxLevel;
    private final int costPerPoint;
    private final int defaultColor;
    private final BiFunction<Integer, IStat, String> nextLevelDescription;

    protected AbstractStat(Builder<?> builder) {
        this.id = builder.id;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.maxLevel = builder.maxLevel;
        this.costPerPoint = builder.costPerPoint;
        this.defaultColor = builder.defaultColor;
        this.nextLevelDescription = builder.nextLevelDescription;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getCostPerPoint() {
        return costPerPoint;
    }

    @Override
    public int getColor(MagicElement element) {
        return defaultColor;
    }

    @Override
    public String getNextLevelDescription(int currentLevel) {
        if (currentLevel >= maxLevel) {
            return "MAX LEVEL";
        }
        return nextLevelDescription != null ? nextLevelDescription.apply(currentLevel, this) : "+1 Level";
    }

    public static abstract class Builder<T extends Builder<T>> {
        private String id;
        private String displayName;
        private String description = "";
        private int maxLevel = 10;
        private int costPerPoint = 1;
        private int defaultColor = 0xFFFFFFFF;
        private BiFunction<Integer, IStat, String> nextLevelDescription = (level, stat) -> "+1 Level";

        public Builder(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        protected abstract T self();

        public T description(String description) {
            this.description = description;
            return self();
        }

        public T maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return self();
        }

        public T costPerPoint(int costPerPoint) {
            this.costPerPoint = costPerPoint;
            return self();
        }

        public T defaultColor(int defaultColor) {
            this.defaultColor = defaultColor;
            return self();
        }

        public T nextLevelDescription(BiFunction<Integer, IStat, String> nextLevelDescription) {
            this.nextLevelDescription = nextLevelDescription;
            return self();
        }

        public abstract AbstractStat build();
    }
}
