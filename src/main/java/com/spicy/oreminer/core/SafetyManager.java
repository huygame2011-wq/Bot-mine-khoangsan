package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.modules.AutoMinerController;
import net.minecraft.client.MinecraftClient;

public final class SafetyManager {
    private static boolean emergencyStopped = false;
    private static String stopReason = "";

    private SafetyManager() {}

    public static boolean isEmergencyStopped() {
        return emergencyStopped;
    }

    public static String getStopReason() {
        return stopReason;
    }

    public static void triggerEmergencyStop(MinecraftClient mc, String reason) {
        emergencyStopped = true;
        stopReason = (reason == null || reason.isEmpty()) ? "Dừng khẩn cấp" : reason;

        ConfigManager cfg = ConfigManager.get();
        cfg.systemEnabled = false;
        cfg.save();

        AutoMinerController.stopAndRelease(mc);
    }

    public static void clearEmergencyStop() {
        emergencyStopped = false;
        stopReason = "";
    }
}
