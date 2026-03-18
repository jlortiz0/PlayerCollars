package org.jlortiz.playercollars.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.PawsItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Unique
    private static boolean shouldPawsBlock(LivingEntity player, BlockState block) {
        return CuriosApi.getCuriosInventory(player)
                .map(h -> h.findCurios(s -> s.is(PlayerCollarsMod.PAWS_TAG)))
                .map(ls -> ls.stream().anyMatch(sr -> PawsItem.shouldPreventBlockInteraction(sr.stack(), block)))
                .orElse(false);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void playercollars$cancelPawInteractions(LocalPlayer player, InteractionHand hand,
                                                      BlockHitResult hitResult,
                                                      CallbackInfoReturnable<InteractionResult> cir) {
        if (player.isSpectator()) return;
        assert this.minecraft.level != null;
        BlockState block = this.minecraft.level.getBlockState(hitResult.getBlockPos());
        if (shouldPawsBlock(player, block)) cir.setReturnValue(InteractionResult.PASS);
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void playercollars$cancelPawBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        assert this.minecraft.level != null;
        BlockState block = this.minecraft.level.getBlockState(pos);
        if (shouldPawsBlock(this.minecraft.player, block)) cir.setReturnValue(false);
    }
}
