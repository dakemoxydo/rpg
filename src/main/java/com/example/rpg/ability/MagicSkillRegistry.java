package com.example.rpg.ability;

import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.ability.magic.fire.FireballAbility;
import com.example.rpg.ability.magic.water.WaterBoltAbility;
import com.example.rpg.ability.magic.wind.GustAbility;
import com.example.rpg.ability.magic.lightning.ShockAbility;
import com.example.rpg.ability.magic.earth.RockThrowAbility;
import com.example.rpg.stats.MagicElement;

import java.util.*;

public class MagicSkillRegistry {

    private static final Map<MagicElement, List<MagicAbility>> ELEMENT_ABILITIES = new EnumMap<>(MagicElement.class);
    private static final Map<String, MagicAbility> ALL_ABILITIES = new HashMap<>();

    static {
        // ==================== FIRE ====================
        registerAbility(new FireballAbility());

        // ==================== WATER ====================
        registerAbility(new WaterBoltAbility());

        // ==================== WIND ====================
        registerAbility(new GustAbility());

        // ==================== LIGHTNING ====================
        registerAbility(new ShockAbility());

        // ==================== EARTH ====================
        registerAbility(new RockThrowAbility());

        // Инициализируем пустые списки
        for (MagicElement element : MagicElement.values()) {
            ELEMENT_ABILITIES.computeIfAbsent(element, e -> new ArrayList<>());
        }
    }

    private static void registerAbility(MagicAbility ability) {
        ALL_ABILITIES.put(ability.getId(), ability);
        ELEMENT_ABILITIES.computeIfAbsent(ability.getElement(), e -> new ArrayList<>()).add(ability);
    }

    public static List<MagicAbility> getAbilitiesForElement(MagicElement element) {
        return ELEMENT_ABILITIES.getOrDefault(element, List.of());
    }

    public static MagicAbility getAbility(String abilityId) {
        return ALL_ABILITIES.get(abilityId);
    }

    public static MagicAbility getAbility(MagicElement element, String abilityId) {
        return ALL_ABILITIES.get(abilityId);
    }

    public static List<MagicAbility> getAbilitiesByTier(MagicElement element, int tier) {
        return getAbilitiesForElement(element).stream()
                .filter(a -> a.getTier() == tier)
                .toList();
    }

    public static MagicAbility getBaseAbility(MagicElement element) {
        return getAbilitiesForElement(element).stream()
                .filter(a -> a.getTier() == 0)
                .findFirst()
                .orElse(null);
    }

    public static boolean isActiveAbility(String abilityId) {
        MagicAbility ability = getAbility(abilityId);
        return ability != null && ability.getManaCost() > 0;
    }

    public static void init() {
    }
}