package org.jlortiz.playercollars.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.DyeColor;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.concurrent.CompletableFuture;

public class RecipeDataGenerator extends FabricRecipeProvider {
    public RecipeDataGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PlayerCollarsMod.COLLAR_ITEM).pattern(" l ").pattern("lil").pattern(" d ")
                .input('l', Items.LEATHER)
                .input('i', ConventionalItemTags.GOLD_INGOTS)
                .input('d', ConventionalItemTags.DYES)
                .criterion(FabricRecipeProvider.hasItem(Items.LEATHER),
                        FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PlayerCollarsMod.CLICKER_ITEM).pattern(" b ").pattern("pip").pattern(" p ")
                .input('b', ItemTags.BUTTONS)
                .input('i', ConventionalItemTags.IRON_INGOTS)
                .input('p', ItemTags.PLANKS)
                .criterion(FabricRecipeProvider.hasItem(Items.LEATHER),
                        FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
                .offerTo(exporter);
        for (DyeColor c : DyeColor.values())
            generateBed(exporter, PlayerCollarsMod.DOG_BED_ITEMS[c.ordinal()], DatagenEntrypoint.WOOLS[c.ordinal()]);
    }

    private void generateBed(RecipeExporter exporter, BedItem output, Item input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).pattern("w w").pattern("www")
                .input('w', input)
                .criterion(FabricRecipeProvider.hasItem(input),
                        FabricRecipeProvider.conditionsFromItem(input))
                .group("dog_bed")
                .offerTo(exporter);
    }
}
