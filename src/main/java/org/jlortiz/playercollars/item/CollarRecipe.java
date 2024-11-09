package org.jlortiz.playercollars.item;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CollarRecipe extends SpecialCraftingRecipe {
    private static final Ingredient ITEM_LEATHER = Ingredient.ofItems(Items.LEATHER);
    // Use dummy placeholders since we can't do our silly Ingredient enum hack anymore
    private static final Ingredient ITEM_INGOTS = Ingredient.fromTag(ItemTags.IRON_ORES);
    private static final Ingredient ITEM_DYE = Ingredient.fromTag(ItemTags.TRIMMABLE_ARMOR);
    private static final Ingredient[] ITEMS = {Ingredient.EMPTY, ITEM_LEATHER, Ingredient.EMPTY, ITEM_LEATHER, ITEM_INGOTS, ITEM_LEATHER, Ingredient.EMPTY, ITEM_DYE, Ingredient.EMPTY};
    public CollarRecipe(Identifier rl, CraftingRecipeCategory r2) {
        super(rl, CraftingRecipeCategory.EQUIPMENT);
    }

    public static class Type implements RecipeType<CollarRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "collar";
    }

    public static final RecipeSerializer<CollarRecipe> Serializer = new SpecialRecipeSerializer<>(CollarRecipe::new);

    @Override
    public ItemStack craft(RecipeInputInventory craftingContainer, DynamicRegistryManager p_266725_) {
        ItemStack ingot = craftingContainer.getStack(4);
        CollarItem.TagType t = null;
        for (CollarItem.TagType tag : CollarItem.TagType.values()) {
            if (tag.ingredient.test(ingot)) {
                t = tag;
                break;
            }
        }
        if (t == null) return ItemStack.EMPTY;
        DyeItem dye = (DyeItem) craftingContainer.getStack(7).getItem();
        return CollarItem.getInstance(t, dye.getColor().getMapColor().color);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStack(i);
            Ingredient ingredient = ITEMS[i];
            if (ingredient == ITEM_INGOTS) {
                boolean match = false;
                for (CollarItem.TagType tag : CollarItem.TagType.values()) {
                    if (tag.ingredient.test(stack)) {
                        match = true;
                        break;
                    }
                }
                if (!match) return false;
            } else if (ingredient == ITEM_DYE) {
                if (!(stack.getItem() instanceof DyeItem)) return false;
            } else if (!ingredient.test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean fits(int i, int i1) {
        return i == 3 && i1 == 3;
    }

    @Override
    public RecipeSerializer<CollarRecipe> getSerializer() {
        return Serializer;
    }
}
