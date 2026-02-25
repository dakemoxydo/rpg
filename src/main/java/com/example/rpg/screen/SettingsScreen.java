package com.example.rpg.screen;

import com.example.rpg.RpgClient;
import com.example.rpg.ability.Ability;
import com.example.rpg.ability.AbilityRegistry;
import com.example.rpg.ability.MagicSkillRegistry;
import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.config.RpgConfig;
import com.example.rpg.config.RpgLocale;
import com.example.rpg.stats.IPlayerStatsAccessor;
import com.example.rpg.stats.MagicElement;
import com.example.rpg.stats.PlayerStatsData;
import com.example.rpg.utils.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends Screen {

    private final Screen parent;
    private String waitingForBindId = null;
    private ButtonWidget languageButton;

    // Список всех кнопок биндов
    private final List<BindEntry> bindEntries = new ArrayList<>();

    private int panelWidth, panelHeight, panelX, panelY, padding;
    private float uiScale;

    // Скроллинг
    private double scrollOffset = 0;
    private int maxScroll = 0;

    // Вспомогательный класс для записи в списке
    private record BindEntry(String id, String label, ButtonWidget button, int relativeY, boolean isHeader) {
    }

    public SettingsScreen(Screen parent) {
        super(Text.literal("RPG Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        calculateAdaptiveSizes();
        bindEntries.clear();
        scrollOffset = 0; // Сброс скролла при ресайзе

        int buttonWidth = (int) (80 * uiScale);
        int buttonHeight = (int) (18 * uiScale);
        int buttonX = panelX + panelWidth - padding - buttonWidth;
        int rowHeight = (int) (24 * uiScale);

        int startY = panelY + (int) (45 * uiScale);

        // --- Статические кнопки ---

        // 1. Language
        languageButton = ButtonWidget.builder(
                Text.literal(RpgConfig.get().isRussian() ? "Русский" : "English"),
                button -> {
                    RpgConfig.get().setLanguage(RpgConfig.get().isRussian() ? "en" : "ru");
                    this.init(); // Перезагрузка для применения языка
                }).dimensions(buttonX, startY, buttonWidth, buttonHeight).build();
        this.addDrawableChild(languageButton);

        // Получаем StatsData
        PlayerStatsData data = null;
        if (this.client != null && this.client.player instanceof IPlayerStatsAccessor accessor) {
            data = accessor.rpg_getStatsData();
        }

        // --- Скроллируемый список ---

        int listCurrentY = 0; // Y внутри списка (относительно верха списка)

        // Основные
        bindEntries.add(new BindEntry(null, RpgLocale.get("settings.category.main"), null, listCurrentY, true));
        listCurrentY += (int) (15 * uiScale);

        ButtonWidget menuBtn = ButtonWidget.builder(
                Text.literal(getShortKeyName(RpgConfig.get().getOpenMenuKey())),
                button -> {
                    waitingForBindId = "MENU";
                    button.setMessage(Text.literal("..."));
                }).dimensions(buttonX, 0, buttonWidth, buttonHeight).build();
        this.addDrawableChild(menuBtn);
        bindEntries.add(new BindEntry("MENU", RpgLocale.get("settings.menu_key"), menuBtn, listCurrentY, false));
        listCurrentY += rowHeight;

        listCurrentY += 5; // Отступ после группы

        // Скилы (проверка на наличие)
        boolean hasSkills = false;
        if (data != null) {
            for (Ability ability : AbilityRegistry.getAll()) {
                if (data.hasAbility(ability.getId())) {
                    hasSkills = true;
                    break;
                }
            }
        }

        if (hasSkills) {
            bindEntries.add(new BindEntry(null, RpgLocale.get("settings.category.skills"), null, listCurrentY, true));
            listCurrentY += (int) (15 * uiScale);

            for (Ability ability : AbilityRegistry.getAll()) {
                if (data.hasAbility(ability.getId())) {
                    addBindEntry(ability.getId(), RpgLocale.getAbilityName(ability.getId()),
                            ability.getDefaultKey(), buttonX, buttonWidth, buttonHeight, listCurrentY);
                    listCurrentY += rowHeight;
                }
            }
            listCurrentY += 5; // Отступ после группы
        }

        // Магические способности
        if (data != null && data.hasElement()) {
            MagicElement element = data.getElement();
            if (element != MagicElement.NONE) {
                List<MagicAbility> abilities = MagicSkillRegistry.getAbilitiesForElement(element);

                boolean hasActive = false;
                for (MagicAbility ability : abilities) {
                    if (ability.getManaCost() > 0 && data.hasMagicSkill(ability.getId())) {
                        hasActive = true;
                        break;
                    }
                }

                if (hasActive) {
                    // Заголовок стихии
                    bindEntries.add(new BindEntry(null,
                            RpgLocale.get("settings.category.element") + RpgLocale.getElementName(element.name()), null,
                            listCurrentY, true));
                    listCurrentY += (int) (15 * uiScale);

                    for (MagicAbility ability : abilities) {
                        if (ability.getManaCost() > 0 && data.hasMagicSkill(ability.getId())) { // Только активные
                            addBindEntry(ability.getId(), RpgLocale.getSkillName(ability.getId()),
                                    ability.getDefaultKey(), buttonX, buttonWidth, buttonHeight, listCurrentY);
                            listCurrentY += rowHeight;
                        }
                    }
                    listCurrentY += 5; // Отступ после группы
                }
            }
        }

        // Высота области просмотра списка
        int listViewHeight = panelHeight - (startY + rowHeight + 10 - panelY) - (int) (30 * uiScale);
        maxScroll = Math.max(0, listCurrentY - listViewHeight);

        // Кнопка назад
        int backWidth = (int) (70 * uiScale);
        this.addDrawableChild(ButtonWidget.builder(Text.literal(RpgLocale.get("settings.back")), button -> close())
                .dimensions(panelX + (panelWidth - backWidth) / 2, panelY + panelHeight - (int) (28 * uiScale),
                        backWidth, buttonHeight)
                .build());
    }

    private void addBindEntry(String id, String name, int defaultKey, int x, int w, int h, int relY) {
        int key = RpgConfig.get().getKeybind(id, defaultKey);
        ButtonWidget btn = ButtonWidget.builder(
                Text.literal(getShortKeyName(key)),
                button -> {
                    waitingForBindId = id;
                    button.setMessage(Text.literal("..."));
                }).dimensions(x, 0, w, h).build();

        this.addDrawableChild(btn);
        bindEntries.add(new BindEntry(id, name, btn, relY, false));
    }

    private void calculateAdaptiveSizes() {
        uiScale = Math.max(0.7f, Math.min(1.0f, Math.min(
                (float) (this.width - 40) / 260,
                (float) (this.height - 40) / 280)));
        panelWidth = (int) (260 * uiScale);
        panelHeight = (int) (280 * uiScale);
        padding = (int) (12 * uiScale);
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MagicElement el = MagicElement.NONE;
        if (this.client != null && this.client.player instanceof IPlayerStatsAccessor accessor) {
            PlayerStatsData data = accessor.rpg_getStatsData();
            if (data != null)
                el = data.getElement();
        }

        renderBackground(context);

        // Фон панели
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, el.bgDark);
        RenderUtils.drawBorder(context, panelX - 2, panelY - 2, panelWidth + 4, panelHeight + 4, el.borderPrimary);

        // Заголовок
        int headerHeight = (int) (24 * uiScale);
        context.fill(panelX + 2, panelY + 2, panelX + panelWidth - 2, panelY + headerHeight, 0x15FFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(RpgLocale.get("settings.title")),
                panelX + panelWidth / 2, panelY + (int) (7 * uiScale), el.textTitle);
        context.fill(panelX + padding, panelY + headerHeight + 1, panelX + panelWidth - padding,
                panelY + headerHeight + 2, el.borderSecondary);

        // Статические строки (Только Язык)
        int rowHeight = (int) (24 * uiScale);
        int startY = panelY + (int) (45 * uiScale);

        context.drawTextWithShadow(this.textRenderer, Text.literal(RpgLocale.get("settings.language")),
                panelX + padding, startY + 4, el.textPrimary);

        // Отрисовка скроллируемого списка
        int listY = startY + rowHeight + 10;
        int listHeight = panelHeight - (listY - panelY) - (int) (35 * uiScale);

        // Используем RenderUtils для правильной обрезки
        RenderUtils.enableScissor(context, panelX, listY, panelWidth, listHeight);

        for (BindEntry entry : bindEntries) {
            int y = (int) (listY + entry.relativeY - scrollOffset);

            // Рендерим только если элемент виден
            if (y + rowHeight > listY && y < listY + listHeight) {
                if (entry.isHeader) {
                    context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(entry.label),
                            panelX + panelWidth / 2, y + 4, 0xFFFFFFFF);
                } else {
                    entry.button.setY(y);
                    entry.button.visible = true;
                    entry.button.render(context, mouseX, mouseY, delta);

                    context.drawTextWithShadow(this.textRenderer, Text.literal(entry.label),
                            panelX + padding, y + 5, el.textPrimary);
                }
            } else if (!entry.isHeader) {
                entry.button.visible = false;
            }
        }

        RenderUtils.disableScissor(context);

        // Скроллбар
        if (maxScroll > 0) {
            int scrollbarX = panelX + panelWidth - 6;
            int scrollbarHeight = (int) ((float) (listHeight * listHeight) / (listHeight + maxScroll));
            int scrollbarY = listY + (int) ((listHeight - scrollbarHeight) * (scrollOffset / maxScroll));

            context.fill(scrollbarX, listY, scrollbarX + 4, listY + listHeight, 0xFF202020); // Трек
            context.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, el.borderPrimary); // Ползунок
        }

        // Рендер статических кнопок поверх списка (чтобы не перекрывались)
        languageButton.render(context, mouseX, mouseY, delta);

        // Подсказка
        String hint = RpgLocale.get("settings.cancel_hint");
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(hint),
                panelX + panelWidth / 2, panelY + panelHeight - (int) (12 * uiScale), el.textSecondary);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - amount * 20));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (waitingForBindId != null) {
            if (waitingForBindId.equals("MENU")) {
                RpgConfig.get().setOpenMenuKey(button);
                RpgClient.updateMenuKeyBind(button);
            } else {
                RpgConfig.get().setKeybind(waitingForBindId, button);
            }
            waitingForBindId = null;
            updateButtonLabels();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForBindId != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForBindId = null;
                updateButtonLabels();
            } else {
                if (waitingForBindId.equals("MENU")) {
                    RpgConfig.get().setOpenMenuKey(keyCode);
                    RpgClient.updateMenuKeyBind(keyCode);
                } else {
                    RpgConfig.get().setKeybind(waitingForBindId, keyCode);
                }
                waitingForBindId = null;
                updateButtonLabels();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateButtonLabels() {
        for (BindEntry entry : bindEntries) {
            if (!entry.isHeader) {
                if ("MENU".equals(entry.id)) {
                    entry.button.setMessage(Text.literal(getShortKeyName(RpgConfig.get().getOpenMenuKey())));
                } else {
                    int key = RpgConfig.get().getKeybind(entry.id, GLFW.GLFW_KEY_UNKNOWN);
                    entry.button.setMessage(Text.literal(getShortKeyName(key)));
                }
            }
        }
    }

    private String getShortKeyName(int keyCode) {
        String name = RpgConfig.getKeyName(keyCode);
        return name.length() > 8 ? name.substring(0, 6) + ".." : name;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}