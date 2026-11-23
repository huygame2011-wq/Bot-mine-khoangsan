package com.spicy.oreminer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "spicy-ore-miner.json";

    private static ConfigManager instance;

    public boolean systemEnabled = false;
    public boolean hudVisible = true;
    public boolean hudEnabled = true;
    public String activeProfile = "Mặc định";
    public boolean compactHudLayout = false;
    public float hudX = 10.0f;
    public float hudY = 10.0f;
    public float hudScale = 1.0f;
    public float hudAlpha = 0.85f;

    public long worldSeed = 6608149111735331168L;
    public int scanRadiusXz = 10;
    public int scanRadiusY = 6;
    public double reachDistance = 4.2;
    public boolean autoSwitchPickaxe = true;
    public boolean breakObstacles = true;

    public boolean oreSimEnabled = true;
    public boolean survivalEnabled = true;
    public boolean lavaDetectEnabled = true;
    public boolean mobDetectEnabled = true;
    public boolean inventoryEnabled = true;
    public boolean targetEnabled = true;

    public boolean timeoutEnabled = true;
    public int targetTimeoutSeconds = 30;
    public int lowDurabilityThreshold = 20;
    public int lowBreadThreshold = 5;

    public boolean notificationsEnabled = true;
    public int notifDurationSeconds = 4;
    public boolean notifSoundEnabled = true;
    public boolean notifHudEnabled = true;
    public boolean notifSystemState = true;
    public boolean notifTargetChange = true;
    public boolean notifTargetTimeout = true;
    public boolean notifLava = true;
    public boolean notifMob = true;
    public boolean notifInventoryFull = true;
    public boolean notifLowDurability = true;
    public boolean notifLowBread = true;

    private ConfigManager() {}

    public static ConfigManager get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static ConfigManager load() {
        Path path = getConfigPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                ConfigManager loaded = GSON.fromJson(reader, ConfigManager.class);
                if (loaded != null) {
                    if (loaded.activeProfile == null || loaded.activeProfile.isEmpty()) {
                        loaded.activeProfile = "Mặc định";
                    }
                    if (loaded.hudScale <= 0f) {
                        loaded.hudScale = 1.0f;
                    }
                    if (loaded.hudAlpha <= 0f) {
                        loaded.hudAlpha = 0.85f;
                    }
                    if (loaded.notifDurationSeconds <= 0) {
                        loaded.notifDurationSeconds = 4;
                    }
                    instance = loaded;
                    return instance;
                }
            } catch (Exception ignored) {
            }
        }
        instance = new ConfigManager();
        instance.save();
        return instance;
    }

    public void save() {
        try {
            Path path = getConfigPath();
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static boolean isHudVisible() {
        ConfigManager cfg = get();
        return cfg.hudVisible && cfg.hudEnabled;
    }

    public static void setHudVisible(boolean visible) {
        ConfigManager cfg = get();
        cfg.hudVisible = visible;
        cfg.hudEnabled = visible;
        cfg.save();
    }

    public static boolean toggleHudVisible() {
        ConfigManager cfg = get();
        boolean next = !isHudVisible();
        cfg.hudVisible = next;
        cfg.hudEnabled = next;
        cfg.save();
        return next;
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }
}
