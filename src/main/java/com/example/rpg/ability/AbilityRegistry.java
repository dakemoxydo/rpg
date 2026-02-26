package com.example.rpg.ability;

import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.ability.magic.earth.RockThrowAbility;
import com.example.rpg.ability.magic.fire.FireballAbility;
import com.example.rpg.ability.magic.lightning.ShockAbility;
import com.example.rpg.ability.magic.water.WaterBoltAbility;
import com.example.rpg.ability.magic.wind.GustAbility;
import com.example.rpg.ability.skills.DashAbility;
import com.example.rpg.ability.skills.HealAbility;
import com.example.rpg.stats.MagicElement;

import java.util.*;

/**
 * Единый реестр всех способностей мода (физических и магических).
 * Обеспечивает быстрый доступ по ID, по стихии, и по типу.
 */
public class AbilityRegistry {

    private static final Map<String, IAbility> ALL = new LinkedHashMap<>();
    private static final List<Ability> PHYSICAL = new ArrayList<>();
    private static final Map<MagicElement, List<MagicAbility>> BY_ELEMENT = new EnumMap<>(MagicElement.class);

    static {
        // Инициализируем пустые списки для всех стихий
        for (MagicElement element : MagicElement.values()) {
            BY_ELEMENT.put(element, new ArrayList<>());
        }

        // ==================== PHYSICAL ABILITIES ====================
        register(new DashAbility());
        register(new HealAbility());

        // ==================== MAGIC ABILITIES ====================
        register(new FireballAbility());
        register(new WaterBoltAbility());
        register(new GustAbility());
        register(new ShockAbility());
        register(new RockThrowAbility());
    }

    // ==================== REGISTRATION ====================

    private static void register(Ability ability) {
        ALL.put(ability.getId(), ability);
        PHYSICAL.add(ability);
    }

    private static void register(MagicAbility ability) {
        ALL.put(ability.getId(), ability);
        BY_ELEMENT.computeIfAbsent(ability.getElement(), e -> new ArrayList<>()).add(ability);
    }

    // ==================== LOOKUP ====================

    /**
     * Получить любую способность по ID (физическую или магическую).
     */
    public static IAbility get(String id) {
        return ALL.get(id);
    }

    /**
     * Получить все зарегистрированные способности.
     */
    public static Collection<IAbility> getAll() {
        return Collections.unmodifiableCollection(ALL.values());
    }

    /**
     * Получить только физические способности (Dash, Heal и т.д.).
     */
    public static List<Ability> getPhysicalAbilities() {
        return Collections.unmodifiableList(PHYSICAL);
    }

    /**
     * Получить магические способности по стихии.
     */
    public static List<MagicAbility> getMagicAbilities(MagicElement element) {
        return BY_ELEMENT.getOrDefault(element, List.of());
    }

    /**
     * Получить магическую способность по ID (или null если не магическая).
     */
    public static MagicAbility getMagicAbility(String id) {
        IAbility ability = ALL.get(id);
        return ability instanceof MagicAbility magic ? magic : null;
    }

    /**
     * Получить базовую способность стихии (tier 0).
     */
    public static MagicAbility getBaseAbility(MagicElement element) {
        return getMagicAbilities(element).stream()
                .filter(a -> a.getTier() == 0)
                .findFirst()
                .orElse(null);
    }

    /**
     * Получить магические способности определённого тира.
     */
    public static List<MagicAbility> getMagicAbilitiesByTier(MagicElement element, int tier) {
        return getMagicAbilities(element).stream()
                .filter(a -> a.getTier() == tier)
                .toList();
    }

    /**
     * Является ли способность активной магической (т.е. тратит ману).
     */
    public static boolean isActiveMagicAbility(String abilityId) {
        MagicAbility ability = getMagicAbility(abilityId);
        return ability != null && ability.getManaCost() > 0;
    }

    /**
     * Вызывается при инициализации мода для загрузки static-блока.
     */
    public static void init() {
    }
}