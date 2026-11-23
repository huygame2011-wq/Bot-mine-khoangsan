package com.spicy.oreminer.ui;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.HudVisibilityManager;
import com.spicy.oreminer.core.ModuleManager;
import com.spicy.oreminer.core.SafetyManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SpicyMinerScreen extends Screen {
    private final Screen parent;

    private ButtonWidget toggleSystemButton;
    private ButtonWidget toggleHudButton;
    private ButtonWidget toggleOreSimButton;
    private ButtonWidget resetStopButton;

    public SpicyMinerScreen(Screen parent) {
        super(Text.literal("Spicy Ore Miner v1"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelWidth = 240;
        int panelHeight = 190;
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;

        int btnX = left + 20;
        int btnWidth = panelWidth - 40;
        int btnHeight = 20;
        int gap = 26;

        toggleSystemButton = ButtonWidget.builder(getSystemButtonLabel(), button -> {
            ConfigManager cfg = ConfigManager.get();
            if (SafetyManager.isEmergencyStopped() && !cfg.systemEnabled) {
                SafetyManager.clearEmergencyStop();
            }
            ModuleManager.setSystemEnabled(this.client, !cfg.systemEnabled);
            refreshButtonLabels();
        }).dimensions(btnX, top + 34, btnWidth, btnHeight).build();

        toggleHudButton = ButtonWidget.builder(getHudButtonLabel(), button -> {
            HudVisibilityManager.toggle();
            refreshButtonLabels();
        }).dimensions(btnX, top + 34 + gap, btnWidth, btnHeight).build();

        toggleOreSimButton = ButtonWidget.builder(getOreSimButtonLabel(), button -> {
            ConfigManager cfg = ConfigManager.get();
            cfg.oreSimEnabled = !cfg.oreSimEnabled;
            cfg.save();
            refreshButtonLabels();
        }).dimensions(btnX, top + 34 + gap * 2, btnWidth, btnHeight).build();

        resetStopButton = ButtonWidget.builder(Text.literal("Xóa trạng thái dừng khẩn cấp"), button -> {
            SafetyManager.clearEmergencyStop();
            refreshButtonLabels();
        }).dimensions(btnX, top + 34 + gap * 3, btnWidth, btnHeight).build();

        ButtonWidget closeButton = ButtonWidget.builder(Text.literal("Đóng"), button -> this.close())
                .dimensions(btnX, top + panelHeight - 28, btnWidth, btnHeight).build();

        this.addDrawableChild(toggleSystemButton);
        this.addDrawableChild(toggleHudButton);
        this.addDrawableChild(toggleOreSimButton);
        this.addDrawableChild(resetStopButton);
        this.addDrawableChild(closeButton);

        refreshButtonLabels();
    }

    private void refreshButtonLabels() {
        if (toggleSystemButton != null) {
            toggleSystemButton.setMessage(getSystemButtonLabel());
        }
        if (toggleHudButton != null) {
            toggleHudButton.setMessage(getHudButtonLabel());
        }
        if (toggleOreSimButton != null) {
            toggleOreSimButton.setMessage(getOreSimButtonLabel());
        }
        if (resetStopButton != null) {
            resetStopButton.active = SafetyManager.isEmergencyStopped();
        }
    }

    private Text getSystemButtonLabel() {
        ConfigManager cfg = ConfigManager.get();
        return Text.literal("Hệ thống đào: " + (cfg.systemEnabled ? "ĐANG BẬT" : "ĐANG TẮT"));
    }

    private Text getHudButtonLabel() {
        return Text.literal("HUD thông tin: " + (HudVisibilityManager.isVisible() ? "ĐANG HIỆN" : "ĐANG ẨN"));
    }

    private Text getOreSimButtonLabel() {
        ConfigManager cfg = ConfigManager.get();
        return Text.literal("Quét quặng: " + (cfg.oreSimEnabled ? "BẬT" : "TẮT"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = 240;
        int panelHeight = 190;
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;

        context.fill(0, 0, this.width, this.height, 0x88000000);
        context.fill(left, top, left + panelWidth, top + panelHeight, 0xE6120D1D);
        drawFrame(context, left, top, panelWidth, panelHeight, 0xFF9C27B0);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                left + panelWidth / 2,
                top + 11,
                0xFFE1BEE7
        );

        String statusLine = SafetyManager.isEmergencyStopped()
                ? "Cảnh báo: " + SafetyManager.getStopReason()
                : "Phím: K (Menu) | H (HUD) | G (Hệ thống)";
        int statusColor = SafetyManager.isEmergencyStopped() ? 0xFFFF5252 : 0xFFB39DDB;
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(statusLine),
                left + panelWidth / 2,
                top + 22,
                statusColor
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Tránh gọi nền blur mặc định chồng lên panel custom
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static void drawFrame(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
}
