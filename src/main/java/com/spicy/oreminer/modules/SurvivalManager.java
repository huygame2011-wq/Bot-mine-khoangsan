package com.spicy.oreminer.modules;

import com.spicy.oreminer.config.ConfigManager;
import com.spicy.oreminer.core.LogManager;
import com.spicy.oreminer.core.NotificationManager;
import com.spicy.oreminer.core.SafetyManager;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class SurvivalManager {
    private static String statusText = "An toàn";
    private static boolean lavaNearby = false;
    private static boolean mobNearby = false;

    private SurvivalManager() {}

    public static void tick(MinecraftClient mc) {
        ConfigManager cfg = ConfigManager.get();
        if (!cfg.survivalEnabled) {
            statusText = "Module đang tắt";
            return;
        }
        if (mc.player == null || mc.world == null) return;

        BlockPos origin = mc.player.getBlockPos();
        boolean foundLava = false;

        if (cfg.lavaDetectEnabled) {
            for (int dx = -4; dx <= 4 && !foundLava; dx++) {
                for (int dy = -3; dy <= 3 && !foundLava; dy++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        BlockPos pos = origin.add(dx, dy, dz);
                        if (mc.world.getBlockState(pos).isOf(Blocks.LAVA)) {
                            foundLava = true;
                            break;
                        }
                    }
                }
            }
        }

        boolean foundMob = false;
        if (cfg.mobDetectEnabled) {
            List<HostileEntity> hostiles = mc.world.getEntitiesByClass(
                    HostileEntity.class,
                    mc.player.getBoundingBox().expand(10.0),
                    e -> e.isAlive()
            );
            foundMob = !hostiles.isEmpty();
        }

        if (foundLava && !lavaNearby) {
            LogManager.log(LogManager.Level.CANH_BAO, "Sinh tồn", "Phát hiện dung nham ở gần khu vực.");
            NotificationManager.push("lava_near", "Cảnh báo dung nham", "Có dung nham gần vị trí hiện tại.", 0xFFFF5252, cfg.notifLava);
            if (cfg.systemEnabled) {
                SafetyManager.pause("Phát hiện dung nham gần");
            }
        }
        lavaNearby = foundLava;

        if (foundMob && !mobNearby) {
            LogManager.log(LogManager.Level.CANH_BAO, "Sinh tồn", "Phát hiện quái vật thù địch trong phạm vi 10 block.");
            NotificationManager.push("mob_near", "Cảnh báo quái vật", "Có quái vật nguy hiểm ở gần.", 0xFFFF8A65, cfg.notifMob);
        }
        mobNearby = foundMob;

        if (foundLava) {
            statusText = "Nguy hiểm: Dung nham";
            SafetyManager.setWarning("Dung nham ở gần");
        } else if (foundMob) {
            statusText = "Cảnh báo: Có quái vật";
            SafetyManager.setWarning("Có quái vật gần");
        } else if (mc.player.getHealth() <= 8.0f) {
            statusText = "Cảnh báo: Máu thấp";
            SafetyManager.setWarning("Máu người chơi thấp");
        } else {
            statusText = "An toàn";
            if (!SafetyManager.isPaused() && !SafetyManager.isEmergencyStopped()) {
                SafetyManager.setWarning("Không có cảnh báo");
            }
        }
    }

    public static String getStatusText() { return statusText; }
    public static boolean isLavaNearby() { return lavaNearby; }
    public static boolean isMobNearby() { return mobNearby; }
}
