package com.example.rpg.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class RenderUtils {

    public static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color); // Top
        context.fill(x, y + h - 1, x + w, y + h, color); // Bottom
        context.fill(x, y + 1, x + 1, y + h - 1, color); // Left
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color); // Right
    }

    // Метод для обрезки области (скроллинг) с учетом масштаба интерфейса
    public static void enableScissor(DrawContext context, int x, int y, int width, int height) {
        double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        int windowHeight = MinecraftClient.getInstance().getWindow().getHeight();

        int scissorX = (int) (x * scale);
        int scissorY = (int) (windowHeight - (y + height) * scale);
        int scissorWidth = (int) (width * scale);
        int scissorHeight = (int) (height * scale);

        context.enableScissor(scissorX, scissorY, scissorX + scissorWidth, scissorY + scissorHeight);
    }

    public static void disableScissor(DrawContext context) {
        context.disableScissor();
    }

    public static float getUiScale(int screenWidth) {
        // Оптимизированный расчет масштаба
        float baseWidth = 854f;
        float scale = screenWidth / baseWidth;
        return Math.max(0.7f, Math.min(1.3f, scale));
    }

    public static int withAlpha(int color, float alpha) {
        int a = (int) (Math.max(0f, Math.min(1f, alpha)) * 255);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static int brighten(int color, int amount) {
        return shiftColor(color, amount);
    }

    public static int darken(int color, int amount) {
        return shiftColor(color, -amount);
    }

    private static int shiftColor(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, Math.min(255, ((color >> 16) & 0xFF) + amount));
        int g = Math.max(0, Math.min(255, ((color >> 8) & 0xFF) + amount));
        int b = Math.max(0, Math.min(255, (color & 0xFF) + amount));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int blendColors(int color1, int color2, float ratio) {
        ratio = Math.max(0f, Math.min(1f, ratio));
        int a = (int) (((color1 >> 24) & 0xFF) * (1 - ratio) + ((color2 >> 24) & 0xFF) * ratio);
        int r = (int) (((color1 >> 16) & 0xFF) * (1 - ratio) + ((color2 >> 16) & 0xFF) * ratio);
        int g = (int) (((color1 >> 8) & 0xFF) * (1 - ratio) + ((color2 >> 8) & 0xFF) * ratio);
        int b = (int) ((color1 & 0xFF) * (1 - ratio) + (color2 & 0xFF) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}