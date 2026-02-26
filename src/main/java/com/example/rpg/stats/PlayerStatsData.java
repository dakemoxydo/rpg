package com.example.rpg.stats;

import com.example.rpg.ability.AbilityRegistry;
import com.example.rpg.ability.IAbility;
import net.minecraft.nbt.NbtCompound;
import java.util.HashMap;
import java.util.Map;

public class PlayerStatsData {

    private final Map<String, Integer> statLevels = new HashMap<>();

    /**
     * Единая карта уровней всех способностей (физических и магических).
     * Ключ — ID способности (например, "dash", "fire_fireball").
     */
    private final Map<String, Integer> skillLevels = new HashMap<>();

    private int totalXp = 0;
    private int currentLevel = 0;
    private int skillPoints = 0;

    private float currentMana = 100;
    private float currentStamina = 100;

    private MagicElement element = MagicElement.NONE;

    // Per-player settings
    private String language = "en";
    private int openMenuKey = org.lwjgl.glfw.GLFW.GLFW_KEY_N;
    private final Map<String, Integer> keybinds = new HashMap<>();

    public PlayerStatsData() {
        for (AttributeStat stat : StatRegistry.getAll()) {
            statLevels.put(stat.getId(), 0);
        }
    }

    // --- Стихия ---

    public MagicElement getElement() {
        return element;
    }

    public void setElement(MagicElement element) {
        this.element = element;
    }

    public boolean hasElement() {
        return element != MagicElement.NONE;
    }

    public void resetElement() {
        this.element = MagicElement.NONE;
        skillLevels.entrySet().removeIf(entry -> AbilityRegistry.getMagicAbility(entry.getKey()) != null);
    }

    // --- Per-player settings ---

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isRussian() {
        return "ru".equals(language);
    }

    public int getOpenMenuKey() {
        return openMenuKey;
    }

    public void setOpenMenuKey(int key) {
        this.openMenuKey = key;
    }

    public int getKeybind(String abilityId, int defaultKey) {
        return keybinds.getOrDefault(abilityId, defaultKey);
    }

    public void setKeybind(String abilityId, int key) {
        keybinds.put(abilityId, key);
    }

    public Map<String, Integer> getKeybinds() {
        return keybinds;
    }

    public void clearAllKeybinds() {
        keybinds.clear();
    }

    // --- Единая система уровней способностей ---

    public int getSkillLevel(String skillId) {
        return skillLevels.getOrDefault(skillId, 0);
    }

    public void setSkillLevel(String skillId, int level) {
        skillLevels.put(skillId, level);
    }

    public boolean hasSkill(String skillId) {
        return getSkillLevel(skillId) > 0;
    }

    public boolean canUpgradeSkill(String skillId, int maxLevel, int cost) {
        int currentLvl = getSkillLevel(skillId);
        return currentLvl < maxLevel && skillPoints >= cost;
    }

    public boolean upgradeSkill(String skillId, int maxLevel, int cost) {
        if (!canUpgradeSkill(skillId, maxLevel, cost))
            return false;
        skillPoints -= cost;
        skillLevels.put(skillId, getSkillLevel(skillId) + 1);
        return true;
    }

    // Обратная совместимость: алиасы для старого API
    public int getAbilityLevel(String abilityId) {
        return getSkillLevel(abilityId);
    }

    public int getMagicSkillLevel(String skillId) {
        return getSkillLevel(skillId);
    }

    public boolean hasAbility(String abilityId) {
        return hasSkill(abilityId);
    }

    public boolean hasMagicSkill(String skillId) {
        return hasSkill(skillId);
    }

    public boolean meetsRequirements(String[] requiredSkills) {
        if (requiredSkills == null || requiredSkills.length == 0)
            return true;
        for (String reqId : requiredSkills) {
            if (reqId == null || getSkillLevel(reqId) <= 0)
                return false;
        }
        return true;
    }

    public int getSpentSkillPoints() {
        int spent = 0;
        for (Map.Entry<String, Integer> entry : skillLevels.entrySet()) {
            IAbility ability = AbilityRegistry.get(entry.getKey());
            if (ability != null) {
                spent += entry.getValue() * ability.getCostPerLevel();
            } else {
                spent += entry.getValue() * 2; // Fallback
            }
        }
        return spent;
    }

