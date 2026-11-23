package com.spicy.oreminer.modules;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.LogManager;
import com.spicy.oreminer.core.NotificationManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class OreSimIntegration {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static boolean merteoDetected = false;
    private static boolean checkedOnce = false;
    private static String dataStatus = "Chưa kiểm tra";
    private static String detectedOre = "Chưa có";
    private static BlockPos testCoord = null;
    private static String lastUpdated = "--:--:--";
    private static String lastError = "Không";

    private OreSimIntegration() {}

    public static void checkCompatibility() {
        boolean modPresent = FabricLoader.getInstance().isModLoaded("merteo")
                || FabricLoader.getInstance().isModLoaded("meteor-client");
        boolean classPresent = false;
        try {
            Class.forName("meteordevelopment.meteorclient.systems.modules.Modules", false, OreSimIntegration.class.getClassLoader());
            classPresent = true;
        } catch (Throwable ignored) {
        }
        merteoDetected = modPresent || classPresent;
        if (!checkedOnce) {
            checkedOnce = true;
            if (merteoDetected) {
                dataStatus = "Phát hiện client tích hợp (Chờ dữ liệu API)";
                LogManager.log(LogManager.Level.THONG_TIN, "Ore Sim", "Phát hiện môi trường client bên ngoài; chưa có API Ore Sim công khai.");
            } else {
                dataStatus = "Không có Ore Sim ngoài (Dùng dữ liệu thử nghiệm)";
                LogManager.log(LogManager.Level.THONG_TIN, "Ore Sim", "Không tìm thấy Ore Sim ngoài. Chuyển sang chế độ quản lý dữ liệu thử nghiệm an toàn.");
            }
        }
    }

    public static void tick(MinecraftClient mc) {
        if (!ConfigManager.get().oreSimEnabled) {
            dataStatus = "Module đang tắt";
            return;
        }
        if (!checkedOnce) {
            checkCompatibility();
        }
    }

    public static void injectTestOreData(String oreName, int x, int y, int z) {
        if (oreName == null || oreName.isBlank()) {
            lastError = "Tên quặng không hợp lệ";
            dataStatus = "Dữ liệu lỗi";
            LogManager.log(LogManager.Level.LOI, "Ore Sim", "Từ chối dữ liệu thử nghiệm không hợp lệ.");
            return;
        }
        if (y < -64 || y > 320) {
            lastError = "Tọa độ Y ngoài phạm vi (-64..320)";
            dataStatus = "Dữ liệu tọa độ lỗi";
            LogManager.log(LogManager.Level.LOI, "Ore Sim", lastError);
            return;
        }
        detectedOre = oreName;
        testCoord = new BlockPos(x, y, z);
        lastUpdated = LocalTime.now().format(TIME_FMT);
        dataStatus = "Dữ liệu thử nghiệm hợp lệ";
        lastError = "Không";

        LogManager.log(LogManager.Level.THONG_TIN, "Ore Sim",
                String.format("Cập nhật dữ liệu thử nghiệm: %s tại (%d, %d, %d)", oreName, x, y, z));
        NotificationManager.push("oresim_update", "Dữ liệu Ore Sim thay đổi",
                oreName + " (" + x + ", " + y + ", " + z + ")", 0xFFB388FF, ConfigManager.get().notifTargetChange);
        TargetManager.submitCandidate(oreName, testCoord);
    }

    public static boolean isMerteoDetected() { return merteoDetected; }
    public static String getDataStatus() { return dataStatus; }
    public static String getDetectedOre() { return detectedOre; }
    public static BlockPos getTestCoord() { return testCoord; }
    public static String getLastUpdated() { return lastUpdated; }
    public static String getLastError() { return lastError; }
}
