package org.jlortiz.playercollars.item;

import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
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
        if (block.isIn(PlayerCollarsMod.PAWS_ALLOW_INTERACT)) return false;
        // TODO 2026-02-12 (solonovamax): finish this
        List<Either<TagKey<Block>, RegistryKey<Block>>> allowed = NbtUtil.getCanInteract(stack);
        Optional<RegistryKey<Block>> key = block.getRegistryEntry().getKey();
        if (allowed == null || key.isEmpty()) return false;
        for (Either<TagKey<Block>, RegistryKey<Block>> entry : allowed) {
            if (entry.map(block::isIn, (y) -> y.equals(key.get()))) return false;
        }
        return true;
    }

    public static boolean shouldDrop(ItemStack pawsStack, ItemStack thing) {
        if (thing.isEmpty()) return false;
        List<Either<TagKey<Item>, RegistryKey<Item>>> slippery = NbtUtil.getHeldItems(pawsStack);
        Optional<RegistryKey<Item>> key = thing.getRegistryEntry().getKey();
        if (slippery == null || key.isEmpty()) return false;
        for (Either<TagKey<Item>, RegistryKey<Item>> entry : slippery) {
            if (entry.map(thing::isIn, (y) -> y.equals(key.get()))) return false;
        }
        return true;
    }

    public static Identifier getIdentifier(DyeColor c) {
        return Identifier.of(PlayerCollarsMod.MOD_ID, c.getName() + "_paws");
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, @NotNull TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (!NbtUtil.getHeldItems(stack).isEmpty()) tooltip.add(Text.translatable("item.playercollars.paws.slippery"));
        if (!NbtUtil.getCanInteract(stack).isEmpty()) tooltip.add(Text.translatable("item.playercollars.paws.interaction"));
    }
}
