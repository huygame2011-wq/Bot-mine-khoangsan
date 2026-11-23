package com.spicy.oreminer.modules;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.LogManager;
import com.spicy.oreminer.core.NotificationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public final class TargetManager {
    private static String activeTargetOre = "Chưa có mục tiêu";
    private static BlockPos targetPos = null;
    private static long targetAssignedAtMs = 0L;
    private static String targetState = "Chờ mục tiêu";

    private TargetManager() {}

    public static void submitCandidate(String oreName, BlockPos pos) {
        if (!ConfigManager.get().targetEnabled || pos == null) return;
        activeTargetOre = oreName;
        targetPos = pos.toImmutable();
        targetAssignedAtMs = System.currentTimeMillis();
        targetState = "Đang theo dõi";
        LogManager.log(LogManager.Level.THONG_TIN, "Mục tiêu",
                String.format("Đặt mục tiêu %s tại (%d, %d, %d).", oreName, pos.getX(), pos.getY(), pos.getZ()));
    }

    public static void clearTarget(String reason) {
        if (targetPos != null) {
            LogManager.log(LogManager.Level.THONG_TIN, "Mục tiêu", "Xóa mục tiêu: " + reason);
        }
        targetPos = null;
        activeTargetOre = "Chưa có mục tiêu";
        targetState = reason;
    }

    public static void tick(MinecraftClient mc) {
        ConfigManager cfg = ConfigManager.get();
        if (!cfg.targetEnabled) {
            targetState = "Module đang tắt";
            return;
        }
        if (targetPos == null || mc.player == null) {
            targetState = "Chờ mục tiêu";
            return;
        }

        if (cfg.timeoutEnabled) {
            long elapsedSec = (System.currentTimeMillis() - targetAssignedAtMs) / 1000L;
            if (elapsedSec >= cfg.targetTimeoutSeconds) {
                String expiredInfo = activeTargetOre + " (" + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ() + ")";
                clearTarget("Hết thời gian (" + cfg.targetTimeoutSeconds + "s)");
                LogManager.log(LogManager.Level.CANH_BAO, "Mục tiêu", "Mục tiêu hết thời gian: " + expiredInfo);
                NotificationManager.push("target_timeout", "Mục tiêu hết thời gian",
                        "Đã bỏ qua " + expiredInfo, 0xFFFFD54F, cfg.notifTargetTimeout);
                return;
            }
        }
        targetState = "Đang theo dõi";
    }

    public static String getActiveTargetOre() {
        return activeTargetOre;
    }

    public static BlockPos getTargetPos() {
        return targetPos;
    }

    public static String getTargetState() {
        return targetState;
    }

    public static String getDistanceText(MinecraftClient mc) {
        if (targetPos == null || mc == null || mc.player == null) return "--";
        double dist = Math.sqrt(mc.player.squaredDistanceTo(
                targetPos.getX() + 0.5,
                targetPos.getY() + 0.5,
                targetPos.getZ() + 0.5
        ));
        return String.format("%.1fm", dist);
    }
}
