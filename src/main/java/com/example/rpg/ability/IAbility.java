package com.example.rpg.ability;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Единый интерфейс для всех активных способностей (и физических, и магических).
 * Позволяет HUD, менеджерам кулдаунов и сетевому слою работать единообразно.
 */
public interface IAbility {

    /**
     * @return Уникальный идентификатор способности (например, "dash",
     *         "fire_fireball")
     */
    String getId();

    /**
     * @return Иконка шрифта (символ), представляющая способность в UI
     */
    String getIcon();

    /**
     * @return Базовый цвет темы способности (по умолчанию можно использовать для
     *         рамок, пульсации, HUD)
     */
    int getThemeColor();

    /**
     * @return Длительность кулдауна в тиках на указанном уровне
     */
    int getCooldownTicks(int level);

    /**
     * @return Длительность кулдауна в секундах на указанном уровне
     */
    int getCooldownSeconds(int level);

    /**
     * @return Сила способности (урон, лечение, сила эффекта) на указанном уровне
     */
    float getPower(int level);

    /**
     * Возвращает креативное описание того, что дает переход на следующий уровень
     * (currentLevel + 1). Если текущий уровень 0, должно быть "Разблокирует
     * способность".
     */
    String getUpgradeDescription(int currentLevel);

    /**
     * @return Использует ли способность стамину (true) или ману (false)
     */
    boolean usesStamina();

    /**
     * @return Клавиша по умолчанию для вызова способности
     */
    int getDefaultKey();

    /**
     * @return Максимальный уровень прокачки
     */
    int getMaxLevel();

    /**
     * @return Стоимость прокачки за уровень (в SP)
     */
    int getCostPerLevel();

    /**
     * Вычисляет стоимость ресурса (маны или стамины) на указанном уровне.
     * 
     * @param level текущий уровень способности
     * @return стоимость ресурса
     */
    int getResourceCost(int level);

    /**
     * Выполняет способность.
     * 
     * @param player игрок, использующий способность
     * @param level  текущий уровень способности у игрока
     */
    void execute(ServerPlayerEntity player, int level);
}
