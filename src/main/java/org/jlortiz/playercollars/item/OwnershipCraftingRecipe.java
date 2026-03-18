package org.jlortiz.playercollars.item;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;

public class OwnershipCraftingRecipe extends CustomRecipe {
    private final Ingredient base;

    public OwnershipCraftingRecipe(ResourceLocation id, CraftingBookCategory category, Ingredient base) {
        super(id, category);
        this.base = base;
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level world) {
        boolean seenDeed = false;
        boolean seenBase = false;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack is = inventory.getItem(i);
            if (is.isEmpty()) continue;
            if (is.is(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED.get())) {
                if (seenDeed) return false;
                seenDeed = true;
            } else if (this.base.test(is)) {
                if (seenBase) return false;
                seenBase = true;
            } else return false;
        }
        return seenDeed && seenBase;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess registryAccess) {
        ItemStack output = ItemStack.EMPTY;
        OwnerComponent owner = null;
        for (int j = 0; j < inventory.getContainerSize(); j++) {
            ItemStack is = inventory.getItem(j);
            if (!is.isEmpty()) {
                if (is.is(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED.get())) {
                    owner = NbtUtil.getDeedOwner(is);
                } else if (this.base.test(is)) {
                    output = is.copy();
                }
            }
        }
        if (owner == null || output.isEmpty()) return ItemStack.EMPTY;
        NbtUtil.setDeedOwner(output, owner);
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height > 2; }

    @Override
    public RecipeSerializer<?> getSerializer() { return Serializer.INSTANCE; }

    @FunctionalInterface
    public interface RecipeFactory {
        OwnershipCraftingRecipe create(ResourceLocation id, CraftingBookCategory category, Ingredient base);
    }

    public static class Serializer implements RecipeSerializer<OwnershipCraftingRecipe> {
        public static final Serializer INSTANCE = new Serializer(OwnershipCraftingRecipe::new);
        private final RecipeFactory factory;

        public Serializer(RecipeFactory factory) { this.factory = factory; }

        @Override
        public OwnershipCraftingRecipe fromJson(ResourceLocation id, JsonObject json) {
            CraftingBookCategory category = CraftingBookCategory.CODEC
                    .byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"), false);
            return this.factory.create(id, category, ingredient);
        }

        @Override
        public OwnershipCraftingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            Ingredient ingredient = Ingredient.fromNetwork(buf);
            return this.factory.create(id, category, ingredient);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, OwnershipCraftingRecipe recipe) {
            buf.writeEnum(recipe.category());
            recipe.base.toNetwork(buf);
        }
    }
}
