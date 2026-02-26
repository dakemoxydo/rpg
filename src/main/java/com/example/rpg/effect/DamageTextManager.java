package com.example.rpg.effect;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DamageTextManager {
    private static final List<DamageText> activeTexts = new ArrayList<>();

    public static void register() {
        // Обновление логики (движение вверх, исчезновение)
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());

        // Отрисовка в 3D мире
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null)
                return;

            Camera camera = context.camera();
            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();

            render(matrices, camera, client.textRenderer, vertexConsumers);
        });
    }

    public static void spawnDamageText(double x, double y, double z, float amount) {
        // Добавляем рандомный разброс X и Z
        double offsetX = (Math.random() - 0.5) * 1.5;
        double offsetY = (Math.random() * 0.5) + 0.5;
        double offsetZ = (Math.random() - 0.5) * 1.5;

        activeTexts.add(new DamageText(x + offsetX, y + offsetY, z + offsetZ, amount));
    }

    private static void tick() {
        Iterator<DamageText> iterator = activeTexts.iterator();
        while (iterator.hasNext()) {
            DamageText text = iterator.next();
            text.tick();
            if (text.isDead()) {
                iterator.remove();
            }
        }
    }

    private static void render(MatrixStack matrices, Camera camera, TextRenderer textRenderer,
            VertexConsumerProvider.Immediate vertexConsumers) {
        if (activeTexts.isEmpty())
            return;

        Vec3d camPos = camera.getPos();

        for (DamageText text : activeTexts) {
            matrices.push();

            // Перемещаемся к координатам попапа относительно камеры
            matrices.translate(text.x - camPos.x, text.y - camPos.y, text.z - camPos.z);

            // Поворачиваем текст к камере
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

            // Масштабирование
            float scale = 0.025f * text.getScale();
            matrices.scale(-scale, -scale, scale);

            String damageStr = String.format(java.util.Locale.US, "%.1f", text.amount);
            if (damageStr.endsWith(".0"))
                damageStr = damageStr.substring(0, damageStr.length() - 2);

            float alpha = text.getAlpha();
            int alphaInt = (int) (alpha * 255);
            int black = (alphaInt << 24) | 0x000000;
            int white = (alphaInt << 24) | 0xFFFFFF;

            float xOffset = (float) (-textRenderer.getWidth(damageStr) / 2);
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();

            // Создаем красивую черную обводку (Outline), рендеря текст со смещением
            // Смещение 1 - фон
            textRenderer.draw(damageStr, xOffset - 1, 0, black, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            textRenderer.draw(damageStr, xOffset + 1, 0, black, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            textRenderer.draw(damageStr, xOffset, -1, black, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            textRenderer.draw(damageStr, xOffset, 1, black, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);

            textRenderer.draw(damageStr, xOffset - 1, 0, black, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            textRenderer.draw(damageStr, xOffset + 1, 0, black, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            textRenderer.draw(damageStr, xOffset, -1, black, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            textRenderer.draw(damageStr, xOffset, 1, black, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);

            // Основной белый текст без дефолтной тени (false)
            textRenderer.draw(damageStr, xOffset, 0, white, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            textRenderer.draw(damageStr, xOffset, 0, white, false, matrix4f, vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);

            matrices.pop();
        }

        vertexConsumers.draw();
    }
}
