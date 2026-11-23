package com.spicy.oreminer.core;

import com.spicy.oreminer.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class LogManager {
    public enum Level {
        THONG_TIN,
        CANH_BAO,
        LOI
    }

    public record LogEntry(long timestampMs, Level level, String tag, String message) {}

    private static final Logger LOGGER = LoggerFactory.getLogger("SpicyOreMiner");
    private static final int MAX_ENTRIES = 100;
    private static final Deque<LogEntry> ENTRIES = new ArrayDeque<>();

    private LogManager() {}

    public static synchronized void log(Level level, String tag, String message) {
        Level safeLevel = level == null ? Level.THONG_TIN : level;
        String safeTag = (tag == null || tag.isBlank()) ? "CORE" : tag;
        String safeMsg = message == null ? "" : message;

        ENTRIES.addLast(new LogEntry(System.currentTimeMillis(), safeLevel, safeTag, safeMsg));
        while (ENTRIES.size() > MAX_ENTRIES) {
            ENTRIES.removeFirst();
        }

        ConfigManager cfg = ConfigManager.get();
        if (cfg != null && !cfg.consoleLogEnabled) {
            return;
        }

        String formatted = "[" + safeTag + "] " + safeMsg;
        switch (safeLevel) {
            case LOI -> LOGGER.error(formatted);
            case CANH_BAO -> LOGGER.warn(formatted);
            case THONG_TIN -> LOGGER.info(formatted);
        }
    }

    public static synchronized List<LogEntry> getRecentLogs() {
        return Collections.unmodifiableList(new ArrayList<>(ENTRIES));
    }

    public static synchronized void clear() {
        ENTRIES.clear();
    }
}
