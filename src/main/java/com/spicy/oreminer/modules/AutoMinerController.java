package com.spicy.oreminer.modules;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class AutoMinerController {
    private static final int RADIUS_XZ = 6;
    private static final int RADIUS_Y = 4;
    private static final double REACH_DISTANCE = 4.2;

    private static BlockPos currentTarget = null;

    private AutoMinerController() {}

    public static void tick(MinecraftClient mc) {
        if (mc == null || mc.player == null || mc.world == null || mc.interactionManager == null) {
            releaseControls(mc);
            return;
        }

        if (currentTarget != null) {
            BlockState state = mc.world.getBlockState(currentTarget);
            if (state.isAir() || !OreDisplayTracker.isTargetOre(state.getBlock()) || OreDisplayTracker.isAlreadyMined(currentTarget)) {
                OreDisplayTracker.onBlockMined(currentTarget);
                currentTarget = null;
            }
        }

        if (currentTarget == null) {
            currentTarget = findNearestOre(mc);
        }

        if (currentTarget == null) {
            releaseControls(mc);
            return;
        }

        Vec3d targetCenter = Vec3d.ofCenter(currentTarget);
        Vec3d eyePos = mc.player.getEyePos();
        double distSq = eyePos.squaredDistanceTo(targetCenter);

        faceTarget(mc, targetCenter);

        if (distSq > REACH_DISTANCE * REACH_DISTANCE) {
            mc.options.forwardKey.setPressed(true);
            mc.options.attackKey.setPressed(false);
            mc.options.jumpKey.setPressed(mc.player.horizontalCollision);
            return;
        }

        mc.options.forwardKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);

        Direction side = Direction.getFacing(
                targetCenter.x - eyePos.x,
                targetCenter.y - eyePos.y,
                targetCenter.z - eyePos.z
        ).getOpposite();

        mc.options.attackKey.setPressed(true);
        mc.interactionManager.updateBlockBreakingProgress(currentTarget, side);
        mc.player.swingHand(Hand.MAIN_HAND);

        BlockState afterState = mc.world.getBlockState(currentTarget);
        if (afterState.isAir() || !OreDisplayTracker.isTargetOre(afterState.getBlock())) {
            mc.options.attackKey.setPressed(false);
            OreDisplayTracker.onBlockMined(currentTarget);
            currentTarget = null;
        }
    }

    private static BlockPos findNearestOre(MinecraftClient mc) {
        BlockPos origin = mc.player.getBlockPos();
        BlockPos bestPos = null;
        double bestDistSq = Double.MAX_VALUE;

        for (int dx = -RADIUS_XZ; dx <= RADIUS_XZ; dx++) {
            for (int dy = -RADIUS_Y; dy <= RADIUS_Y; dy++) {
                for (int dz = -RADIUS_XZ; dz <= RADIUS_XZ; dz++) {
                    BlockPos pos = origin.add(dx, dy, dz);
                    if (OreDisplayTracker.isAlreadyMined(pos)) continue;

                    BlockState state = mc.world.getBlockState(pos);
                    if (OreDisplayTracker.isTargetOre(state.getBlock())) {
                        OreDisplayTracker.markOrUpdateOre(pos, state.getBlock());
                        double dSq = mc.player.squaredDistanceTo(Vec3d.ofCenter(pos));
                        if (dSq < bestDistSq) {
                            bestDistSq = dSq;
                            bestPos = pos.toImmutable();
                        }
                    }
                }
            }
        }
        return bestPos;
    }

    private static void faceTarget(MinecraftClient mc, Vec3d target) {
        Vec3d eye = mc.player.getEyePos();
        double diffX = target.x - eye.x;
        double diffY = target.y - eye.y;
        double diffZ = target.z - eye.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float targetYaw = (float) (MathHelper.atan2(diffZ, diffX) * (180.0 / Math.PI)) - 90.0f;
        float targetPitch = (float) (-(MathHelper.atan2(diffY, diffXZ) * (180.0 / Math.PI)));

        mc.player.setYaw(MathHelper.lerpAngleDegrees(0.45f, mc.player.getYaw(), targetYaw));
        mc.player.setPitch(MathHelper.lerpAngleDegrees(0.45f, mc.player.getPitch(), targetPitch));
    }

    private static void releaseControls(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;
        mc.options.forwardKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.attackKey.setPressed(false);
    }
}
