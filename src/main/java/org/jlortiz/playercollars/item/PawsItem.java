package org.jlortiz.playercollars.item;

import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PawsItem extends FootPawsItem {
    public PawsItem(RegistryKey<Item> key, int color, int pawColor) {
        super(key, color, pawColor);
    }

    public static boolean shouldPreventBlockInteraction(ItemStack stack, @NotNull BlockState block) {
        if (block.isIn(PlayerCollarsMod.PAWS_ALLOW_INTERACT)) return false;
        List<Either<TagKey<Block>, RegistryKey<Block>>> allowed = stack.get(PlayerCollarsMod.CAN_INTERACT_COMPONENT_TYPE);
        Optional<RegistryKey<Block>> key = block.getRegistryEntry().getKey();
        if (allowed == null || key.isEmpty()) return false;
        for (Either<TagKey<Block>, RegistryKey<Block>> entry : allowed) {
            if (entry.map(block::isIn, (y) -> y.equals(key.get()))) return false;
        }
        return true;
    }

    public static boolean shouldDrop(ItemStack pawsStack, ItemStack thing) {
        if (thing.isEmpty()) return false;
        List<Either<TagKey<Item>, RegistryKey<Item>>> slippery = pawsStack.get(PlayerCollarsMod.HELD_ITEMS_COMPONENT_TYPE);
        Optional<RegistryKey<Item>> key = thing.getRegistryEntry().getKey();
        if (slippery == null || key.isEmpty()) return false;
        for (Either<TagKey<Item>, RegistryKey<Item>> entry : slippery) {
            if (entry.map(thing::isIn, (y) -> y.equals(key.get()))) return false;
        }
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        if (stack.get(PlayerCollarsMod.HELD_ITEMS_COMPONENT_TYPE) != null) textConsumer.accept(Text.translatable("item.playercollars.paws.slippery"));
        if (stack.get(PlayerCollarsMod.CAN_INTERACT_COMPONENT_TYPE) != null) textConsumer.accept(Text.translatable("item.playercollars.paws.interaction"));
    }

    public static RegistryKey<Item> getRegistryKey(DyeColor c) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, c.getId() + "_paws"));
    }
}
