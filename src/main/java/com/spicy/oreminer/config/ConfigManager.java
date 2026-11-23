package com.spicy.oreminer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("spicy-ore-miner.json");

    private static ConfigManager instance;

    // Trạng thái hệ thống & hồ sơ
    public boolean systemEnabled = false;
    public String activeProfile = "Mặc định";

    // Quét & theo dõi quặng
    public boolean oreSimEnabled = true;
    public int scanRadiusXz = 12;
    public int scanRadiusY = 8;

    // Hiển thị HUD
    public float hudX = 10.0f;
    public float hudY = 10.0f;
    public float hudAlpha = 0.72f;
    public boolean compactHudLayout = false;

    // Thông báo & log
    public boolean notificationsEnabled = true;
    public int notifDurationSeconds = 4;
    public boolean notifHudEnabled = true;
    public boolean notifSoundEnabled = false;
    public boolean consoleLogEnabled = true;

    public static synchronized ConfigManager get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static synchronized void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
                ConfigManager loaded = GSON.fromJson(json, ConfigManager.class);
                if (loaded != null) {
                    instance = loaded;
                    instance.sanitize();
                    return;
                }
            } catch (Exception ignored) {
                // Nếu file cấu hình lỗi định dạng thì tạo lại mặc định bên dưới
            }
        }

        instance = new ConfigManager();
        instance.sanitize();
        instance.save();
    }

    public synchronized void save() {
        sanitize();
        try {
            Path parent = CONFIG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(CONFIG_PATH, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private void sanitize() {
        if (activeProfile == null || activeProfile.isBlank()) {
            activeProfile = "Mặc định";
        }
        scanRadiusXz = Math.max(1, Math.min(24, scanRadiusXz));
        scanRadiusY = Math.max(1, Math.min(16, scanRadiusY));
        hudX = Math.max(0.0f, Math.min(2000.0f, hudX));
        hudY = Math.max(0.0f, Math.min(2000.0f, hudY));
        hudAlpha = Math.max(0.15f, Math.min(1.0f, hudAlpha));
        notifDurationSeconds = Math.max(1, Math.min(30, notifDurationSeconds));
    }
}
