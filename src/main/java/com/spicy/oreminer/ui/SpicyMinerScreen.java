package com.spicy.oreminer.ui;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.LogManager;
import com.spicy.oreminer.core.ModuleManager;
import com.spicy.oreminer.core.NotificationManager;
import com.spicy.oreminer.core.SafetyManager;
import com.spicy.oreminer.modules.InventoryManager;
import com.spicy.oreminer.modules.OreSimIntegration;
import com.spicy.oreminer.modules.SurvivalManager;
import com.spicy.oreminer.modules.TargetManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class SpicyMinerScreen extends Screen {
    private enum Tab {
        TONG_QUAN("Tổng quan"),
        ORE_SIM("Ore Sim"),
        MUC_TIEU("Mục tiêu"),
        SINH_TON("Sinh tồn"),
        KHO_DO("Kho đồ"),
        THONG_BAO("Thông báo"),
        NHAT_KY("Nhật ký"),
        HO_SO("Hồ sơ"),
        GIAO_DIEN("Giao diện");

        public final String label;
        Tab(String label) { this.label = label; }
    }

    private Tab currentTab = Tab.TONG_QUAN;
    private int logScroll = 0;

    public SpicyMinerScreen() {
        super(Text.literal("Spicy Ore Miner - Bảng điều khiển"));
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearChildren();
        int pad = 10;
        int tabY = 28;
        int tabW = Math.max(54, (this.width - pad * 2 - 4 * 8) / 5);
        int tabH = 18;

        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            Tab t = tabs[i];
            int col = i % 5;
            int row = i / 5;
            int x = pad + col * (tabW + 4);
            int y = tabY + row * (tabH + 3);

            this.addDrawableChild(ButtonWidget.builder(Text.literal(t.label), btn -> {
                currentTab = t;
                logScroll = 0;
                rebuildWidgets();
            }).dimensions(x, y, tabW, tabH).build());
        }

        int bodyTop = tabY + 2 * (tabH + 3) + 8;
        int leftX = pad + 6;
        int rowY = bodyTop + 14;
        ConfigManager cfg = ConfigManager.get();

        if (currentTab == Tab.TONG_QUAN) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(cfg.systemEnabled ? "Tắt hệ thống" : "Bật hệ thống"),
                    btn -> {
                        if (cfg.systemEnabled) {
                            cfg.systemEnabled = false;
                            cfg.save();
                            SafetyManager.pause("Người dùng tắt hệ thống");
                            LogManager.log(LogManager.Level.THONG_TIN, "Hệ thống", "Đã tắt hệ thống giám sát.");
                        } else {
                            SafetyManager.resetEmergencyAndEnable();
                        }
                        rebuildWidgets();
                    }).dimensions(leftX, rowY, 110, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(SafetyManager.isPaused() ? "Tiếp tục" : "Tạm dừng"),
                    btn -> {
                        if (SafetyManager.isPaused()) {
                            SafetyManager.resume();
                        } else {
                            SafetyManager.pause("Tạm dừng thủ công từ giao diện");
                        }
                        rebuildWidgets();
                    }).dimensions(leftX + 116, rowY, 95, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("DỪNG KHẨN CẤP"),
                    btn -> {
                        SafetyManager.triggerEmergencyStop();
                        rebuildWidgets();
                    }).dimensions(leftX + 217, rowY, 125, 20).build());

        } else if (currentTab == Tab.ORE_SIM) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Kiểm tra tương thích"),
                    btn -> {
                        OreSimIntegration.checkCompatibility();
                        rebuildWidgets();
                    }).dimensions(leftX, rowY, 130, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Gửi mẫu quặng thử nghiệm"),
                    btn -> {
                        int px = (client != null && client.player != null) ? client.player.getBlockX() + 12 : 0;
                        int py = (client != null && client.player != null) ? Math.max(-59, Math.min(20, client.player.getBlockY() - 2)) : -30;
                        int pz = (client != null && client.player != null) ? client.player.getBlockZ() + 8 : 0;
                        OreSimIntegration.injectTestOreData(cfg.selectedOre, px, py, pz);
                        rebuildWidgets();
                    }).dimensions(leftX + 136, rowY, 155, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Mô phỏng dữ liệu lỗi"),
                    btn -> {
                        OreSimIntegration.injectTestOreData("Quặng lỗi", 0, 500, 0);
                        rebuildWidgets();
                    }).dimensions(leftX, rowY + 26, 130, 20).build());

        } else if (currentTab == Tab.MUC_TIEU) {
            String[] ores = {"Kim cương", "Sắt", "Vàng", "Ngọc lục bảo", "Cổ vật cổ đại"};
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Quặng: " + cfg.selectedOre),
                    btn -> {
                        int idx = 0;
                        for (int i = 0; i < ores.length; i++) {
                            if (ores[i].equals(cfg.selectedOre)) {
                                idx = (i + 1) % ores.length;
                                break;
                            }
                        }
                        cfg.selectedOre = ores[idx];
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX, rowY, 145, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Hết hạn: " + (cfg.timeoutEnabled ? cfg.targetTimeoutSeconds + "s" : "Tắt")),
                    btn -> {
                        cfg.timeoutEnabled = !cfg.timeoutEnabled;
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX + 151, rowY, 120, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Xóa mục tiêu"),
                    btn -> {
                        TargetManager.clearTarget("Người dùng xóa thủ công");
                        rebuildWidgets();
                    }).dimensions(leftX + 277, rowY, 95, 20).build());

        } else if (currentTab == Tab.SINH_TON) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Cảnh báo dung nham: " + (cfg.lavaDetectEnabled ? "Bật" : "Tắt")),
                    btn -> {
                        cfg.lavaDetectEnabled = !cfg.lavaDetectEnabled;
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX, rowY, 145, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Cảnh báo quái vật: " + (cfg.mobDetectEnabled ? "Bật" : "Tắt")),
                    btn -> {
                        cfg.mobDetectEnabled = !cfg.mobDetectEnabled;
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX + 151, rowY, 145, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Thử nghiệm lấp hố: " + (cfg.autoFillHoleTest ? "Bật" : "Tắt")),
                    btn -> {
                        cfg.autoFillHoleTest = !cfg.autoFillHoleTest;
                        cfg.save();
                        LogManager.log(LogManager.Level.THONG_TIN, "Sinh tồn", "Trạng thái thử nghiệm lấp hố: " + (cfg.autoFillHoleTest ? "Bật" : "Tắt"));
                        rebuildWidgets();
                    }).dimensions(leftX, rowY + 26, 155, 20).build());

        } else if (currentTab == Tab.KHO_DO) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Ngưỡng bánh mì: " + cfg.lowBreadThreshold),
                    btn -> {
                        cfg.lowBreadThreshold = (cfg.lowBreadThreshold >= 16) ? 2 : cfg.lowBreadThreshold + 2;
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX, rowY, 140, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Ngưỡng độ bền: " + cfg.lowDurabilityThreshold),
                    btn -> {
                        cfg.lowDurabilityThreshold = (cfg.lowDurabilityThreshold >= 100) ? 15 : cfg.lowDurabilityThreshold + 10;
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX + 146, rowY, 145, 20).build());

        } else if (currentTab == Tab.THONG_BAO) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Thông báo HUD: " + (cfg.notifHudEnabled ? "Bật" : "Tắt")),
                    btn -> {
                        cfg.notifHudEnabled = !cfg.notifHudEnabled;
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX, rowY, 130, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Âm thanh: " + (cfg.notifSoundEnabled ? "Bật" : "Tắt")),
                    btn -> {
                        cfg.notifSoundEnabled = !cfg.notifSoundEnabled;
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX + 136, rowY, 115, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Xóa lịch sử"),
                    btn -> {
                        NotificationManager.clearHistory();
                        rebuildWidgets();
                    }).dimensions(leftX + 257, rowY, 95, 20).build());

        } else if (currentTab == Tab.NHAT_KY) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Cuộn lên"),
                    btn -> {
                        logScroll = Math.max(0, logScroll - 4);
                        rebuildWidgets();
                    }).dimensions(leftX, rowY, 80, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Cuộn xuống"),
                    btn -> {
                        logScroll = logScroll + 4;
                        rebuildWidgets();
                    }).dimensions(leftX + 86, rowY, 90, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Xóa nhật ký"),
                    btn -> {
                        LogManager.clear();
                        logScroll = 0;
                        rebuildWidgets();
                    }).dimensions(leftX + 182, rowY, 95, 20).build());

        } else if (currentTab == Tab.HO_SO) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Đổi hồ sơ"),
                    btn -> {
                        int idx = cfg.profiles.indexOf(cfg.activeProfile);
                        String next = cfg.profiles.get((idx + 1) % cfg.profiles.size());
                        ConfigManager.load(next);
                        LogManager.log(LogManager.Level.THONG_TIN, "Hồ sơ", "Chuyển sang hồ sơ: " + next);
                        rebuildWidgets();
                    }).dimensions(leftX, rowY, 105, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Lưu cấu hình"),
                    btn -> {
                        ConfigManager.get().save();
                        LogManager.log(LogManager.Level.THONG_TIN, "Hồ sơ", "Đã lưu hồ sơ: " + ConfigManager.get().activeProfile);
                        rebuildWidgets();
                    }).dimensions(leftX + 111, rowY, 115, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Khôi phục mặc định"),
                    btn -> {
                        ConfigManager.get().resetDefaults();
                        LogManager.log(LogManager.Level.THONG_TIN, "Hồ sơ", "Đã khôi phục mặc định cho hồ sơ hiện tại.");
                        rebuildWidgets();
                    }).dimensions(leftX + 232, rowY, 130, 20).build());

        } else if (currentTab == Tab.GIAO_DIEN) {
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("HUD: " + (cfg.hudEnabled ? "Bật" : "Tắt")),
                    btn -> {
                        cfg.hudEnabled = !cfg.hudEnabled;
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX, rowY, 90, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Gọn HUD: " + (cfg.compactHudLayout ? "Bật" : "Tắt")),
                    btn -> {
                        cfg.compactHudLayout = !cfg.compactHudLayout;
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX + 96, rowY, 110, 20).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Độ mờ: " + Math.round(cfg.hudAlpha * 100) + "%"),
                    btn -> {
                        cfg.hudAlpha = (cfg.hudAlpha <= 0.45f) ? 0.95f : cfg.hudAlpha - 0.15f;
                        cfg.save();
                        rebuildWidgets();
                    }).dimensions(leftX + 212, rowY, 105, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xAA07050E);

        int pad = 10;
        int panelTop = 76;
        int panelBottom = this.height - 10;
        int panelRight = this.width - pad;

        context.fill(pad, panelTop, panelRight, panelBottom, 0xE60D0919);
        context.fill(pad, panelTop, panelRight, panelTop + 1, 0xFF7C3AED);
        context.fill(pad, panelBottom - 1, panelRight, panelBottom, 0xFF7C3AED);
        context.fill(pad, panelTop, pad + 1, panelBottom, 0xFF7C3AED);
        context.fill(panelRight - 1, panelTop, panelRight, panelBottom, 0xFF7C3AED);

        context.drawCenteredTextWithShadow(this.textRenderer,
                "SPICY ORE MINER v1 - BẢNG ĐIỀU KHIỂN (" + currentTab.label.toUpperCase() + ")",
                this.width / 2, 10, 0xFFC084FC);

        super.render(context, mouseX, mouseY, delta);

        int textX = pad + 16;
        int textY = panelTop + 56;
        ConfigManager cfg = ConfigManager.get();

        if (currentTab == Tab.TONG_QUAN) {
            String stateText = SafetyManager.isEmergencyStopped() ? "DỪNG KHẨN CẤP (Đã khóa)"
                    : (SafetyManager.isPaused() ? "Tạm dừng (" + SafetyManager.getPauseReason() + ")"
                    : (cfg.systemEnabled ? "Đang hoạt động" : "Đang tắt"));
            context.drawTextWithShadow(this.textRenderer, "Trạng thái hệ thống: " + stateText, textX, textY, 0xFFF3E8FF);
            context.drawTextWithShadow(this.textRenderer, "Hồ sơ hiện tại: " + cfg.activeProfile, textX, textY + 13, 0xFFC4B5FD);
            context.drawTextWithShadow(this.textRenderer, "Cảnh báo hiện tại: " + SafetyManager.getCurrentWarning(), textX, textY + 26, 0xFFFF8A65);
            context.drawTextWithShadow(this.textRenderer, "Trạng thái sinh tồn: " + SurvivalManager.getStatusText(), textX, textY + 39, 0xFF69F0AE);

            Map<String, String> errs = ModuleManager.getModuleErrors();
            context.drawTextWithShadow(this.textRenderer,
                    "Lỗi module độc lập: " + (errs.isEmpty() ? "Không có" : errs.toString()),
                    textX, textY + 52, errs.isEmpty() ? 0xFFA78BFA : 0xFFFF5252);

        } else if (currentTab == Tab.ORE_SIM) {
            BlockPos tp = OreSimIntegration.getTestCoord();
            String coordStr = (tp == null) ? "Chưa đặt" : ("(" + tp.getX() + ", " + tp.getY() + ", " + tp.getZ() + ")");
            context.drawTextWithShadow(this.textRenderer, "Trạng thái: " + OreSimIntegration.getDataStatus(), textX, textY + 24, 0xFFF3E8FF);
            context.drawTextWithShadow(this.textRenderer, "Quặng phát hiện: " + OreSimIntegration.getDetectedOre(), textX, textY + 37, 0xFFC4B5FD);
            context.drawTextWithShadow(this.textRenderer, "Tọa độ thử nghiệm: " + coordStr, textX, textY + 50, 0xFFE9D5FF);
            context.drawTextWithShadow(this.textRenderer, "Cập nhật lúc: " + OreSimIntegration.getLastUpdated(), textX, textY + 63, 0xFFA78BFA);
            context.drawTextWithShadow(this.textRenderer, "Lỗi gần nhất: " + OreSimIntegration.getLastError(), textX, textY + 76, 0xFFFF8A65);

        } else if (currentTab == Tab.MUC_TIEU) {
            BlockPos pos = TargetManager.getTargetPos();
            String posStr = (pos == null) ? "Không có" : ("(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
            context.drawTextWithShadow(this.textRenderer, "Mục tiêu: " + TargetManager.getActiveTargetOre(), textX, textY, 0xFFF3E8FF);
            context.drawTextWithShadow(this.textRenderer, "Tọa độ: " + posStr + " | Khoảng cách: " + TargetManager.getDistanceText(client), textX, textY + 13, 0xFFC4B5FD);
            context.drawTextWithShadow(this.textRenderer, "Trạng thái: " + TargetManager.getTargetState(), textX, textY + 26, 0xFFE9D5FF);

        } else if (currentTab == Tab.SINH_TON) {
            context.drawTextWithShadow(this.textRenderer, "Trạng thái sinh tồn: " + SurvivalManager.getStatusText(), textX, textY + 24, 0xFFF3E8FF);
            context.drawTextWithShadow(this.textRenderer, "Dung nham gần: " + (SurvivalManager.isLavaNearby() ? "Có" : "Không")
                    + " | Quái vật gần: " + (SurvivalManager.isMobNearby() ? "Có" : "Không"), textX, textY + 37, 0xFFC4B5FD);

        } else if (currentTab == Tab.KHO_DO) {
            String dur = InventoryManager.getLowestToolDurability() < 0 ? "Không có" : String.valueOf(InventoryManager.getLowestToolDurability());
            context.drawTextWithShadow(this.textRenderer, "Ô trống túi đồ: " + InventoryManager.getEmptySlots() + "/36", textX, textY, 0xFFF3E8FF);
            context.drawTextWithShadow(this.textRenderer, "Bánh mì: " + InventoryManager.getBreadCount() + " (Cảnh báo <= " + cfg.lowBreadThreshold + ")", textX, textY + 13, 0xFFC4B5FD);
            context.drawTextWithShadow(this.textRenderer, "Công cụ: " + InventoryManager.getToolName() + " | Độ bền: " + dur, textX, textY + 26, 0xFFE9D5FF);

        } else if (currentTab == Tab.THONG_BAO) {
            List<NotificationManager.Toast> hist = NotificationManager.getHistory();
            int y = textY;
            int shown = 0;
            for (NotificationManager.Toast t : hist) {
                if (y + 12 > panelBottom - 8) break;
                context.drawTextWithShadow(this.textRenderer, "[" + t.timestamp() + "] " + t.title() + ": " + t.detail(), textX, y, t.color());
                y += 12;
                shown++;
            }
            if (shown == 0) {
                context.drawTextWithShadow(this.textRenderer, "Chưa có thông báo nào được ghi nhận.", textX, y, 0xFFA78BFA);
            }

        } else if (currentTab == Tab.NHAT_KY) {
            List<LogManager.Entry> entries = LogManager.getEntries(null);
            int maxLines = Math.max(1, (panelBottom - textY - 8) / 12);
            int start = Math.min(logScroll, Math.max(0, entries.size() - maxLines));
            int y = textY;
            for (int i = start; i < entries.size() && i < start + maxLines; i++) {
                LogManager.Entry e = entries.get(i);
                String line = String.format("[%s] [%s] [%s] %s", e.time(), e.level().label, e.module(), e.message());
                context.drawTextWithShadow(this.textRenderer, this.textRenderer.trimToWidth(line, this.width - 36), textX, y, e.level().color());
                y += 12;
            }
            if (entries.isEmpty()) {
                context.drawTextWithShadow(this.textRenderer, "Nhật ký đang trống.", textX, y, 0xFFA78BFA);
            }

        } else if (currentTab == Tab.HO_SO) {
            context.drawTextWithShadow(this.textRenderer, "Hồ sơ đang dùng: " + cfg.activeProfile, textX, textY, 0xFFF3E8FF);
            context.drawTextWithShadow(this.textRenderer, "Danh sách hồ sơ: " + String.join(", ", cfg.profiles), textX, textY + 13, 0xFFC4B5FD);

        } else if (currentTab == Tab.GIAO_DIEN) {
            context.drawTextWithShadow(this.textRenderer, "Vị trí HUD: X=" + cfg.hudX + ", Y=" + cfg.hudY + ", Tỷ lệ=" + cfg.hudScale, textX, textY, 0xFFF3E8FF);
            context.drawTextWithShadow(this.textRenderer, "Mở giao diện: Nhấn phím M hoặc dùng lệnh /spicygui", textX, textY + 13, 0xFFC4B5FD);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
