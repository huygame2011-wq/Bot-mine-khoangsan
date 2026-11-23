package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;
import net.minecraft.client.MinecraftClient;

public final class HudVisibilityManager {
    private HudVisibilityManager() {}

    public static boolean isVisible() {
        return ConfigManager.isHudVisible();
    }

    public static void setVisible(boolean visible) {
        ConfigManager.setHudVisible(visible);
        LogManager.log(
                LogManager.Level.THONG_TIN,
                "HUD",
                visible ? "Đã hiện menu hiển thị" : "Đã ẩn menu hiển thị"
        );
        NotificationManager.push(
                "hud_toggle",
                "HUD",
                visible ? "Menu hiển thị: Đang hiện" : "Menu hiển thị: Đang ẩn",
                visible ? 0xFF69F0AE : 0xFFFFD54F,
                ConfigManager.get().notifHudEnabled
        );
    }

    public static boolean toggle() {
        boolean next = !isVisible();
        setVisible(next);
        return next;
    }

    public static void tick(MinecraftClient mc) {
        if (mc == null || mc.player == null) return;
    }
}
