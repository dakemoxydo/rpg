package com.example.rpg.screen;

import com.example.rpg.ability.Ability;
import com.example.rpg.ability.AbilityRegistry;
import com.example.rpg.ability.magic.MagicAbility;
import com.example.rpg.config.RpgLocale;
import com.example.rpg.network.StatsNetworking;
import com.example.rpg.stats.*;
import com.example.rpg.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.List;

public class StatsScreen extends Screen {

    private float uiScale;
    private int panelX, panelY, panelW, panelH;
    private int contentX, contentY, contentW, contentH;
    private int sidebarW;

    private enum Tab {
        STATS, SKILLS, MAGIC
    }

    private Tab currentTab = Tab.STATS;
    private PlayerStatsData cachedData;
    private boolean resetConfirmStats = false;
    private boolean resetConfirmMagic = false;
    private long resetConfirmTime = 0;

    private MagicAbility selectedMagicAbility = null;
    private int magicUpgradeBtnX = 0, magicUpgradeBtnY = 0, magicUpgradeBtnW = 0, magicUpgradeBtnH = 0;

    // Lerp animation for background
    private float bgR = 5, bgG = 5, bgB = 5;

    public StatsScreen() {
        super(Text.literal("RPG Hub"));
    }

    @Override
    protected void init() {
        super.init();
        cachedData = getPlayerData();
        if (cachedData != null && !cachedData.hasElement()) {
            MinecraftClient.getInstance()
                    .execute(() -> MinecraftClient.getInstance().setScreen(new ElementSelectionScreen()));
            return;
        }
        calculateLayout();
    }

    private PlayerStatsData getPlayerData() {
        if (this.client != null && this.client.player instanceof IPlayerStatsAccessor accessor) {
            return accessor.rpg_getStatsData();
        }
        return null;
    }

    private void calculateLayout() {
        uiScale = Math.max(0.6f, Math.min(1.0f, Math.min(
                (float) this.width / 500, (float) this.height / 350)));
        panelW = (int) (this.width * 0.82f);
        panelH = (int) (this.height * 0.82f);
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        sidebarW = (int) (panelW * 0.2f);
        contentX = panelX + sidebarW + 2;
        contentY = panelY;
        contentW = panelW - sidebarW - 2;
        contentH = panelH;
    }

    // ==================== RENDERING ====================

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        cachedData = getPlayerData();
        if (cachedData == null)
            return;
        MagicElement el = cachedData.getElement();

        // Lerp background to element color
        int targetBg = el != MagicElement.NONE ? RenderUtils.withAlpha(el.bgDark, 0.6f) : 0xFF050505;
        bgR = RenderUtils.lerp(bgR, ((targetBg >> 16) & 0xFF), 0.08f);
        bgG = RenderUtils.lerp(bgG, ((targetBg >> 8) & 0xFF), 0.08f);
        bgB = RenderUtils.lerp(bgB, (targetBg & 0xFF), 0.08f);
        int currentBg = 0xDD000000 | ((int) bgR << 16) | ((int) bgG << 8) | (int) bgB;
        context.fill(0, 0, this.width, this.height, currentBg);

        // Sidebar panel (with shadow to match content panel)
        context.fill(panelX + 4, panelY + 4, panelX + sidebarW + 4, panelY + panelH + 4, 0x66000000);
        int sidebarBgTop = RenderUtils.withAlpha(0xFF0a0a0a, 0.95f);
        int sidebarBgBot = RenderUtils.withAlpha(0xFF050505, 0.95f);
        context.fillGradient(panelX, panelY, panelX + sidebarW, panelY + panelH, sidebarBgTop, sidebarBgBot);
        float sideGlow = (float) (Math.sin(System.currentTimeMillis() % 4000 / 4000.0 * Math.PI * 2) * 0.5 + 0.5);
        int sideOuterBorder = RenderUtils.blendColors(0xFF1a1a1a, 0xFF3a3a3a, sideGlow);
        RenderUtils.drawBorder(context, panelX - 1, panelY - 1, sidebarW + 2, panelH + 2, sideOuterBorder);
        RenderUtils.drawBorder(context, panelX, panelY, sidebarW, panelH, 0xFF2a2a2a);

