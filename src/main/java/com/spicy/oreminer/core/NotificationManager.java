package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class NotificationManager {
    public static final class Entry {
        public final String key;
        public final String title;
        public final String message;
        public final int color;
        public final long expireAtMs;

        public Entry(String key, String title, String message, int color, long expireAtMs) {
            this.key = key;
            this.title = title;
            this.message = message;
            this.color = color;
            this.expireAtMs = expireAtMs;
        }
    }

    private static final List<Entry> ACTIVE = new ArrayList<>();

    private NotificationManager() {}

    public static void push(String key, String title, String message, int color, boolean typeEnabled) {
        ConfigManager cfg = ConfigManager.get();
        if (!cfg.notificationsEnabled || !typeEnabled) {
            return;
        }

        long now = System.currentTimeMillis();
        int durationSec = Math.max(2, cfg.notifDurationSeconds);
        long expireAt = now + durationSec * 1000L;

        ACTIVE.removeIf(e -> e.key != null && e.key.equals(key));
        ACTIVE.add(0, new Entry(key, title, message, color, expireAt));
        if (ACTIVE.size() > 5) {
            ACTIVE.subList(5, ACTIVE.size()).clear();
        }

        if (cfg.notifSoundEnabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) {
                mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.35f, 1.15f);
            }
        }
    }

    public static List<Entry> getActiveEntries() {
        long now = System.currentTimeMillis();
        Iterator<Entry> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            if (e.expireAtMs <= now) {
                it.remove();
            }
        }
        return Collections.unmodifiableList(ACTIVE);
    }

    public static boolean isHudNotificationEnabled() {
        ConfigManager cfg = ConfigManager.get();
        return cfg.notificationsEnabled && cfg.notifHudEnabled;
    }

    public static void clear() {
        ACTIVE.clear();
    }
}
