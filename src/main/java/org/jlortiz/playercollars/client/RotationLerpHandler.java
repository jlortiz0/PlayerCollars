package org.jlortiz.playercollars.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = PlayerCollarsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RotationLerpHandler {
    private static final float TIME_TO_TURN = 0.25f;
    private static float turnTimer = TIME_TO_TURN + 1;
    private static float rotX = 0;
    private static float rotY = 0;
    private static long millis = 0;

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        if (turnTimer >= TIME_TO_TURN) return;

        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) { turnTimer = TIME_TO_TURN + 1; return; }

        long mils = net.minecraft.Util.getMillis();
        float delta = (mils - millis) / 1000.0f;
        p.turn(rotY * delta, rotX * delta);
        turnTimer += delta;
        millis = mils;
    }

    public static void beginClickTurn(Vec3 towards) {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        turnTimer = 0;
        Vec3 pos = EntityAnchorArgument.Anchor.EYES.apply(player).subtract(towards);
        double d3 = Math.sqrt(pos.x * pos.x + pos.z * pos.z);
        rotX = Mth.wrapDegrees((float)((Mth.atan2(pos.y, d3) * Mth.RAD_TO_DEG) - player.getXRot())) / TIME_TO_TURN / 0.15f;
        rotY = Mth.wrapDegrees((float)(-Mth.atan2(-pos.z, pos.x) * Mth.RAD_TO_DEG) + 90.0F - player.getYRot()) / TIME_TO_TURN / 0.15f;
        millis = net.minecraft.Util.getMillis();
    }
}