        // Content panel
        int cBgTop = RenderUtils.withAlpha(el != MagicElement.NONE ? el.bgDark : 0xFF0d0d0d, 0.9f);
        int cBgBot = RenderUtils.withAlpha(el != MagicElement.NONE ? el.bgMedium : 0xFF151515, 0.7f);
        int borderP = el != MagicElement.NONE ? el.borderPrimary : 0xFF555555;
        int borderS = el != MagicElement.NONE ? el.borderSecondary : 0xFF333333;
        RenderUtils.drawGlassPanelAnimated(context, contentX, contentY, contentW, contentH,
                cBgTop, cBgBot, borderP, borderS);

        // Sidebar title
        context.drawCenteredTextWithShadow(this.textRenderer, RpgLocale.get("menu.title"),
                panelX + sidebarW / 2, panelY + (int) (14 * uiScale), 0xFFFFD700);

        // Sidebar header line
        context.fill(panelX + 6, panelY + (int) (28 * uiScale),
                panelX + sidebarW - 6, panelY + (int) (29 * uiScale), 0xFF333333);

        // Tab buttons
        drawTab(context, Tab.STATS, RpgLocale.get("tab.stats"), 0, mouseX, mouseY, el);
        drawTab(context, Tab.SKILLS, RpgLocale.get("tab.skills"), 1, mouseX, mouseY, el);
        drawTab(context, Tab.MAGIC, RpgLocale.get("tab.magic"), 2, mouseX, mouseY, el);

        // Close button (Top)
        int closeX = panelX + 6;
        int closeY = panelY + panelH - (int) (28 * uiScale);
        int menuBtnW = sidebarW - 12;
        int menuBtnH = (int) (20 * uiScale);
        RenderUtils.drawCustomButton(context, this.textRenderer, closeX, closeY, menuBtnW, menuBtnH,
                RpgLocale.get("menu.close"), mouseX, mouseY,
                0x33FF4444, 0x55FF4444, 0xFFFF6666, 0xFF883333);

        // Settings gear button (Bottom)
        int gearY = closeY - menuBtnH - (int) (4 * uiScale);
        RenderUtils.drawCustomButton(context, this.textRenderer, closeX, gearY, menuBtnW, menuBtnH,
                "⚙ " + RpgLocale.get("settings.title").replace("⚙ ", ""),
                mouseX, mouseY, 0x33FFFFFF, 0x55FFFFFF, 0xFFCCCCCC, 0xFF444444);

