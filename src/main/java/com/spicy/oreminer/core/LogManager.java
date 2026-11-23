package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LogManager {
    public enum Level {
        THONG_TIN("THÔNG TIN", 0xFF90CAF9),
        CANH_BAO("CẢNH BÁO", 0xFFFFD54F),
        LOI("LỖI", 0xFFEF5350),
        AN_TOAN("AN TOÀN", 0xFFCE93D8);

        public final String label;
        public final int color;

        Level(String label, int color) {
            this.label = label;
            this.color = color;
        }
    }

    public record Entry(String time, Level level, String module, String message) {}

    private static final int MAX_ENTRIES = 250;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private LogManager() {}

    public static synchronized void log(Level level, String module, String message) {
        if (!ConfigManager.get().loggingEnabled) return;
        String now = LocalTime.now().format(TIME_FMT);
        if (!ENTRIES.isEmpty()) {
            Entry last = ENTRIES.get(0);
            if (last.module.equals(module) && last.message.equals(message) && last.time.equals(now)) {
                return;
            }
        }
        ENTRIES.add(0, new Entry(now, level, module, message));
        if (ENTRIES.size() > MAX_ENTRIES) {
            ENTRIES.remove(ENTRIES.size() - 1);
        }
    }

    public static synchronized List<Entry> getEntries(Level filter) {
        if (filter == null) {
            return Collections.unmodifiableList(new ArrayList<>(ENTRIES));
        }
        List<Entry> out = new ArrayList<>();
        for (Entry e : ENTRIES) {
            if (e.level == filter) out.add(e);
        }
        return out;
    }

    public static synchronized void clear() {
        ENTRIES.clear();
    }
}
