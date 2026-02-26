package com.example.rpg.stats;

import java.util.function.BiFunction;

public class AttributeStat extends AbstractStat {
    private final double baseValue;
    private final double valuePerLevel;
    private final double baseRegen;
    private final double regenPerLevel;
    private final BiFunction<MagicElement, Integer, Integer> colorFunction;

    private AttributeStat(Builder builder) {
        super(builder);
        this.baseValue = builder.baseValue;
        this.valuePerLevel = builder.valuePerLevel;
        this.baseRegen = builder.baseRegen;
        this.regenPerLevel = builder.regenPerLevel;
        this.colorFunction = builder.colorFunction;
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

    @Override
    public int getColor(MagicElement element) {
        if (colorFunction != null) {
            return colorFunction.apply(element, super.getColor(element));
        }
        return super.getColor(element);
    }

    public static class Builder extends AbstractStat.Builder<Builder> {
        private double baseValue = 0;
        private double valuePerLevel = 0;
        private double baseRegen = 0;
        private double regenPerLevel = 0;
        private BiFunction<MagicElement, Integer, Integer> colorFunction = null;

        public Builder(String id, String displayName) {
            super(id, displayName);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder baseValue(double baseValue) {
            this.baseValue = baseValue;
            return this;
        }

        public Builder valuePerLevel(double valuePerLevel) {
            this.valuePerLevel = valuePerLevel;
            return this;
        }

        public Builder baseRegen(double baseRegen) {
            this.baseRegen = baseRegen;
            return this;
        }

        public Builder regenPerLevel(double regenPerLevel) {
            this.regenPerLevel = regenPerLevel;
            return this;
        }

        public Builder dynamicColor(BiFunction<MagicElement, Integer, Integer> colorFunction) {
            this.colorFunction = colorFunction;
            return this;
        }

        @Override
        public AttributeStat build() {
            return new AttributeStat(this);
        }
    }
}
