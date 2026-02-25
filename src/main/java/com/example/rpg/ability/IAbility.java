package com.example.rpg.ability;

/**
 * Единый интерфейс для всех активных способностей (и физических, и магических).
 * Позволяет HUD и менеджерам кулдаунов работать с ними единообразно.
 */
public interface IAbility {

    /**
     * @return Уникальный идентификатор способности (например, "dash", "fireball")
     */
    String getId();

    /**
     * @return Иконка шрифта (символ), представляющая способность в UI
     */
    String getIcon();

    /**
     * @return Длительность кулдауна в тиках
     */
    int getCooldownTicks();

    /**
     * @return Использует ли способность стамину (true) или ману (false)
     */
    boolean usesStamina();

    /**
     * @return Клавиша по умолчанию для вызова способности
     */
    int getDefaultKey();
}
