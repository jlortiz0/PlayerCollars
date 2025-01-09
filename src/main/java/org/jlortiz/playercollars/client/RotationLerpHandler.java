package org.jlortiz.playercollars.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationLerpHandler {
    private static final float timeToTurn = 0.25f;
    private static float turnTimer = timeToTurn + 1;
    private static float rotX;
    private static float rotY;
    private static long millis;

    public static void turnTowardsClick(WorldRenderContext ctx) {
        if (turnTimer < timeToTurn) {
            ClientPlayerEntity p = MinecraftClient.getInstance().player;
            if (p == null) {
                turnTimer = timeToTurn + 1;
                return;
            }
            // FIXME: there has to be a real timer for this
            long mils = Util.getEpochTimeMs();
            float delta = (mils - millis) / 1000f;
            p.changeLookDirection(rotY * delta, rotX * delta);
            turnTimer += delta;
            millis = mils;
        }
    }

    public static void beginClickTurn(Vec3d towards) {
        PlayerEntity p = MinecraftClient.getInstance().player;
        turnTimer = 0;
        Vec3d pos = EntityAnchorArgumentType.EntityAnchor.EYES.positionAt(p).subtract(towards);
        double d3 = Math.sqrt(pos.x * pos.x + pos.z * pos.z);
        rotX = MathHelper.wrapDegrees((float)((MathHelper.atan2(pos.y, d3) * MathHelper.DEGREES_PER_RADIAN) - p.getPitch())) / timeToTurn / 0.15f;
        rotY = MathHelper.wrapDegrees((float)(-MathHelper.atan2(-pos.z, pos.x) * MathHelper.DEGREES_PER_RADIAN) + 90.0F - p.getYaw()) / timeToTurn / 0.15f;
        millis = Util.getEpochTimeMs();
    }
}
