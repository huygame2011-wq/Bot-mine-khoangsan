package com.spicy.oreminer.modules;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class OreDisplayTracker {
    private static final Map<BlockPos, Block> ACTIVE_DISPLAY_ORES = new LinkedHashMap<>();
    private static final Set<BlockPos> MINED_POSITIONS = new HashSet<>();

    private OreDisplayTracker() {}

    public static boolean isTargetOre(Block block) {
        return block == Blocks.DIAMOND_ORE
                || block == Blocks.DEEPSLATE_DIAMOND_ORE
                || block == Blocks.ANCIENT_DEBRIS;
    }

    public static void markOrUpdateOre(BlockPos pos, Block block) {
        BlockPos immutable = pos.toImmutable();
        if (MINED_POSITIONS.contains(immutable) || !isTargetOre(block)) {
            ACTIVE_DISPLAY_ORES.remove(immutable);
            return;
        }
        ACTIVE_DISPLAY_ORES.put(immutable, block);
    }

    public static void onBlockMined(BlockPos pos) {
        BlockPos immutable = pos.toImmutable();
        ACTIVE_DISPLAY_ORES.remove(immutable);
        MINED_POSITIONS.add(immutable);
    }

    public static void tick(MinecraftClient mc) {
        if (mc == null || mc.world == null) return;

        Iterator> it = ACTIVE_DISPLAY_ORES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Block> entry = it.next();
            BlockPos pos = entry.getKey();
            Block currentBlock = mc.world.getBlockState(pos).getBlock();

            if (mc.world.getBlockState(pos).isAir() || currentBlock != entry.getValue()) {
                MINED_POSITIONS.add(pos);
                it.remove();
            }
        }
    }

    public static Map<BlockPos, Block> getActiveDisplayOres() {
        return Collections.unmodifiableMap(ACTIVE_DISPLAY_ORES);
    }

    public static boolean isAlreadyMined(BlockPos pos) {
        return MINED_POSITIONS.contains(pos.toImmutable());
    }
}