        // Content tabs
        int titleColor = el != MagicElement.NONE ? el.textTitle : 0xFFFFD700;
        switch (currentTab) {
            case STATS -> renderStatsTab(context, mouseX, mouseY, el, titleColor);
            case SKILLS -> renderSkillsTab(context, mouseX, mouseY, el, titleColor);
            case MAGIC -> renderMagicTab(context, mouseX, mouseY, el, titleColor);
        }
    }

    private void drawTab(DrawContext context, Tab tab, String name, int index,
            int mouseX, int mouseY, MagicElement el) {
        int tabY = panelY + (int) (36 * uiScale) + index * (int) (26 * uiScale);
        int tabX = panelX + 4;
        int tabW = sidebarW - 8;
        int tabH = (int) (22 * uiScale);
        boolean selected = currentTab == tab;
        boolean hovered = mouseX >= tabX && mouseX <= tabX + tabW && mouseY >= tabY && mouseY <= tabY + tabH;

        int bg = selected
                ? RenderUtils.withAlpha(el != MagicElement.NONE ? el.borderPrimary : 0xFFFFD700, 0.25f)
                : (hovered ? 0x22FFFFFF : 0x00000000);
        context.fill(tabX, tabY, tabX + tabW, tabY + tabH, bg);
        if (selected) {
            context.fill(tabX, tabY, tabX + 2, tabY + tabH,
                    el != MagicElement.NONE ? el.borderPrimary : 0xFFFFD700);
        }

        int textColor = selected ? 0xFFFFFFFF : (hovered ? 0xFFDDDDDD : 0xFF888888);
        context.drawTextWithShadow(this.textRenderer, name,
                tabX + (int) (10 * uiScale), tabY + (tabH - textRenderer.fontHeight) / 2, textColor);
    }

    // ==================== STATS TAB ====================

    private void renderStatsTab(DrawContext context, int mouseX, int mouseY, MagicElement el, int titleColor) {
        int pad = (int) (16 * uiScale);
        int cx = contentX + pad;
        int cy = contentY + pad;

        // Header
        String levelText = RpgLocale.get("menu.level") + " " + cachedData.getCurrentLevel();
        context.drawTextWithShadow(this.textRenderer, levelText, cx, cy, titleColor);
        String spText = "SP: " + cachedData.getSkillPoints();
        context.drawTextWithShadow(this.textRenderer, spText,
                contentX + contentW - pad - textRenderer.getWidth(spText), cy, 0xFF55FF55);

        // XP bar
        int barY = cy + (int) (14 * uiScale);
        int barW = contentW - pad * 2;
        RenderUtils.drawProgressBar(context, cx, barY, barW, (int) (5 * uiScale),
                cachedData.getXpProgress(),
                el != MagicElement.NONE ? el.xpBarFill : 0xFF4488FF,
                el != MagicElement.NONE ? el.xpBarBorder : 0xFF2266CC,
                0x44000000);

        // Separator after header
        int sepY = barY + (int) (10 * uiScale);
        int borderS = el != MagicElement.NONE ? el.borderSecondary : 0xFF333333;
        context.fillGradient(cx, sepY, cx + barW / 2, sepY + 1, borderS, 0x00000000);
        context.fillGradient(contentX + contentW - pad - barW / 2, sepY, contentX + contentW - pad, sepY + 1,
                0x00000000, borderS);

        // Stat rows
        int startY = barY + (int) (18 * uiScale);
        int rowH = (int) (26 * uiScale);
        int btnSize = (int) (16 * uiScale);

        for (int i = 0; i < StatType.values().length; i++) {
            StatType stat = StatType.values()[i];
            int y = startY + i * rowH;
            int level = cachedData.getStatLevel(stat);
            int statColor = stat.getColor(el);

            // Name
            context.drawTextWithShadow(this.textRenderer, RpgLocale.get("stat." + stat.name().toLowerCase()),
                    cx, y + (rowH - textRenderer.fontHeight) / 2, 0xFFDDDDDD);

            // Value
            String val = String.valueOf(level);
            int valX = cx + (int) (75 * uiScale);
            context.drawTextWithShadow(this.textRenderer, val,
                    valX, y + (rowH - textRenderer.fontHeight) / 2, statColor);

            // Progress bar
            int pX = cx + (int) (95 * uiScale);
            int pW = contentW - pad * 2 - (int) (130 * uiScale);
            int pH = (int) (5 * uiScale);
            int pY = y + (rowH - pH) / 2;
            RenderUtils.drawProgressBar(context, pX, pY, pW, pH,
                    level / (float) stat.getMaxLevel(), statColor, RenderUtils.darken(statColor, 40), 0x44000000);

            // Upgrade button
            int btnX = contentX + contentW - pad - btnSize;
            int btnY = y + (rowH - btnSize) / 2;
            boolean canUp = cachedData.canUpgrade(stat);
            RenderUtils.drawUpgradeButton(context, this.textRenderer, btnX, btnY, btnSize,
                    mouseX, mouseY, statColor, canUp);
        }

        // Reset button with confirmation state
        int resetY = contentY + contentH - (int) (30 * uiScale);
        int resetW = (int) (90 * uiScale);
        int resetH = (int) (18 * uiScale);
        boolean isConfirmingStats = resetConfirmStats && (System.currentTimeMillis() - resetConfirmTime < 3000);
        String resetText = isConfirmingStats ? RpgLocale.get("menu.reset.confirm") : RpgLocale.get("menu.reset");
        int resetBg = isConfirmingStats ? 0x66FF2222 : 0x33FF4444;
        int resetHover = isConfirmingStats ? 0x88FF2222 : 0x55FF4444;
        RenderUtils.drawCustomButton(context, this.textRenderer,
                cx, resetY, resetW, resetH, resetText,
                mouseX, mouseY, resetBg, resetHover, 0xFFFF6666, 0xFF883333);
    }

    // ==================== SKILLS TAB ====================

    private void renderSkillsTab(DrawContext context, int mouseX, int mouseY, MagicElement el, int titleColor) {
        int pad = (int) (16 * uiScale);
        int cx = contentX + pad;
        int cy = contentY + pad;

        String spText = "SP: " + cachedData.getSkillPoints();
        context.drawTextWithShadow(this.textRenderer, spText, cx, cy, 0xFF55FF55);

        // Separator after header
        int sepY = cy + (int) (12 * uiScale);
        int borderS = el != MagicElement.NONE ? el.borderSecondary : 0xFF333333;
        context.fillGradient(cx, sepY, cx + (contentW - pad * 2) / 2, sepY + 1, borderS, 0x00000000);

        int startY = cy + (int) (20 * uiScale);
        int rowH = (int) (30 * uiScale);
        int btnSize = (int) (16 * uiScale);

        int index = 0;
        for (Ability ability : AbilityRegistry.getPhysicalAbilities()) {
            int y = startY + index * rowH;
            int level = cachedData.getSkillLevel(ability.getId());
            boolean isMax = level >= ability.getMaxLevel();

            // Icon
            context.drawTextWithShadow(this.textRenderer, ability.getIcon(), cx,
                    y + (rowH - textRenderer.fontHeight) / 2, 0xFFFFFFFF);

            // Name & Power
            String name = RpgLocale.getAbilityName(ability.getId());
            float power = ability.getPower(Math.max(1, level));
            String effectKey = getEffectKey(ability.getId());
            String powerStr = power > 0 ? " [" + RpgLocale.get(effectKey) + String.format("%.1f", power) + "]" : "";

            context.drawTextWithShadow(this.textRenderer, name + powerStr,
                    cx + (int) (18 * uiScale), y + (rowH - textRenderer.fontHeight) / 2, 0xFFDDDDDD);

            // Level
            String lvlStr = level + "/" + ability.getMaxLevel();
            int lvlColor = isMax ? 0xFFFFD700 : 0xFFAAAAAA;
            int lvlX = cx + (int) (120 * uiScale);
            context.drawTextWithShadow(this.textRenderer, lvlStr, lvlX, y + (rowH - textRenderer.fontHeight) / 2,
                    lvlColor);

            // Progress bar
            int pX = cx + (int) (160 * uiScale);
            int pW = contentW - pad * 2 - (int) (195 * uiScale);
            int accentColor = el != MagicElement.NONE ? el.borderPrimary : 0xFF4488FF;
            RenderUtils.drawProgressBar(context, pX, y + (rowH - (int) (4 * uiScale)) / 2,
                    pW, (int) (4 * uiScale), level / (float) ability.getMaxLevel(),
                    accentColor, RenderUtils.darken(accentColor, 40), 0x44000000);

            // Upgrade button
            if (!isMax) {
                int btnX = contentX + contentW - pad - btnSize;
                int btnY = y + (rowH - btnSize) / 2;
                boolean canUp = cachedData.canUpgradeSkill(ability.getId(), ability.getMaxLevel(),
                        ability.getCostPerLevel());
                RenderUtils.drawUpgradeButton(context, this.textRenderer, btnX, btnY, btnSize,
                        mouseX, mouseY, accentColor, canUp);
            }
            index++;
        }
    }

    // ==================== MAGIC TAB ====================

    private void renderMagicTab(DrawContext context, int mouseX, int mouseY, MagicElement el, int titleColor) {
        int pad = (int) (16 * uiScale);
        int cx = contentX + pad;
        int cy = contentY + pad;

        String spText = "SP: " + cachedData.getSkillPoints();
        context.drawTextWithShadow(this.textRenderer, spText, cx, cy, 0xFF55FF55);

        String elName = el.getIcon() + " " + RpgLocale.getElementName(el.name().toLowerCase());
        context.drawTextWithShadow(this.textRenderer, elName,
                cx + (int) (60 * uiScale), cy, el.textPrimary);

        // Radial skill tree
        List<MagicAbility> abilities = AbilityRegistry.getMagicAbilities(el);
        int treeCx = contentX + (int) (contentW * 0.35f);
        int treeCy = contentY + contentH / 2 + (int) (15 * uiScale);

        // Draw tethers
        for (MagicAbility ability : abilities) {
            if (ability.getTier() == 0)
                continue;
            int tier = ability.getTier();
            int branch = ability.getBranch();
            int radius = tier * (int) (55 * uiScale);
            double angle = Math.PI + (branch - 2) * (Math.PI / 4.0);
            int x = treeCx + (int) (Math.sin(angle) * radius);
            int y = treeCy - (int) (Math.cos(angle) * radius);

            int parentRadius = (tier - 1) * (int) (55 * uiScale);
            double parentAngle = tier - 1 > 0 ? Math.PI + (branch - 2) * (Math.PI / 4.0) : Math.PI / 2;
            int px = treeCx + (int) (Math.sin(parentAngle) * parentRadius);
            int py = treeCy - (int) (Math.cos(parentAngle) * parentRadius);

            boolean unlocked = cachedData.getSkillLevel(ability.getId()) > 0;
            int lineColor = unlocked ? RenderUtils.withAlpha(el.borderPrimary, 0.6f) : 0x33555555;
            drawLine(context, px, py, x, y, lineColor);
        }

        // Draw nodes
        int nodeW = (int) (70 * uiScale);
        int nodeH = (int) (22 * uiScale);
        for (MagicAbility ability : abilities) {
            int tier = ability.getTier();
            int branch = ability.getBranch();
            int radius = tier * (int) (55 * uiScale);
            double angle = tier == 0 ? Math.PI / 2 : Math.PI + (branch - 2) * (Math.PI / 4.0);
            int nx = treeCx + (int) (Math.sin(angle) * radius) - nodeW / 2;
            int ny = treeCy - (int) (Math.cos(angle) * radius) - nodeH / 2;

            int level = cachedData.getSkillLevel(ability.getId());
            boolean isMax = level >= ability.getMaxLevel();
            boolean meetsReqs = cachedData.meetsRequirements(ability.getRequiredSkills());
            boolean hovered = mouseX >= nx && mouseX <= nx + nodeW && mouseY >= ny && mouseY <= ny + nodeH;

            // Node background
            boolean isSelected = selectedMagicAbility != null && selectedMagicAbility.getId().equals(ability.getId());
            int nodeBg = level > 0
                    ? RenderUtils.withAlpha(el.bgLight, hovered || isSelected ? 0.9f : 0.7f)
                    : (meetsReqs ? (hovered || isSelected ? 0x44FFFFFF : 0x33333333) : 0x22222222);
            context.fill(nx, ny, nx + nodeW, ny + nodeH, nodeBg);

            int nodeBorder = isMax ? 0xFFFFD700
                    : (level > 0 ? el.borderPrimary : (meetsReqs ? 0xFF555555 : 0xFF333333));
            if (isSelected) {
                nodeBorder = 0xFFFFFFFF; // Белая обводка для выбранного
            }
            RenderUtils.drawBorder(context, nx, ny, nodeW, nodeH, nodeBorder);

            // Node text
            String name = RpgLocale.getSkillName(ability.getId());
            if (textRenderer.getWidth(name) > nodeW - 6) {
                name = name.substring(0, Math.min(name.length(), 5)) + "..";
            }
            int textColor = level > 0 ? 0xFFFFFFFF : (meetsReqs ? 0xFFAAAAAA : 0xFF555555);
            context.drawCenteredTextWithShadow(this.textRenderer, name, nx + nodeW / 2,
                    ny + (nodeH - textRenderer.fontHeight) / 2, textColor);
        }

        // Details Panel
        int panelX = contentX + (int) (contentW * 0.65f);
        int panelW = contentW - (int) (contentW * 0.65f) - pad;
        int panelY = contentY + pad;
        int panelH = contentH - pad * 2 - (int) (30 * uiScale);
        renderSkillDetailsPanel(context, mouseX, mouseY, el, pad, panelX, panelY, panelW, panelH);

        // Reset element button with confirmation state
        int resetY = contentY + contentH - (int) (30 * uiScale);
        int resetW = (int) (110 * uiScale);
        int resetH = (int) (18 * uiScale);
        boolean isConfirmingMagic = resetConfirmMagic && (System.currentTimeMillis() - resetConfirmTime < 3000);
        String resetText = isConfirmingMagic ? RpgLocale.get("menu.reset.confirm") : RpgLocale.get("menu.reset");
        int resetBg = isConfirmingMagic ? 0x66FF2222 : 0x33FF4444;
        int resetHover = isConfirmingMagic ? 0x88FF2222 : 0x55FF4444;
        RenderUtils.drawCustomButton(context, this.textRenderer, cx, resetY, resetW, resetH, resetText,
                mouseX, mouseY, resetBg, resetHover, 0xFFFF6666, 0xFF883333);
    }

    private void renderSkillDetailsPanel(DrawContext context, int mouseX, int mouseY, MagicElement el, int pad, int x,
            int y, int w, int availableH) {

        if (selectedMagicAbility == null) {
            this.magicUpgradeBtnW = 0;
            RenderUtils.drawGlassPanel(context, x, y, w, availableH, el.bgMedium, el.bgDark, el.borderPrimary,
                    el.borderSecondary);
            String hint = "▶ " + RpgLocale.get("element.click_hint");
            context.drawCenteredTextWithShadow(this.textRenderer, hint, x + w / 2, y + availableH / 2, 0xFF888888);
            return;
        }

        int level = cachedData.getSkillLevel(selectedMagicAbility.getId());
        boolean isMax = level >= selectedMagicAbility.getMaxLevel();
        boolean meetsReqs = cachedData.meetsRequirements(selectedMagicAbility.getRequiredSkills());

        // Расчет высоты контента
        int currentDynamicHeight = pad * 2; // Паддинги
        currentDynamicHeight += 15 * uiScale; // Header
        currentDynamicHeight += 20 * uiScale; // Level
        currentDynamicHeight += 12 * uiScale; // Resource cost
        currentDynamicHeight += 12 * uiScale; // Cooldown
        currentDynamicHeight += 20 * uiScale; // Power/Damage

        String desc = RpgLocale.getSkillDesc(selectedMagicAbility.getId());
        java.util.List<net.minecraft.text.OrderedText> descLines = this.textRenderer.wrapLines(Text.literal(desc),
                w - pad * 2);
        currentDynamicHeight += descLines.size() * (this.textRenderer.fontHeight + 2); // Description
        currentDynamicHeight += 10 * uiScale; // Отступ перед апгрейдом

        String upgradeDesc = selectedMagicAbility.getUpgradeDescription(level);
        java.util.List<net.minecraft.text.OrderedText> upgLines = this.textRenderer
                .wrapLines(Text.literal(RpgLocale.get("skill.upgrade_info") + upgradeDesc), w - pad * 2);
        currentDynamicHeight += upgLines.size() * (this.textRenderer.fontHeight + 2); // Upgrade info
        currentDynamicHeight += pad; // Padding before button

        int btnH = (int) (22 * uiScale);
        currentDynamicHeight += btnH; // Button

        // Центрирование
        int panelH = currentDynamicHeight;
        int centeredY = y + (availableH - panelH) / 2;
        centeredY = Math.max(y, centeredY); // Не вылезаем за верх

        RenderUtils.drawGlassPanel(context, x, centeredY, w, panelH, el.bgMedium, el.bgDark, el.borderPrimary,
                el.borderSecondary);

        // Header (Icon + Name)
        int textY = centeredY + pad;
        String name = selectedMagicAbility.getIcon() + " " + RpgLocale.getSkillName(selectedMagicAbility.getId());
        context.drawCenteredTextWithShadow(this.textRenderer, name, x + w / 2, textY,
                isMax ? 0xFFFFD700 : el.textPrimary);
        textY += (int) (15 * uiScale);

        // Level
        String lblLvl = RpgLocale.get("levelup.level") + " " + level + "/" + selectedMagicAbility.getMaxLevel();
        context.drawCenteredTextWithShadow(this.textRenderer, lblLvl, x + w / 2, textY, 0xFFAAAAAA);
        textY += (int) (20 * uiScale);

        // Stats (Cost, Cooldown)
        int statsX = x + pad;
        String resourceText = (selectedMagicAbility.usesStamina() ? RpgLocale.get("skill.stamina")
                : RpgLocale.get("skill.mana")) + selectedMagicAbility.getResourceCost(Math.max(1, level));
        context.drawTextWithShadow(this.textRenderer, resourceText, statsX, textY, 0xFF55FFFF);
        textY += (int) (12 * uiScale);

        String cdrText = RpgLocale.get("skill.cooldown")
                + (selectedMagicAbility.getCooldownTicks(Math.max(1, level)) / 20.0f)
                + RpgLocale.get("skill.sec");
        context.drawTextWithShadow(this.textRenderer, cdrText, statsX, textY, 0xFFFFAA00);
        textY += (int) (12 * uiScale);

        String effectKey = getEffectKey(selectedMagicAbility.getId());
        String powerText = RpgLocale.get(effectKey)
                + String.format("%.1f", selectedMagicAbility.getPower(Math.max(1, level)));
        context.drawTextWithShadow(this.textRenderer, powerText, statsX, textY, 0xFFFF5555);
        textY += (int) (20 * uiScale);

        // Description
        for (net.minecraft.text.OrderedText line : descLines) {
            context.drawTextWithShadow(this.textRenderer, line, statsX, textY, 0xFFCCCCCC);
            textY += this.textRenderer.fontHeight + 2;
        }

        textY += (int) (10 * uiScale);

        // Upgrade Info
        if (!isMax) {
            for (net.minecraft.text.OrderedText line : upgLines) {
                context.drawTextWithShadow(this.textRenderer, line, statsX, textY, 0xFF55FF55);
                textY += this.textRenderer.fontHeight + 2;
            }
        }

        // Upgrade Button
        int btnW = w - pad * 2;
        int btnX = x + pad;
        int btnY = centeredY + panelH - btnH - pad;
        this.magicUpgradeBtnX = btnX;
        this.magicUpgradeBtnY = btnY;
        this.magicUpgradeBtnW = btnW;
        this.magicUpgradeBtnH = btnH;

        if (isMax) {
            RenderUtils.drawCustomButton(context, this.textRenderer, btnX, btnY, btnW, btnH,
                    RpgLocale.get("skill.maxed_btn"),
                    mouseX, mouseY, 0x55555555, 0x55555555, 0xFF888888, 0xFF666666);
        } else if (meetsReqs) {
            int cost = selectedMagicAbility.getCostPerLevel();
            boolean canAfford = cachedData.getSkillPoints() >= cost;
            String btnText = RpgLocale.get("skill.upgrade_btn") + " (" + cost + " SP)";
            int textC = canAfford ? 0xFF55FF55 : 0xFFFF5555;
            RenderUtils.drawCustomButton(context, this.textRenderer, btnX, btnY, btnW, btnH, btnText,
                    mouseX, mouseY, el.bgMedium, el.bgLight, textC, el.borderSecondary);
        } else {
            RenderUtils.drawCustomButton(context, this.textRenderer, btnX, btnY, btnW, btnH,
                    RpgLocale.get("skill.locked"),
                    mouseX, mouseY, 0x55333333, 0x55333333, 0xFF555555, 0xFF222222);
        }
    }

    private void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        if (Math.abs(x2 - x1) > Math.abs(y2 - y1)) {
            context.fill(Math.min(x1, x2), y1, Math.max(x1, x2), y1 + 1, color);
            context.fill(x2, Math.min(y1, y2), x2 + 1, Math.max(y1, y2), color);
        } else {
            context.fill(x1, Math.min(y1, y2), x1 + 1, Math.max(y1, y2), color);
            context.fill(Math.min(x1, x2), y2, Math.max(x1, x2), y2 + 1, color);
        }
    }

    // ==================== INPUT HANDLING ====================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (cachedData == null)
            return false;
        int mx = (int) mouseX, my = (int) mouseY;
        MagicElement el = cachedData.getElement();
        int pad = (int) (16 * uiScale);

        // Tab clicks
        for (int i = 0; i < Tab.values().length; i++) {
            int tabY = panelY + (int) (36 * uiScale) + i * (int) (26 * uiScale);
            int tabX = panelX + 4;
            int tabW = sidebarW - 8;
            int tabH = (int) (22 * uiScale);
            if (mx >= tabX && mx <= tabX + tabW && my >= tabY && my <= tabY + tabH) {
                currentTab = Tab.values()[i];
                return true;
            }
        }

        // Close button (Top)
        int closeX = panelX + 6;
        int closeY = panelY + panelH - (int) (28 * uiScale);
        int menuBtnW = sidebarW - 12;
        int menuBtnH = (int) (20 * uiScale);
        if (mx >= closeX && mx <= closeX + menuBtnW && my >= closeY && my <= closeY + menuBtnH) {
            close();
            return true;
        }

        // Settings button (Bottom)
        int gearY = closeY - menuBtnH - (int) (4 * uiScale);
        if (mx >= closeX && mx <= closeX + menuBtnW && my >= gearY && my <= gearY + menuBtnH) {
            MinecraftClient.getInstance().setScreen(new SettingsScreen(this));
            return true;
        }

        // Content-specific clicks
        int btnSize = (int) (16 * uiScale);
        if (currentTab == Tab.STATS) {
            int barY = contentY + pad + (int) (14 * uiScale);
            int startY = barY + (int) (18 * uiScale);
            int rowH = (int) (26 * uiScale);
            for (int i = 0; i < StatType.values().length; i++) {
                StatType stat = StatType.values()[i];
                int y = startY + i * rowH;
                int btnX = contentX + contentW - pad - btnSize;
                int btnY = y + (rowH - btnSize) / 2;
                if (mx >= btnX && mx <= btnX + btnSize && my >= btnY && my <= btnY + btnSize) {
                    if (cachedData.canUpgrade(stat)) {
                        StatsNetworking.sendUpgradeRequest(stat);
                        playClickSound();
                    }
                    return true;
                }
            }
            // Reset with confirmation
            int resetY = contentY + contentH - (int) (30 * uiScale);
            int resetW = (int) (90 * uiScale);
            int resetH = (int) (18 * uiScale);
            int cx = contentX + pad;
            if (mx >= cx && mx <= cx + resetW && my >= resetY && my <= resetY + resetH) {
                if (resetConfirmStats && (System.currentTimeMillis() - resetConfirmTime < 3000)) {
                    StatsNetworking.sendResetRequest();
                    resetConfirmStats = false;
                    playClickSound();
                } else {
                    resetConfirmStats = true;
                    resetConfirmMagic = false;
                    resetConfirmTime = System.currentTimeMillis();
                    playClickSound();
                }
                return true;
            }
        } else if (currentTab == Tab.SKILLS) {
            int startY = contentY + pad + (int) (20 * uiScale);
            int rowH = (int) (30 * uiScale);
            int index = 0;
            for (Ability ability : AbilityRegistry.getPhysicalAbilities()) {
                int y = startY + index * rowH;
                int level = cachedData.getSkillLevel(ability.getId());
                if (level < ability.getMaxLevel()) {
                    int btnX = contentX + contentW - pad - btnSize;
                    int btnY = y + (rowH - btnSize) / 2;
                    if (mx >= btnX && mx <= btnX + btnSize && my >= btnY && my <= btnY + btnSize) {
                        StatsNetworking.sendUpgradeAbility(ability.getId());
                        playClickSound();
                        return true;
                    }
                }
                index++;
            }
        } else if (currentTab == Tab.MAGIC) {
            List<MagicAbility> abilities = AbilityRegistry.getMagicAbilities(el);
            int treeCx = contentX + (int) (contentW * 0.35f);
            int treeCy = contentY + contentH / 2 + (int) (15 * uiScale);
            int nodeW = (int) (70 * uiScale);
            int nodeH = (int) (22 * uiScale);
            for (MagicAbility ability : abilities) {
                int tier = ability.getTier();
                int branch = ability.getBranch();
                int radius = tier * (int) (55 * uiScale);
                double angle = tier == 0 ? Math.PI / 2 : Math.PI + (branch - 2) * (Math.PI / 4.0);
                int nx = treeCx + (int) (Math.sin(angle) * radius) - nodeW / 2;
                int ny = treeCy - (int) (Math.cos(angle) * radius) - nodeH / 2;
                if (mx >= nx && mx <= nx + nodeW && my >= ny && my <= ny + nodeH) {
                    selectedMagicAbility = ability;
                    playClickSound();
                    return true;
                }
            }

            // Upgrade Button click logic
            if (selectedMagicAbility != null && magicUpgradeBtnW > 0) {
                if (mx >= magicUpgradeBtnX && mx <= magicUpgradeBtnX + magicUpgradeBtnW && my >= magicUpgradeBtnY
                        && my <= magicUpgradeBtnY + magicUpgradeBtnH) {
                    int level = cachedData.getSkillLevel(selectedMagicAbility.getId());
                    if (level < selectedMagicAbility.getMaxLevel()
                            && cachedData.meetsRequirements(selectedMagicAbility.getRequiredSkills())) {
                        int cost = selectedMagicAbility.getCostPerLevel();
                        if (cachedData.getSkillPoints() >= cost) {
                            StatsNetworking.sendUpgradeAbility(selectedMagicAbility.getId());
                            playClickSound();
                        }
                    }
                    return true;
                }
            }
            // Reset element
            int resetY = contentY + contentH - (int) (30 * uiScale);
            int resetW = (int) (110 * uiScale);
            int resetH = (int) (18 * uiScale);
            int cx = contentX + pad;
            if (mx >= cx && mx <= cx + resetW && my >= resetY && my <= resetY + resetH) {
                if (resetConfirmMagic && (System.currentTimeMillis() - resetConfirmTime < 3000)) {
                    StatsNetworking.sendResetElement();
                    resetConfirmMagic = false;
                    playClickSound();
                    MinecraftClient.getInstance()
                            .execute(() -> MinecraftClient.getInstance().setScreen(new ElementSelectionScreen()));
                } else {
                    resetConfirmMagic = true;
                    resetConfirmStats = false;
                    resetConfirmTime = System.currentTimeMillis();
                    playClickSound();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private String getEffectKey(String skillId) {
        if (skillId.contains("heal"))
            return "skill.power.heal";
        if (skillId.contains("dash") || skillId.contains("gust"))
            return "skill.power.force";
        return "skill.power.damage";
    }

    private void playClickSound() {
        if (this.client != null) {
            this.client.getSoundManager().play(
                    PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}