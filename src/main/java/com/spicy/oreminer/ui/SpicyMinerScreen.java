package com.spicy.oreminer.gui;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.HudVisibilityManager;
import com.spicy.oreminer.core.SafetyManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SpicyMinerScreen extends Screen {
    private final Screen parent;
    private ButtonWidget toggleSystemButton;
    private ButtonWidget toggleHudButton;

    public SpicyMinerScreen(Screen parent) {
        super(Text.literal("SPICY ORE MINER v1"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ConfigManager cfg = ConfigManager.get();
        int centerX = this.width / 2;
        int startY = this.height / 2 - 48;
        int btnWidth = 220;
        int btnHeight = 22;

        this.toggleSystemButton = ButtonWidget.builder(getSystemButtonText(cfg.systemEnabled), button -> {
            cfg.systemEnabled = !cfg.systemEnabled;
            if (cfg.systemEnabled) {
                SafetyManager.resetEmergencyStop();
            }
            cfg.save();
            button.setMessage(getSystemButtonText(cfg.systemEnabled));
        }).dimensions(centerX - btnWidth / 2, startY, btnWidth, btnHeight).build();

        this.toggleHudButton = ButtonWidget.builder(getHudButtonText(HudVisibilityManager.isVisible()), button -> {
            boolean visible = HudVisibilityManager.toggle();
            button.setMessage(getHudButtonText(visible));
        }).dimensions(centerX - btnWidth / 2, startY + 28, btnWidth, btnHeight).build();

        ButtonWidget doneButton = ButtonWidget.builder(Text.literal("Đóng"), button -> close())
                .dimensions(centerX - btnWidth / 2, startY + 64, btnWidth, btnHeight)
                .build();

        this.addDrawableChild(this.toggleSystemButton);
        this.addDrawableChild(this.toggleHudButton);
        this.addDrawableChild(doneButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        ConfigManager cfg = ConfigManager.get();
        int centerX = this.width / 2;
        int topY = this.height / 2 - 74;

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, topY, 0xFFB388FF);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Seed: " + cfg.worldSeed + " | Bán kính: " + cfg.scanRadiusXz + "m"),
                centerX,
                topY + 13,
                0xFFCCCCCC
        );
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

    private static Text getSystemButtonText(boolean enabled) {
        return Text.literal(enabled ? "Hệ thống: Đang bật" : "Hệ thống: Đang tắt");
    }

    private static Text getHudButtonText(boolean visible) {
        return Text.literal(visible ? "Menu hiển thị (HUD): Đang hiện" : "Menu hiển thị (HUD): Đang ẩn");
    }
}
