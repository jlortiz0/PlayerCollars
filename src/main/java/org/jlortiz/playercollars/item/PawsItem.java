package org.jlortiz.playercollars.item;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.List;
import java.util.Optional;

public class PawsItem extends FootPawsItem {
    public PawsItem(int color, int pawColor) {
        super(color, pawColor);
    }

    public static boolean shouldPreventBlockInteraction(ItemStack stack, @NotNull BlockState block) {
        if (block.is(PlayerCollarsMod.PAWS_ALLOW_INTERACT)) return false;

        List<Either<TagKey<Block>, ResourceKey<Block>>> allowed = NbtUtil.getCanInteract(stack);
        Optional<ResourceKey<Block>> key = block.getBlockHolder().unwrapKey();

        if (allowed == null || allowed.isEmpty() || key.isEmpty()) return false;

        for (Either<TagKey<Block>, ResourceKey<Block>> entry : allowed) {
            if (entry.map(block::is, y -> y.equals(key.get()))) return false;
        }
        return true;
    }

    public static boolean shouldDrop(ItemStack pawsStack, ItemStack thing) {
        if (thing.isEmpty()) return false;

        List<Either<TagKey<Item>, ResourceKey<Item>>> slippery = NbtUtil.getHeldItems(pawsStack);
        Optional<ResourceKey<Item>> key = thing.getItemHolder().unwrapKey();

        if (slippery == null || slippery.isEmpty() || key.isEmpty()) return false;

        for (Either<TagKey<Item>, ResourceKey<Item>> entry : slippery) {
            if (entry.map(thing::is, y -> y.equals(key.get()))) return false;
        }
        return true;
    }

    public static ResourceLocation getIdentifier(DyeColor c) {
        return new ResourceLocation(PlayerCollarsMod.MOD_ID, c.getName() + "_paws");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, @NotNull TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        if (!NbtUtil.getHeldItems(stack).isEmpty())
            tooltip.add(Component.translatable("item.playercollars.paws.slippery"));
        if (!NbtUtil.getCanInteract(stack).isEmpty())
            tooltip.add(Component.translatable("item.playercollars.paws.interaction"));
    }
}
