package org.jlortiz.playercollars.mixin;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.PawsItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Shadow @Final private MinecraftClient client;

    @Unique
    private static boolean shouldPawsBlock(LivingEntity player, BlockState block, boolean isBreak) {
        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return false;
        for (SlotEntryReference sr : cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.PAWS_TAG))) {
            if (PawsItem.shouldPreventBlockInteraction(sr.stack(), block, isBreak)) {
                return true;
            }
        }
        return false;
    }

    @Inject(method="interactBlock", at=@At("HEAD"), cancellable = true)
    private void playercollars$cancelPawInteractions(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (player.isSpectator()) return;
        BlockState block = this.client.world.getBlockState(hitResult.getBlockPos());
        if (shouldPawsBlock(player, block, false)) cir.setReturnValue(ActionResult.PASS);
    }

    @Inject(method = "attackBlock", at = @At(value = "HEAD"), cancellable = true)
    private void playercollars$cancelPawBreak(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        BlockState block = this.client.world.getBlockState(pos);
        if (shouldPawsBlock(client.player, block, true)) cir.setReturnValue(false);
    }
}
