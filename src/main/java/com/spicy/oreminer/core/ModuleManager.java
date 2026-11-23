package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.modules.AutoMinerController;
import net.minecraft.client.MinecraftClient;

public final class ModuleManager {
    private static boolean wasSystemEnabled = false;

    private ModuleManager() {}

    public static void onClientTick(MinecraftClient mc) {
        if (mc == null || mc.player == null || mc.world == null) {
            wasSystemEnabled = false;
            return;
        }

        ConfigManager cfg = ConfigManager.get();
        boolean active = cfg.systemEnabled && !SafetyManager.isEmergencyStopped();

        if (!active) {
            if (wasSystemEnabled) {
                AutoMinerController.stopAndRelease(mc);
                wasSystemEnabled = false;
            }
            return;
        }

        wasSystemEnabled = true;
        AutoMinerController.tick(mc);
    }

    public static void setSystemEnabled(MinecraftClient mc, boolean enabled) {
        ConfigManager cfg = ConfigManager.get();
        cfg.systemEnabled = enabled;
        cfg.save();

        if (!enabled) {
            AutoMinerController.stopAndRelease(mc);
            wasSystemEnabled = false;
        }
    }

    public static boolean isSystemActive() {
        ConfigManager cfg = ConfigManager.get();
        return cfg.systemEnabled && !SafetyManager.isEmergencyStopped();
    }
}
