package com.spicy.oreminer.core;

import com.spicy.oreminer.modules.AutoMinerController;
import net.minecraft.client.MinecraftClient;

public final class SafetyManager {
    private static boolean emergencyStopped = false;
    private static boolean paused = false;
    private static String stopReason = "";
    private static String currentWarning = "Không có cảnh báo";

    private SafetyManager() {}

    public static void triggerEmergencyStop(MinecraftClient mc, String reason) {
        emergencyStopped = true;
        paused = true;
        stopReason = (reason == null || reason.isBlank()) ? "Dừng khẩn cấp" : reason;
        currentWarning = stopReason;
        AutoMinerController.stopAndRelease(mc);
        LogManager.log(LogManager.Level.CANH_BAO, "SAFETY", "Dừng khẩn cấp: " + stopReason);
    }

    public static void pause(String reason) {
        paused = true;
        String msg = (reason == null || reason.isBlank()) ? "Tạm dừng an toàn" : reason;
        currentWarning = msg;
        if (stopReason.isBlank()) {
            stopReason = msg;
        }
        AutoMinerController.stopAndRelease(MinecraftClient.getInstance());
    }

    public static void setWarning(String warning) {
        if (warning == null || warning.isBlank()) {
            currentWarning = "Không có cảnh báo";
        } else {
            currentWarning = warning;
        }
    }

    public static String getWarning() {
        return currentWarning;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static void clearEmergencyStop() {
        emergencyStopped = false;
        paused = false;
        stopReason = "";
        currentWarning = "Không có cảnh báo";
    }

    public static boolean isEmergencyStopped() {
        return emergencyStopped;
    }

    public static String getStopReason() {
        return stopReason;
    }
}
