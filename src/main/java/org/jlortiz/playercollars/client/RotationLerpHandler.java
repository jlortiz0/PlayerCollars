package org.jlortiz.playercollars.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RotationLerpHandler {
    private static final float timeToTurn = 0.25f;
    private static float turnTimer = timeToTurn + 1;
    private static float rotX;
    private static float rotY;
    private static long millis;

    @SubscribeEvent
    public void turnTowardsClick(ViewportEvent.ComputeCameraAngles evt) {
        if (turnTimer < timeToTurn) {
            LocalPlayer p = Minecraft.getInstance().player;
            if (p == null) {
                turnTimer = timeToTurn + 1;
                return;
            }
            // FIXME: there has to be a real timer for this
            long mils = Util.getMillis();
            float delta = (mils - millis) / 1000f;
            p.turn(rotY * delta, rotX * delta);
            turnTimer += delta;
            millis = mils;
        }
    }

    public static void beginClickTurn(Vec3 towards) {
        Player p = Minecraft.getInstance().player;
        turnTimer = 0;
        Vec3 pos = EntityAnchorArgument.Anchor.EYES.apply(p).subtract(towards);
        double d3 = Math.sqrt(pos.x * pos.x + pos.z * pos.z);
        rotX = Mth.wrapDegrees((float)((Mth.atan2(pos.y, d3) * Mth.RAD_TO_DEG) - p.getXRot())) / timeToTurn / 0.15f;
        rotY = Mth.wrapDegrees((float)(-Mth.atan2(-pos.z, pos.x) * Mth.RAD_TO_DEG) + 90.0F - p.getYRot()) / timeToTurn / 0.15f;
        millis = Util.getMillis();
    }
}
