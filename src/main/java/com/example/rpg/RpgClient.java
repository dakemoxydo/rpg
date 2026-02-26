package com.example.rpg;

import com.example.rpg.ability.*;
import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.config.RpgConfig;
import com.example.rpg.network.StatsNetworking;
import com.example.rpg.effect.RpgHudRenderer;
import com.example.rpg.screen.StatsScreen;
import com.example.rpg.stats.IPlayerStatsAccessor;
import com.example.rpg.stats.PlayerStatsData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RpgClient implements ClientModInitializer {

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        // Загрузка конфигурации
        RpgConfig.load();

        // Регистрация клиентских обработчиков
        StatsNetworking.registerClientHandlers();

        // HUD
        RpgHudRenderer.register();

        // Кнопка открытия меню
        openMenuKey = new KeyBinding("key.rpg.menu", InputUtil.Type.KEYSYM,
                RpgConfig.get().getOpenMenuKey(), "category.rpg");
        net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(openMenuKey);

        // Обработка клавиш каждый тик
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen != null)
                return;

            // Открытие меню
            if (openMenuKey.wasPressed()) {
                client.setScreen(new StatsScreen());
            }

            // Проверка всех способностей через единый реестр
            PlayerStatsData data = null;
            if (client.player instanceof IPlayerStatsAccessor accessor) {
                data = accessor.rpg_getStatsData();
            }
            if (data == null)
                return;

            for (IAbility ability : AbilityRegistry.getAll()) {
                // Фильтр: нужен ли скилл у игрока
                if (!data.hasSkill(ability.getId()))
                    continue;

                // Для магии — проверяем стихию
                if (ability instanceof MagicAbility magic) {
                    if (data.getElement() != magic.getElement())
                        continue;
                    if (magic.getManaCost() <= 0)
                        continue; // Пассивные скиллы не активируются
                }

                // Проверяем нажатие клавиши
                int keyCode = RpgConfig.get().getKeybind(ability.getId(), ability.getDefaultKey());
                if (keyCode != GLFW.GLFW_KEY_UNKNOWN && isKeyPressed(client, keyCode)) {
                    // Проверяем кулдаун на клиенте (для отзывчивости)
                    if (!AbilityCooldownManager.isOnCooldown(client.player.getUuid(), ability.getId())) {
                        StatsNetworking.sendUseAbility(ability.getId());
                    }
                }
            }
        });
    }

    private static boolean isKeyPressed(MinecraftClient client, int keyCode) {
        if (client.getWindow() == null)
            return false;
        // Мышь (GLFW mouse buttons 0-7)
        if (keyCode >= 0 && keyCode <= 7) {
            return GLFW.glfwGetMouseButton(client.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
        }
        // Клавиатура
        return InputUtil.isKeyPressed(client.getWindow().getHandle(), keyCode);
    }

    public static void updateMenuKeyBind(int keyCode) {
        if (openMenuKey != null) {
            openMenuKey.setBoundKey(keyCode >= 0 && keyCode <= 7
                    ? InputUtil.Type.MOUSE.createFromCode(keyCode)
                    : InputUtil.Type.KEYSYM.createFromCode(keyCode));
        }
    }
}