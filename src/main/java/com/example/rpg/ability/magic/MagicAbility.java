package com.example.rpg.ability.magic;

import com.example.rpg.ability.AbstractAbility;
import com.example.rpg.stats.MagicElement;

/**
 * Базовый класс для магических способностей.
 * Наследует общую логику из AbstractAbility и добавляет магические поля:
 * element, tier, branch, requiredSkills.
 * Магия всегда использует ману (usesStamina = false).
 */
public abstract class MagicAbility extends AbstractAbility {

    protected final MagicElement element;
    protected final int tier;
    protected final int branch;
    protected final String[] requiredSkills;

    protected MagicAbility(Builder builder) {
        super(builder);
        this.element = builder.element;
        this.tier = builder.tier;
        this.branch = builder.branch;
        this.requiredSkills = builder.requiredSkills;
    }

    public MagicElement getElement() {
        return element;
    }

    public int getTier() {
        return tier;
    }

    public int getBranch() {
        return branch;
    }

    public String[] getRequiredSkills() {
        return requiredSkills;
    }

    public int getManaCost() {
        return getResourceCost();
    }

    public int getManaCost(int level) {
        return getResourceCost(level);
    }

    @Override
    public boolean usesStamina() {
        return false;
    }

    @Override
    public int getThemeColor() {
        return element.borderPrimary;
    }

    // =========================================
    // Builder, расширяющий AbstractAbility.Builder
    // =========================================

    public static class Builder extends AbstractAbility.Builder<Builder> {
        private final MagicElement element;
        private int tier = 0;
        private int branch = 0;
        private String[] requiredSkills = new String[0];

        public Builder(String id, MagicElement element) {
            super(id);
            this.element = element;
            // Магия по умолчанию использует ману
            usesMana();
        }

        public Builder tier(int v) {
            tier = v;
            return this;
        }

        public Builder branch(int v) {
            branch = v;
            return this;
        }

        public Builder requires(String... v) {
            requiredSkills = v;
            return this;
        }

        // Алиас для удобства: manaCost == resourceCost для магии
        public Builder manaCost(int v) {
            return resourceCost(v);
        }
    }
}