package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.LaserPointerItem;
import org.joml.Matrix4f;

public class LaserRenderer {

    public static void register() {
        // This event runs every single frame right before the screen is finished drawing!
        WorldRenderEvents.LAST.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) return;

            // ✨ Loop through EVERY player the client can see!
            for (net.minecraft.client.network.AbstractClientPlayerEntity player : client.world.getPlayers()) {
                ItemStack mainHand = player.getMainHandStack();
                ItemStack offHand = player.getOffHandStack();
                ItemStack laser = mainHand.isOf(PlayerCollarsMod.LASER_POINTER_ITEM) ? mainHand : (offHand.isOf(PlayerCollarsMod.LASER_POINTER_ITEM) ? offHand : null);

                if (laser != null) {
                    int reachLevel = 0;
                    var reg = client.world.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT);
                    if (reg.isPresent()) {
                        var entry = reg.get().getOptional(LaserPointerItem.LASER_REACH_KEY);
                        if (entry.isPresent()) {
                            reachLevel = net.minecraft.enchantment.EnchantmentHelper.getLevel(entry.get(), laser);
                        }
                    }

                    double maxDistance = 32.0 * (1.0 + reachLevel);
                    HitResult hit = player.raycast(maxDistance, context.tickCounter().getTickDelta(true), false);

                    // ✨ It only renders if it successfully hits a block within range!
                    if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
                        drawLaserSquare(context, blockHit);
                    }
                }
            }
        });
    }

    private static void drawLaserSquare(WorldRenderContext context, BlockHitResult hit) {
        Vec3d cameraPos = context.camera().getPos();
        Vec3d hitPos = hit.getPos();
        Direction side = hit.getSide();

        MatrixStack matrices = context.matrixStack();
        matrices.push();

        // 1. Move to the exact spot we hit, relative to the camera
        matrices.translate(hitPos.x - cameraPos.x, hitPos.y - cameraPos.y, hitPos.z - cameraPos.z);

        // 2. Push the square out from the block by a tiny fraction so it doesn't clip into the wall (Z-fighting)
        matrices.translate(side.getOffsetX() * 0.015f, side.getOffsetY() * 0.015f, side.getOffsetZ() * 0.015f);

        // 3. Rotate the matrix so our square lies perfectly flat against the block face!
        matrices.multiply(side.getRotationQuaternion());

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // The size of your laser dot!
        float size = 0.05f;

        // 4. Draw the 4 corners of our bright green square! (R, G, B, Alpha)
        buffer.vertex(positionMatrix, -size, -size, 0).color(0, 255, 0, 255);
        buffer.vertex(positionMatrix, -size, size, 0).color(0, 255, 0, 255);
        buffer.vertex(positionMatrix, size, size, 0).color(0, 255, 0, 255);
        buffer.vertex(positionMatrix, size, -size, 0).color(0, 255, 0, 255);

        // 5. Tell the game to render our colored shape without caring about shadows!
        RenderSystem.setShader(net.minecraft.client.gl.ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.disableDepthTest(); // This makes it glow visibly even through tall grass!

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableDepthTest(); // Turn depth test back on so we don't break other rendering
        matrices.pop();
    }
}