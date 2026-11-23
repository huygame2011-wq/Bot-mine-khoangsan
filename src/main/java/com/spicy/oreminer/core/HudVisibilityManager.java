package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;
import net.minecraft.client.MinecraftClient;

public final class HudVisibilityManager {
    private static boolean visible = true;
    private static boolean initialized = false;

    private HudVisibilityManager() {}

    public static void initFromConfig() {
        ConfigManager cfg = ConfigManager.get();
        visible = cfg.notifHudEnabled || true;
        initialized = true;
    }

    public static boolean isVisible() {
        if (!initialized) {
            initFromConfig();
        }
        return visible;
    }

    public static void setVisible(boolean value) {
        if (!initialized) {
            initFromConfig();
        }
        visible = value;
    }

    public static void toggle() {
        setVisible(!isVisible());
    }

    public static void tick(MinecraftClient mc) {
        if (!initialized) {
            initFromConfig();
        }
        if (mc == null || mc.player == null) {
            return;
        }
    }
}
