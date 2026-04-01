package org.jlortiz.playercollars.event;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.PawsItem;

public class PawsEventHandler {
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

    public static void registerPawsEvents() {
        AttackBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) ->
                shouldPawsBlock(player, world.getBlockState(pos), true) ? ActionResult.FAIL : ActionResult.PASS);

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) ->
                !shouldPawsBlock(player, world.getBlockState(pos), true));

        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {
            if (player.isSpectator()) return ActionResult.PASS;
            BlockState block = world.getBlockState(hitResult.getBlockPos());
            if (shouldPawsBlock(player, block, false)) return ActionResult.FAIL;
            return ActionResult.PASS;
        });
    }
}
