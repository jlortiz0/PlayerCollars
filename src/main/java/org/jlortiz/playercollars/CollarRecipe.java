package org.jlortiz.playercollars;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CollarRecipe extends ShapedRecipe {
    private static final Ingredient ITEM_LEATHER = Ingredient.of(Items.LEATHER);
    private static final Ingredient ITEM_INGOTS = PlayerCollarItem.TagType.getIngredient();
    private static final Ingredient ITEM_DYE = Ingredient.of(Tags.Items.DYES);
    public CollarRecipe(ResourceLocation rl) {
        super(rl, "collar", 3, 3, NonNullList.of(
                Ingredient.EMPTY, Ingredient.EMPTY, ITEM_LEATHER, Ingredient.EMPTY, ITEM_LEATHER, ITEM_INGOTS, ITEM_LEATHER, Ingredient.EMPTY, ITEM_DYE, Ingredient.EMPTY
        ), new ItemStack(PlayerCollarsMod.COLLAR_ITEM.get()));
    }

    public static class Type implements RecipeType<CollarRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "collar";
    }

    public static final RecipeSerializer<CollarRecipe> Serializer = new SimpleRecipeSerializer<>(CollarRecipe::new);

    @Override
    public @NotNull ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack ingot = craftingContainer.getItem(4);
        PlayerCollarItem.TagType t = null;
        for (PlayerCollarItem.TagType tag : PlayerCollarItem.TagType.values()) {
            if (Ingredient.of(tag.item).test(ingot)) {
                t = tag;
                break;
            }
        }
        if (t == null) return ItemStack.EMPTY;
        DyeItem dye = (DyeItem) craftingContainer.getItem(7).getItem();
        return PlayerCollarItem.getInstance(t, dye.getDyeColor().getMaterialColor().col);
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return i == 3 && i1 == 3;
    }

    @Override
    public RecipeSerializer<CollarRecipe> getSerializer() {
        return Serializer;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
