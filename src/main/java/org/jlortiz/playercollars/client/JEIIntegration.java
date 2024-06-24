package org.jlortiz.playercollars.client;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.Collections;

@JeiPlugin
public class JEIIntegration implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation(PlayerCollarsMod.MOD_ID, "jei_compat");
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                Collections.singleton(new ItemStack(PlayerCollarsMod.CLICKER_ITEM.get())));
    }
}
