package org.jlortiz.playercollars.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class InvisibleFenceBlockItem extends BlockItem {
    public InvisibleFenceBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        PlayerEntity player = context.getPlayer();
        if (player != null && PlayerCollarsMod.isPet(player)) {
            if (!context.getWorld().isClient) {
                player.sendMessage(Text.translatable("item.playercollars.invisible_fence.place_fail").formatted(Formatting.RED), true);
            }
            return false;
        }
        return super.canPlace(context, state);
    }
}
