package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;

public final class SafetyManager {
    private static boolean paused = false;
    private static boolean emergencyStopped = false;
    private static String pauseReason = "Bình thường";
    private static String currentWarning = "Không có cảnh báo";

    private SafetyManager() {}

    public static boolean isPaused() {
        return paused;
    }

    public static boolean isEmergencyStopped() {
        return emergencyStopped;
    }

    public static String getPauseReason() {
        return pauseReason;
    }

    public static String getCurrentWarning() {
        return currentWarning;
    }

    public static void setWarning(String warning) {
        currentWarning = (warning == null || warning.isBlank()) ? "Không có cảnh báo" : warning;
    }

    public static void pause(String reason) {
        if (!paused) {
            paused = true;
            pauseReason = reason;
            setWarning(reason);
            LogManager.log(LogManager.Level.CANH_BAO, "An toàn", "Hệ thống tạm dừng: " + reason);
            NotificationManager.push("sys_pause", "Hệ thống tạm dừng", reason, 0xFFFFD54F, ConfigManager.get().notifSystemState);
        }
    }

    public static void resume() {
        if (emergencyStopped) {
            LogManager.log(LogManager.Level.AN_TOAN, "An toàn", "Không thể tiếp tục tự động khi đang Dừng Khẩn Cấp.");
            return;
        }
        if (paused) {
            paused = false;
            pauseReason = "Bình thường";
            setWarning("Không có cảnh báo");
            LogManager.log(LogManager.Level.THONG_TIN, "An toàn", "Hệ thống hoạt động trở lại.");
            NotificationManager.push("sys_resume", "Hệ thống hoạt động lại", "Đã khôi phục luồng giám sát.", 0xFF69F0AE, ConfigManager.get().notifSystemState);
        }
    }

    public static void triggerEmergencyStop() {
        ConfigManager cfg = ConfigManager.get();
        cfg.systemEnabled = false;
        cfg.save();
        emergencyStopped = true;
        paused = true;
        pauseReason = "DỪNG KHẨN CẤP";
        setWarning("DỪNG KHẨN CẤP - Đã khóa mọi module");
        LogManager.log(LogManager.Level.AN_TOAN, "Dừng khẩn cấp", "Người dùng kích hoạt dừng khẩn cấp. Toàn bộ module đã ngắt.");
        NotificationManager.push("emergency_stop", "DỪNG KHẨN CẤP", "Đã dừng mọi module và khóa an toàn.", 0xFFFF5252, true);
    }

    public static void resetEmergencyAndEnable() {
        emergencyStopped = false;
        paused = false;
        pauseReason = "Bình thường";
        setWarning("Không có cảnh báo");
        ConfigManager cfg = ConfigManager.get();
        cfg.systemEnabled = true;
        cfg.save();
        LogManager.log(LogManager.Level.THONG_TIN, "Hệ thống", "Người dùng mở khóa an toàn và bật lại hệ thống.");
        NotificationManager.push("sys_start", "Bật hệ thống", "Spicy Ore Miner đã sẵn sàng.", 0xFF69F0AE, cfg.notifSystemState);
    }
}
