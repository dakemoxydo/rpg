package com.example.rpg;

import com.example.rpg.ability.Ability;
import com.example.rpg.ability.AbilityRegistry;
import com.example.rpg.ability.MagicSkillRegistry;
import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.config.RpgConfig;
import com.example.rpg.effect.RpgHudRenderer;
import com.example.rpg.network.StatsNetworking;
import com.example.rpg.screen.StatsScreen;
import com.example.rpg.stats.IPlayerStatsAccessor;
import com.example.rpg.stats.MagicElement;
import com.example.rpg.stats.PlayerStatsData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class RpgClient implements ClientModInitializer {

    private static int menuKey;
    private static boolean menuWasPressed = false;

    // Состояние нажатия для каждой способности: ID -> pressed
    private static final Map<String, Boolean> abilityPressedState = new HashMap<>();

    @Override
    public void onInitializeClient() {
        RpgConfig.load();
        menuKey = RpgConfig.get().getOpenMenuKey();

        StatsNetworking.registerClientHandlers();
        RpgHudRenderer.register();
        registerKeybinds();
    }

    private void registerKeybinds() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null)
                return;

            long window = client.getWindow().getHandle();

            // Menu key (Support both keyboard and mouse)
            boolean menuPressed = false;
            if (menuKey >= 0 && menuKey <= 7) {
                menuPressed = GLFW.glfwGetMouseButton(window, menuKey) == GLFW.GLFW_PRESS;
            } else {
                menuPressed = GLFW.glfwGetKey(window, menuKey) == GLFW.GLFW_PRESS;
            }

            if (menuPressed && !menuWasPressed) {
                if (client.currentScreen == null) {
                    client.setScreen(new StatsScreen());
                } else if (client.currentScreen instanceof StatsScreen) {
                    client.setScreen(null);
                }
            }
            menuWasPressed = menuPressed;

            // Если открыто меню - способности не работают
            if (client.currentScreen != null)
                return;

            // Получаем данные игрока для проверки доступности скиллов
            PlayerStatsData data = null;
            if (client.player instanceof IPlayerStatsAccessor accessor) {
                data = accessor.rpg_getStatsData();
            }
            if (data == null)
                return;

            // 1. Обычные способности
            for (Ability ability : AbilityRegistry.getAll()) {
                checkAbilityKey(window, ability.getId(), ability.getDefaultKey(), false);
            }

            // 2. Магические способности (только активные и только текущей стихии)
            MagicElement element = data.getElement();
            if (element != MagicElement.NONE) {
                for (MagicAbility ability : MagicSkillRegistry.getAbilitiesForElement(element)) {
                    // Проверяем только активные скиллы (у которых есть мана кост)
                    if (ability.getManaCost() > 0) {
                        checkAbilityKey(window, ability.getId(), ability.getDefaultKey(), true);
                    }
                }
            }
        });
    }

    private void checkAbilityKey(long window, String abilityId, int defaultKey, boolean isMagic) {
        int key = RpgConfig.get().getKeybind(abilityId, defaultKey);
        if (key == GLFW.GLFW_KEY_UNKNOWN)
            return;

        boolean pressed = false;
        if (key >= 0 && key <= 7) {
            pressed = GLFW.glfwGetMouseButton(window, key) == GLFW.GLFW_PRESS;
        } else {
            pressed = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
        }

        boolean wasPressed = abilityPressedState.getOrDefault(abilityId, false);

        if (pressed && !wasPressed) {
            if (isMagic) {
                StatsNetworking.sendUseMagicSkill(abilityId);
            } else {
                StatsNetworking.sendUseAbility(abilityId);
            }
        }
        abilityPressedState.put(abilityId, pressed);
    }

    public static void updateMenuKeyBind(int newKey) {
        menuKey = newKey;
    }
}