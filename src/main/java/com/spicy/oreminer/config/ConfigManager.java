package com.spicy.oreminer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ConfigManager instance;

    public String activeProfile = "Mặc định";
    public List<String> profiles = new ArrayList<>(Arrays.asList("Mặc định", "Thử nghiệm", "An toàn cao"));

    // Trạng thái module
    public boolean systemEnabled = false;
    public boolean oreSimEnabled = true;
    public boolean targetEnabled = true;
    public boolean survivalEnabled = true;
    public boolean inventoryEnabled = true;
    public boolean hudEnabled = true;
    public boolean notificationsEnabled = true;
    public boolean loggingEnabled = true;

    // Sinh tồn
    public boolean autoEatMonitor = true;
    public boolean lavaDetectEnabled = true;
    public boolean mobDetectEnabled = true;
    public boolean autoFillHoleTest = false;
    public int lowBreadThreshold = 4;
    public int lowDurabilityThreshold = 35;

    // Mục tiêu
    public String selectedOre = "Kim cương";
    public String priorityMode = "Gần nhất";
    public boolean timeoutEnabled = true;
    public int targetTimeoutSeconds = 60;

    // Thông báo
    public boolean notifHudEnabled = true;
    public boolean notifSoundEnabled = true;
    public int notifDurationSeconds = 5;
    public boolean notifTargetChange = true;
    public boolean notifTargetTimeout = true;
    public boolean notifLava = true;
    public boolean notifMob = true;
    public boolean notifInventoryFull = true;
    public boolean notifLowDurability = true;
    public boolean notifLowBread = true;
    public boolean notifModuleError = true;
    public boolean notifSystemState = true;

    // Giao diện & HUD
    public int hudX = 12;
    public int hudY = 12;
    public float hudScale = 1.0f;
    public float hudAlpha = 0.85f;
    public float guiAlpha = 0.92f;
    public float guiScale = 1.0f;
    public boolean compactHudLayout = false;

    public static ConfigManager get() {
        if (instance == null) {
            instance = load("Mặc định");
        }
        return instance;
    }

    private static Path getProfilePath(String profileName) {
        String safeName = profileName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        if (safeName.isEmpty()) safeName = "default";
        return FabricLoader.getInstance().getConfigDir()
                .resolve("spicy_ore_miner")
                .resolve("profile_" + safeName + ".json");
    }

    public static ConfigManager load(String profileName) {
        Path path = getProfilePath(profileName);
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                ConfigManager loaded = GSON.fromJson(json, ConfigManager.class);
                if (loaded != null) {
                    loaded.activeProfile = profileName;
                    instance = loaded;
                    return loaded;
                }
            } catch (Exception ignored) {
            }
        }
        ConfigManager fresh = new ConfigManager();
        fresh.activeProfile = profileName;
        instance = fresh;
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Path path = getProfilePath(activeProfile);
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public void resetDefaults() {
        String current = this.activeProfile;
        ConfigManager fresh = new ConfigManager();
        fresh.activeProfile = current;
        instance = fresh;
        fresh.save();
    }
}