    // Обратная совместимость
    public int getSpentMagicPoints() {
        int spent = 0;
        for (Map.Entry<String, Integer> entry : skillLevels.entrySet()) {
            if (AbilityRegistry.getMagicAbility(entry.getKey()) != null) {
                IAbility ability = AbilityRegistry.get(entry.getKey());
                spent += entry.getValue() * ability.getCostPerLevel();
            }
        }
        return spent;
    }

    // --- Статы ---

    public int getStatLevel(String statId) {
        return statLevels.getOrDefault(statId, 0);
    }

    public boolean canUpgrade(String statId) {
        AttributeStat stat = StatRegistry.get(statId);
        if (stat == null)
            return false;
        int currentLvl = getStatLevel(statId);
        return currentLvl < stat.getMaxLevel() && skillPoints >= stat.getCostPerPoint();
    }

    public boolean upgradeStat(String statId) {
        AttributeStat stat = StatRegistry.get(statId);
        if (stat == null || !canUpgrade(statId))
            return false;
        skillPoints -= stat.getCostPerPoint();
        statLevels.put(statId, getStatLevel(statId) + 1);
        return true;
    }

    public void setStatLevel(String statId, int level) {
        AttributeStat stat = StatRegistry.get(statId);
        if (stat == null)
            return;
        statLevels.put(statId, Math.min(level, stat.getMaxLevel()));
        if (StatRegistry.INTELLIGENCE.equals(statId)) {
            currentMana = Math.min(currentMana, getMaxMana());
        } else if (StatRegistry.DEXTERITY.equals(statId)) {
            currentStamina = Math.min(currentStamina, getMaxStamina());
        }
    }

    // --- Мана ---

    public int getMaxMana() {
        AttributeStat intel = StatRegistry.get(StatRegistry.INTELLIGENCE);
        if (intel == null)
            return 100;
        return (int) intel.getValueAtLevel(getStatLevel(StatRegistry.INTELLIGENCE));
    }

    public float getCurrentMana() {
        return Math.min(currentMana, getMaxMana());
    }

    public void setCurrentMana(float mana) {
        this.currentMana = Math.max(0, Math.min(mana, getMaxMana()));
    }

    public float getManaRegenPerSecond() {
        AttributeStat intel = StatRegistry.get(StatRegistry.INTELLIGENCE);
        if (intel == null)
            return 2.0f;
        return (float) intel.getRegenAtLevel(getStatLevel(StatRegistry.INTELLIGENCE));
    }

