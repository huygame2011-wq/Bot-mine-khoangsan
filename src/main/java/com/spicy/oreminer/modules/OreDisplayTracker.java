package com.spicy.oreminer.modules;

import com.spicy.oreminer.config.ConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OreDisplayTracker {
    private static final Map<BlockPos, Block> ACTIVE_DISPLAY_ORES = new LinkedHashMap<>();
    private static int tickCounter = 0;

    private OreDisplayTracker() {}

    public static void tick(MinecraftClient mc) {
        if (mc == null || mc.player == null || mc.world == null) {
            clear();
            return;
        }

        ConfigManager cfg = ConfigManager.get();
        if (!cfg.systemEnabled || !cfg.oreSimEnabled) {
            clear();
            return;
        }

        tickCounter++;
        if (tickCounter % 10 != 0) {
            return;
        }

        scanNearbyOres(mc, cfg);
    }

    private static void scanNearbyOres(MinecraftClient mc, ConfigManager cfg) {
        Map<BlockPos, Block> found = new LinkedHashMap<>();
        BlockPos origin = mc.player.getBlockPos();

        int xz = Math.max(1, Math.min(24, cfg.scanRadiusXz));
        int yRange = Math.max(1, Math.min(16, cfg.scanRadiusY));

        for (int dx = -xz; dx <= xz; dx++) {
            for (int dy = -yRange; dy <= yRange; dy++) {
                for (int dz = -xz; dz <= xz; dz++) {
                    BlockPos pos = origin.add(dx, dy, dz);
                    Block block = mc.world.getBlockState(pos).getBlock();
                    if (isTrackedOre(block)) {
                        found.put(pos.toImmutable(), block);
                        if (found.size() >= 64) {
                            ACTIVE_DISPLAY_ORES.clear();
                            ACTIVE_DISPLAY_ORES.putAll(found);
                            return;
                        }
                    }
                }
            }
        }

        ACTIVE_DISPLAY_ORES.clear();
        ACTIVE_DISPLAY_ORES.putAll(found);
    }

    private static boolean isTrackedOre(Block block) {
        return block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE
                || block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE
                || block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE
                || block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE
                || block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE
                || block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
                || block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE
                || block == Blocks.ANCIENT_DEBRIS;
    }

    public static Map<BlockPos, Block> getActiveDisplayOres() {
        if (!ConfigManager.get().systemEnabled) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(ACTIVE_DISPLAY_ORES);
    }

    public static void clear() {
        ACTIVE_DISPLAY_ORES.clear();
        tickCounter = 0;
    }
}
