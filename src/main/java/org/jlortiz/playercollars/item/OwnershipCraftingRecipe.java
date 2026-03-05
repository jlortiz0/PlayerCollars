package org.jlortiz.playercollars.item;

import com.google.gson.JsonObject;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;

public class OwnershipCraftingRecipe extends SpecialCraftingRecipe {
    private final Ingredient base;

    public OwnershipCraftingRecipe(Identifier id, CraftingRecipeCategory category, Ingredient base) {
        super(id, category);
        this.base = base;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        boolean seenDeed = false;
        boolean seenBase = false;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack is = inventory.getStack(i);
            if (is.isEmpty())
                continue;
            if (is.isOf(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED)) {
                if (seenDeed)
                    return false;

                seenDeed = true;
            } else if (this.base.test(is)) {
                if (seenBase)
                    return false;

                seenBase = true;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        ItemStack output = ItemStack.EMPTY;
        OwnerComponent owner = null;

        for (int j = 0; j < inventory.size(); j++) {
            ItemStack is = inventory.getStack(j);
            if (!is.isEmpty()) {
                if (is.isOf(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED)) {
                    owner = NbtUtil.getDeedOwner(is);
                } else if (this.base.test(is)) {
                    output = is.copy();
                }
            }
        }

        if (owner == null || output.isEmpty())
            return ItemStack.EMPTY;

        NbtUtil.setDeedOwner(output, owner);
        return output;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height > 2;
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    @FunctionalInterface
    public interface RecipeFactory {
        OwnershipCraftingRecipe create(Identifier id, CraftingRecipeCategory category, Ingredient base);
    }

    public static class Serializer implements RecipeSerializer<OwnershipCraftingRecipe> {
        public static final Serializer INSTANCE = new Serializer(OwnershipCraftingRecipe::new);

        private final RecipeFactory factory;

        public Serializer(RecipeFactory factory) {
            this.factory = factory;
        }

        @Override
        public OwnershipCraftingRecipe read(Identifier id, JsonObject json) {
            CraftingRecipeCategory category = CraftingRecipeCategory.CODEC
                    .byId(JsonHelper.getString(json, "category", null), CraftingRecipeCategory.MISC);

            Ingredient ingredient = Ingredient.fromJson(json, false);

            return this.factory.create(id, category, ingredient);
        }

        @Override
        public OwnershipCraftingRecipe read(Identifier id, PacketByteBuf buf) {
            CraftingRecipeCategory category = buf.readEnumConstant(CraftingRecipeCategory.class);
            Ingredient ingredient = Ingredient.fromPacket(buf);

            return this.factory.create(id, category, ingredient);
        }

        @Override
        public void write(PacketByteBuf buf, OwnershipCraftingRecipe recipe) {
            buf.writeEnumConstant(recipe.getCategory());
            recipe.base.write(buf);
        }
    }
}
