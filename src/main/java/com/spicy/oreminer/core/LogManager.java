package com.spicy.oreminer.core;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LogManager {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final int MAX_ENTRIES = 300;

    private LogManager() {}

    public enum Level {
        THONG_TIN("Thông tin", 0xFFE9D5FF),
        CANH_BAO("Cảnh báo", 0xFFFFD54F),
        LOI("Lỗi", 0xFFFF5252),
        AN_TOAN("An toàn", 0xFF69F0AE);

        public final String label;
        private final int argb;

        Level(String label, int argb) {
            this.label = label;
            this.argb = argb;
        }

        public int color() {
            return argb;
        }
    }

    public record Entry(String time, Level level, String module, String message) {}

    public static synchronized void log(Level level, String module, String message) {
        String now = LocalTime.now().format(TIME_FMT);
        ENTRIES.add(new Entry(now, level, module, message));
        if (ENTRIES.size() > MAX_ENTRIES) {
            ENTRIES.remove(0);
        }
    }

    public static synchronized void clear() {
        ENTRIES.clear();
    }

    public static synchronized List<Entry> getEntries(Level filter) {
        if (filter == null) {
            return Collections.unmodifiableList(new ArrayList<>(ENTRIES));
        }
        List<Entry> out = new ArrayList<>();
        for (Entry e : ENTRIES) {
            if (e.level == filter) {
                out.add(e);
            }
        }
        return Collections.unmodifiableList(out);
    }
}
