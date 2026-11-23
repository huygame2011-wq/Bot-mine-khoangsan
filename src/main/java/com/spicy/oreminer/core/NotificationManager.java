package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class NotificationManager {
    public static final class Entry {
        public final String id;
        public final String title;
        public final String message;
        public final int accentColor;
        public int remainingTicks;
        public final int maxTicks;

        public Entry(String id, String title, String message, int accentColor, int durationTicks) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.accentColor = accentColor;
            this.maxTicks = Math.max(20, durationTicks);
            this.remainingTicks = this.maxTicks;
        }
    }

    private static final List<Entry> ACTIVE = new ArrayList<>();

    private NotificationManager() {}

    public static void push(String id, String title, String message, int accentColor) {
        push(id, title, message, accentColor, true);
    }

    public static void push(String id, String title, String message, int accentColor, boolean enabledFlag) {
        ConfigManager cfg = ConfigManager.get();
        if (!cfg.notificationsEnabled || !enabledFlag) {
            return;
        }

        int durationTicks = Math.max(1, cfg.notifDurationSeconds) * 20;

        if (id != null && !id.isBlank()) {
            for (Entry existing : ACTIVE) {
                if (id.equals(existing.id)) {
                    existing.remainingTicks = durationTicks;
                    return;
                }
            }
        }

        ACTIVE.add(0, new Entry(id, title, message, accentColor, durationTicks));
        while (ACTIVE.size() > 5) {
            ACTIVE.remove(ACTIVE.size() - 1);
        }

        if (cfg.consoleLogEnabled) {
            LogManager.log(LogManager.Level.THONG_TIN, "NOTIF", title + ": " + message);
        }

        if (cfg.notifSoundEnabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) {
                mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
        }
    }

    public static void tick() {
        if (ACTIVE.isEmpty()) {
            return;
        }
        Iterator<Entry> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            e.remainingTicks--;
            if (e.remainingTicks <= 0) {
                it.remove();
            }
        }
    }

    public static List<Entry> getActive() {
        return ACTIVE;
    }

    public static void clear() {
        ACTIVE.clear();
    }
}
