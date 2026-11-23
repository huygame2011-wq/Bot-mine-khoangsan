package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NotificationManager {
    public record Toast(String title, String detail, int color, long expireAtMs, String timestamp) {}

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final List<Toast> HISTORY = new ArrayList<>();
    private static final List<Toast> ACTIVE_TOASTS = new ArrayList<>();
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private NotificationManager() {}

    public static synchronized void push(String key, String title, String detail, int color, boolean typeEnabled) {
        ConfigManager cfg = ConfigManager.get();
        if (!cfg.notificationsEnabled || !typeEnabled) return;

        long now = System.currentTimeMillis();
        Long last = COOLDOWNS.get(key);
        if (last != null && (now - last) < 4000L) {
            return;
        }
        COOLDOWNS.put(key, now);

        String time = LocalTime.now().format(TIME_FMT);
        long expire = now + Math.max(2, cfg.notifDurationSeconds) * 1000L;
        Toast toast = new Toast(title, detail, color, expire, time);

        HISTORY.add(0, toast);
        if (HISTORY.size() > 100) HISTORY.remove(HISTORY.size() - 1);

        if (cfg.notifHudEnabled) {
            ACTIVE_TOASTS.add(0, toast);
            if (ACTIVE_TOASTS.size() > 4) ACTIVE_TOASTS.remove(ACTIVE_TOASTS.size() - 1);
        }

        if (cfg.notifSoundEnabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) {
                mc.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.5f, 1.35f);
            }
        }
    }

    public static synchronized List<Toast> getActiveToasts() {
        long now = System.currentTimeMillis();
        ACTIVE_TOASTS.removeIf(t -> t.expireAtMs <= now);
        return new ArrayList<>(ACTIVE_TOASTS);
    }

    public static synchronized List<Toast> getHistory() {
        return new ArrayList<>(HISTORY);
    }

    public static synchronized void clearHistory() {
        HISTORY.clear();
        ACTIVE_TOASTS.clear();
    }
}
