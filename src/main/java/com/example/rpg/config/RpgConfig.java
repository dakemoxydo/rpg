package com.example.rpg.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RpgConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("rpg");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("rpg_stats.json");

    private static RpgConfig INSTANCE = new RpgConfig();

    private int openMenuKey = GLFW.GLFW_KEY_N;
    private String language = "en";

    // Хранение биндов: ID способности -> Код клавиши
    private Map<String, Integer> keybinds = new HashMap<>();

    public static RpgConfig get() {
        return INSTANCE;
    }

    public int getOpenMenuKey() {
        return openMenuKey;
    }

    public void setOpenMenuKey(int key) {
        this.openMenuKey = key;
        save();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        this.language = lang;
        save();
    }

    public boolean isRussian() {
        return "ru".equals(language);
    }

    // Работа с биндами способностей
    public int getKeybind(String abilityId, int defaultKey) {
        return keybinds.getOrDefault(abilityId, defaultKey);
    }

    public void setKeybind(String abilityId, int key) {
        keybinds.put(abilityId, key);
        save();
    }

    public void clearAllKeybinds() {
        keybinds.clear();
        save();
    }

    public int getDashKey() {
        return getKeybind("dash", GLFW.GLFW_KEY_R);
    }

    public void setDashKey(int key) {
        setKeybind("dash", key);
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                RpgConfig loaded = GSON.fromJson(reader, RpgConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    // Инициализация карты если null (для старых конфигов)
                    if (INSTANCE.keybinds == null)
                        INSTANCE.keybinds = new HashMap<>();
                    LOGGER.info("RPG config loaded successfully");
                } else {
                    createDefault();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load RPG config", e);
                createDefault();
            }
        } else {
            createDefault();
        }
    }

    private static void createDefault() {
        INSTANCE = new RpgConfig();
        INSTANCE.keybinds = new HashMap<>();
        INSTANCE.save();
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save RPG config", e);
        }
    }

    public static String getKeyName(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN)
            return "NONE";

        // Check if it's a mouse button (GLFW mouse buttons are 0-7)
        if (keyCode >= 0 && keyCode <= 7) {
            return "M-" + keyCode;
        }

        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null)
            return name.toUpperCase();
        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "L-SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R-SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "L-CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "L-ALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACK";
            default -> "KEY " + keyCode;
        };
    }
}