    public boolean useMana(int amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            return true;
        }
        return false;
    }

    public void regenMana(float deltaSeconds) {
        currentMana = Math.min(currentMana + getManaRegenPerSecond() * deltaSeconds, getMaxMana());
    }

    public float getManaProgress() {
        int max = getMaxMana();
        if (max <= 0)
            return 0;
        return getCurrentMana() / max;
    }

    // --- Стамина ---

    public int getMaxStamina() {
        AttributeStat dex = StatRegistry.get(StatRegistry.DEXTERITY);
        if (dex == null)
            return 100;
        return (int) dex.getValueAtLevel(getStatLevel(StatRegistry.DEXTERITY));
    }

    public float getCurrentStamina() {
        return Math.min(currentStamina, getMaxStamina());
    }

    public void setCurrentStamina(float stamina) {
        this.currentStamina = Math.max(0, Math.min(stamina, getMaxStamina()));
    }

    public float getStaminaRegenPerSecond() {
        AttributeStat dex = StatRegistry.get(StatRegistry.DEXTERITY);
        if (dex == null)
            return 5.0f;
        return (float) dex.getRegenAtLevel(getStatLevel(StatRegistry.DEXTERITY));
    }

    public boolean useStamina(int amount) {
        if (currentStamina >= amount) {
            currentStamina -= amount;
            return true;
        }
        return false;
    }

    public void regenStamina(float deltaSeconds) {
        currentStamina = Math.min(currentStamina + getStaminaRegenPerSecond() * deltaSeconds, getMaxStamina());
    }

    public float getStaminaProgress() {
        int max = getMaxStamina();
        if (max <= 0)
            return 0;
        return getCurrentStamina() / max;
    }

    // --- Здоровье (Конституция/Живучесть) ---
    public float getMaxHealthBonus() {
        AttributeStat vit = StatRegistry.get(StatRegistry.VITALITY);
        if (vit == null)
            return 0f;
        return (float) vit.getValueAtLevel(getStatLevel(StatRegistry.VITALITY)) - (float) vit.getBaseValue(); // Only
                                                                                                              // bonus
                                                                                                              // goes to
                                                                                                              // max
                                                                                                              // health
    }

    public float getDefenseReduction() {
        AttributeStat vit = StatRegistry.get(StatRegistry.VITALITY);
        if (vit == null)
            return 0f;
        return Math.min(0.60f, (getStatLevel(StatRegistry.VITALITY) / 10.0f) * 0.05f);
    }

    // --- Урон ---
    public float getMeleeDamageBonus() {
        AttributeStat str = StatRegistry.get(StatRegistry.STRENGTH);
        if (str == null)
            return 0f;
        return (float) str.getValueAtLevel(getStatLevel(StatRegistry.STRENGTH)) - (float) str.getBaseValue();
    }

    public float getMagicDamageBonus() {
        AttributeStat mag = StatRegistry.get(StatRegistry.MAGIC_POWER);
        if (mag == null)
            return 0f;
        return (float) mag.getValueAtLevel(getStatLevel(StatRegistry.MAGIC_POWER)) - (float) mag.getBaseValue();
    }

    // --- XP и уровни ---

    public int getTotalXp() {
        return totalXp;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int points) {
        this.skillPoints = points;
    }

    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }

    public void setTotalXp(int xp) {
        this.totalXp = xp;
    }

    public int getXpForNextLevel() {
        return 50 + (currentLevel * 25);
    }

    public int getCurrentLevelXp() {
        int xpUsed = 0;
        for (int i = 0; i < currentLevel; i++) {
            xpUsed += 50 + (i * 25);
        }
        return totalXp - xpUsed;
    }

    public float getXpProgress() {
        int needed = getXpForNextLevel();
        if (needed <= 0)
            return 1.0f;
        return (float) getCurrentLevelXp() / (float) needed;
    }

    public boolean addXp(int amount) {
        totalXp += amount;
        boolean leveledUp = false;
        while (getCurrentLevelXp() >= getXpForNextLevel()) {
            currentLevel++;
            skillPoints++;
            currentMana = getMaxMana();
            currentStamina = getMaxStamina();
            leveledUp = true;
        }
        return leveledUp;
    }

    public void reset() {
        totalXp = 0;
        currentLevel = 0;
        skillPoints = 0;
        currentMana = 100;
        currentStamina = 100;
        statLevels.clear();
        for (AttributeStat stat : StatRegistry.getAll()) {
            statLevels.put(stat.getId(), 0);
        }
        skillLevels.clear();
    }

    public void resetStats() {
        statLevels.clear();
        for (AttributeStat stat : StatRegistry.getAll()) {
            statLevels.put(stat.getId(), 0);
        }
        // При сбросе статов обнуляем только физические скиллы
        skillLevels.entrySet().removeIf(entry -> AbilityRegistry.getMagicAbility(entry.getKey()) == null);
        currentMana = Math.min(currentMana, getMaxMana());
        currentStamina = Math.min(currentStamina, getMaxStamina());
    }

    public int getSpentPoints() {
        int spent = 0;
        for (AttributeStat stat : StatRegistry.getAll()) {
            spent += getStatLevel(stat.getId()) * stat.getCostPerPoint();
        }
        // Считаем только физические скиллы при сбросе статов
        for (Map.Entry<String, Integer> entry : skillLevels.entrySet()) {
            if (AbilityRegistry.getMagicAbility(entry.getKey()) == null) {
                IAbility ability = AbilityRegistry.get(entry.getKey());
                if (ability != null) {
                    spent += entry.getValue() * ability.getCostPerLevel();
                } else {
                    spent += entry.getValue() * 2;
                }
            }
        }
        return spent;
    }

    // --- NBT ---

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("totalXp", totalXp);
        nbt.putInt("currentLevel", currentLevel);
        nbt.putInt("skillPoints", skillPoints);
        nbt.putFloat("currentMana", currentMana);
        nbt.putFloat("currentStamina", currentStamina);
        nbt.putString("element", element.name());

        NbtCompound statsNbt = new NbtCompound();
        for (Map.Entry<String, Integer> entry : statLevels.entrySet()) {
            statsNbt.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("statsV2", statsNbt); // Using new key to avoid conflicts

        NbtCompound skillsNbt = new NbtCompound();
        for (Map.Entry<String, Integer> entry : skillLevels.entrySet()) {
            skillsNbt.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("skills", skillsNbt);

        // Per-player settings
        nbt.putString("language", language);
        nbt.putInt("openMenuKey", openMenuKey);
        NbtCompound bindsNbt = new NbtCompound();
        for (Map.Entry<String, Integer> entry : keybinds.entrySet()) {
            bindsNbt.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("keybinds", bindsNbt);

        return nbt;
    }

    public static PlayerStatsData fromNbt(NbtCompound nbt) {
        if (nbt == null) {
            return new PlayerStatsData();
        }

        PlayerStatsData data = new PlayerStatsData();
        data.totalXp = nbt.getInt("totalXp");
        data.currentLevel = nbt.getInt("currentLevel");
        data.skillPoints = nbt.getInt("skillPoints");
        data.currentMana = nbt.contains("currentMana") ? nbt.getFloat("currentMana") : 100;
        data.currentStamina = nbt.contains("currentStamina") ? nbt.getFloat("currentStamina") : 100;

        if (nbt.contains("element")) {
            try {
                data.element = MagicElement.valueOf(nbt.getString("element"));
            } catch (Exception e) {
                data.element = MagicElement.NONE;
            }
        }

        // New format
        if (nbt.contains("statsV2")) {
            NbtCompound statsNbt = nbt.getCompound("statsV2");
            for (String key : statsNbt.getKeys()) {
                data.statLevels.put(key, statsNbt.getInt(key));
            }
        }
        // Migrate old format
        else if (nbt.contains("stats")) {
            NbtCompound oldStatsNbt = nbt.getCompound("stats");
            if (oldStatsNbt.contains("HEALTH"))
                data.statLevels.put(StatRegistry.VITALITY, oldStatsNbt.getInt("HEALTH"));
            if (oldStatsNbt.contains("STRENGTH"))
                data.statLevels.put(StatRegistry.STRENGTH, oldStatsNbt.getInt("STRENGTH"));
            if (oldStatsNbt.contains("MANA"))
                data.statLevels.put(StatRegistry.INTELLIGENCE, oldStatsNbt.getInt("MANA"));
            if (oldStatsNbt.contains("STAMINA"))
                data.statLevels.put(StatRegistry.DEXTERITY, oldStatsNbt.getInt("STAMINA"));
        }

        // Ensure all stats are present in map
        for (AttributeStat stat : StatRegistry.getAll()) {
            data.statLevels.putIfAbsent(stat.getId(), 0);
        }

        // Единая карта "skills"
        if (nbt.contains("skills")) {
            NbtCompound skillsNbt = nbt.getCompound("skills");
            for (String key : skillsNbt.getKeys()) {
                data.skillLevels.put(key, skillsNbt.getInt(key));
            }
        }

        // Миграция старого формата
        if (nbt.contains("abilities")) {
            NbtCompound abilitiesNbt = nbt.getCompound("abilities");
            for (String key : abilitiesNbt.getKeys()) {
                data.skillLevels.putIfAbsent(key, abilitiesNbt.getInt(key));
            }
        }
        if (nbt.contains("magicSkills")) {
            NbtCompound magicNbt = nbt.getCompound("magicSkills");
            for (String key : magicNbt.getKeys()) {
                data.skillLevels.putIfAbsent(key, magicNbt.getInt(key));
            }
        }

        // Per-player settings
        if (nbt.contains("language")) {
            data.language = nbt.getString("language");
        }
        if (nbt.contains("openMenuKey")) {
            data.openMenuKey = nbt.getInt("openMenuKey");
        }
        if (nbt.contains("keybinds")) {
            NbtCompound bindsNbt = nbt.getCompound("keybinds");
            for (String key : bindsNbt.getKeys()) {
                data.keybinds.put(key, bindsNbt.getInt(key));
            }
        }

        return data;
    }

    public void copyFrom(PlayerStatsData other) {
        this.totalXp = other.totalXp;
        this.currentLevel = other.currentLevel;
        this.skillPoints = other.skillPoints;
        this.currentMana = other.currentMana;
        this.currentStamina = other.currentStamina;
        this.element = other.element;
        this.language = other.language;
        this.openMenuKey = other.openMenuKey;
        this.statLevels.clear();
        this.statLevels.putAll(other.statLevels);
        this.skillLevels.clear();
        this.skillLevels.putAll(other.skillLevels);
        this.keybinds.clear();
        this.keybinds.putAll(other.keybinds);
    }
}