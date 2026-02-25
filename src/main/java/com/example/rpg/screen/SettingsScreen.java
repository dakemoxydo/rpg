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
        uiScale = Math.max(0.6f, Math.min(1.0f, Math.min(
                (float) this.width / 500,
                (float) this.height / 400)));
        panelWidth = (int) (380 * uiScale);
        panelHeight = (int) (340 * uiScale);
        padding = (int) (18 * uiScale);
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
        context.fill(0, 0, this.width, this.height, 0xAA000000); // Fullscreen vignette

        // Massive Glassmorphism Panel
        int bgDark = RenderUtils.withAlpha(el == MagicElement.NONE ? 0xFF050505 : el.bgDark, 0.95f);
        int bgMed = RenderUtils.withAlpha(el == MagicElement.NONE ? 0xFF111111 : el.bgMedium, 0.85f);
        context.fillGradient(panelX + 6, panelY + 6, panelX + panelWidth + 6, panelY + panelHeight + 6, 0x88000000,
                0x44000000);
        context.fillGradient(panelX, panelY, panelX + panelWidth, panelY + panelHeight, bgDark, bgMed);

        int primaryBorder = el == MagicElement.NONE ? 0xFF555555 : el.borderPrimary;
        int secondaryBorder = el == MagicElement.NONE ? 0xFF333333 : el.borderSecondary;
        RenderUtils.drawBorder(context, panelX - 2, panelY - 2, panelWidth + 4, panelHeight + 4, primaryBorder);
        RenderUtils.drawBorder(context, panelX - 1, panelY - 1, panelWidth + 2, panelHeight + 2, secondaryBorder);

        // Header Top Bar
        int headerHeight = (int) (32 * uiScale);
        context.fillGradient(panelX, panelY, panelX + panelWidth, panelY + headerHeight, 0x33FFFFFF, 0x05FFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(RpgLocale.get("settings.title")),
                panelX + panelWidth / 2, panelY + (int) (12 * uiScale),
                el == MagicElement.NONE ? 0xFFFFD700 : el.textTitle);
        context.fill(panelX + padding, panelY + headerHeight, panelX + panelWidth - padding, panelY + headerHeight + 1,
                secondaryBorder);

        // Static Row (Language)
        int rowHeight = (int) (26 * uiScale);
        int startY = panelY + (int) (45 * uiScale);
        context.drawTextWithShadow(this.textRenderer, Text.literal(RpgLocale.get("settings.language")),
                panelX + padding, startY + (int) (6 * uiScale), 0xFFFFFFFF);

        // Scrollable List Area
        int listY = startY + rowHeight + (int) (10 * uiScale);
        int listHeight = panelHeight - (listY - panelY) - (int) (40 * uiScale);

        RenderUtils.enableScissor(context, panelX, listY, panelWidth, listHeight);

        for (BindEntry entry : bindEntries) {
            int y = (int) (listY + entry.relativeY - scrollOffset);

            if (y + rowHeight > listY && y < listY + listHeight) {
                if (entry.isHeader) {
                    context.fillGradient(panelX + padding, y, panelX + panelWidth - padding - (int) (15 * uiScale),
                            y + (int) (18 * uiScale), 0x22FFFFFF, 0x00FFFFFF);
                    context.drawTextWithShadow(this.textRenderer, Text.literal(" " + entry.label), panelX + padding,
                            y + (int) (5 * uiScale), el == MagicElement.NONE ? 0xFFFFD700 : el.textTitle);
                    context.fill(panelX + padding, y + (int) (18 * uiScale),
                            panelX + panelWidth - padding - (int) (15 * uiScale), y + (int) (19 * uiScale),
                            secondaryBorder);
                } else {
                    entry.button.setY(y);
                    entry.button.setX(panelX + panelWidth - padding - entry.button.getWidth() - (int) (15 * uiScale));
                    entry.button.visible = true;
                    entry.button.render(context, mouseX, mouseY, delta);

                    context.drawTextWithShadow(this.textRenderer, Text.literal(entry.label),
                            panelX + padding + (int) (10 * uiScale), y + (int) (6 * uiScale), 0xFFAAAAAA);
                }
            } else if (!entry.isHeader) {
                entry.button.visible = false;
            }
        }

        RenderUtils.disableScissor(context);

        // Sleek MMO Scrollbar
        if (maxScroll > 0) {
            int scrollbarWidth = Math.max(3, (int) (6 * uiScale));
            int scrollbarX = panelX + panelWidth - padding;
            int scrollbarHeight = Math.max((int) (20 * uiScale),
                    (int) ((float) (listHeight * listHeight) / (listHeight + maxScroll)));
            int scrollbarY = listY + (int) ((listHeight - scrollbarHeight) * (scrollOffset / maxScroll));

            context.fillGradient(scrollbarX, listY, scrollbarX + scrollbarWidth, listY + listHeight, 0x66000000,
                    0x88000000); // Track
            RenderUtils.drawBorder(context, scrollbarX - 1, listY - 1, scrollbarWidth + 2, listHeight + 2, 0x44000000);

            context.fillGradient(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + scrollbarHeight,
                    RenderUtils.brighten(primaryBorder, 50),
                    RenderUtils.darken(primaryBorder, 20)); // Thumb
            // Inner glowing rim on thumb
            context.fill(scrollbarX, scrollbarY, scrollbarX + scrollbarWidth, scrollbarY + 1,
                    RenderUtils.brighten(primaryBorder, 80));
        }

        // Render Static Buttons over everything
        languageButton.render(context, mouseX, mouseY, delta);

        String hint = RpgLocale.get("settings.cancel_hint");
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(hint),
                panelX + panelWidth / 2, panelY + panelHeight - (int) (14 * uiScale), 0xFF777777);
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