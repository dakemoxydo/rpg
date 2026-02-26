package com.example.rpg.ability;

/**
 * Базовый класс для физических/поддерживающих способностей (Dash, Heal и т.д.).
 * Наследует общую логику из AbstractAbility.
 */
public abstract class Ability extends AbstractAbility {

    protected Ability(Builder builder) {
        super(builder);
    }

    public static class Builder extends AbstractAbility.Builder<Builder> {
        public Builder(String id) {
            super(id);
        }
    }
}