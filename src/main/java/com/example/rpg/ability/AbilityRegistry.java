package com.example.rpg.ability;

import com.example.rpg.ability.skills.DashAbility;
import com.example.rpg.ability.skills.HealAbility;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class AbilityRegistry {

    private static final Map<String, Ability> ABILITIES = new HashMap<>();

    static {
        // ==================== PHYSICAL ABILITIES ====================
        registerAbility(new DashAbility());

        // ==================== SUPPORT ABILITIES ====================
        registerAbility(new HealAbility());

        // Добавляйте новые способности здесь:
        // registerAbility(new ShieldAbility());
        // registerAbility(new BerserkAbility());
    }

    /**
     * Регистрирует способность
     */
    private static void registerAbility(Ability ability) {
        ABILITIES.put(ability.getId(), ability);
    }

    /**
     * Получить способность по ID
     */
    public static Ability get(String id) {
        return ABILITIES.get(id);
    }

    /**
     * Получить все способности
     */
    public static Collection<Ability> getAll() {
        return ABILITIES.values();
    }

    public static void init() {
        // Вызывается при инициализации мода
    }
}