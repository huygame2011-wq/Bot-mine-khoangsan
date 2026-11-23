package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.modules.AutoMinerController;
import net.minecraft.client.MinecraftClient;

public final class ModuleManager {
    private ModuleManager() {}

    public static void setSystemEnabled(MinecraftClient mc, boolean enabled) {
        ConfigManager cfg = ConfigManager.get();
        cfg.systemEnabled = enabled;
        cfg.save();

        if (!enabled) {
            AutoMinerController.stopAndRelease(mc);
            LogManager.log(LogManager.Level.THONG_TIN, "MODULE", "Hệ thống đã tắt và nhả điều khiển.");
        } else {
            SafetyManager.clearEmergencyStop();
            LogManager.log(LogManager.Level.THONG_TIN, "MODULE", "Hệ thống đã bật.");
        }
    }

    public static void onClientTick(MinecraftClient mc) {
        if (mc == null) {
            return;
        }

        ConfigManager cfg = ConfigManager.get();
        if (SafetyManager.isEmergencyStopped() || !cfg.systemEnabled || mc.player == null || mc.world == null) {
            AutoMinerController.stopAndRelease(mc);
            return;
        }

        AutoMinerController.tick(mc);
    }
}
