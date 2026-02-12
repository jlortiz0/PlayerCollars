package org.jlortiz.playercollars.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.DyeColor;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.FootPawsItem;
import org.jlortiz.playercollars.item.PawsItem;

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
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PlayerCollarsMod.TAGLESS_COLLAR_ITEM).pattern(" l ").pattern("ldl")
                .input('l', Items.LEATHER)
                .input('d', ConventionalItemTags.DYES)
                .criterion(FabricRecipeProvider.hasItem(Items.LEATHER),
                        FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, PlayerCollarsMod.CLICKER_ITEM).pattern(" b ").pattern("pip").pattern(" p ")
                .input('b', ItemTags.BUTTONS)
                .input('i', ConventionalItemTags.IRON_INGOTS)
                .input('p', ItemTags.PLANKS)
                .criterion(FabricRecipeProvider.hasItem(Items.IRON_INGOT),
                        FabricRecipeProvider.conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, PlayerCollarsMod.PAW_CONFIGURATION_ITEM)
                .input(ConventionalItemTags.REDSTONE_DUSTS)
                .input(ConventionalItemTags.COPPER_INGOTS)
                .input(PlayerCollarsMod.COLLAR_LOCKER_ITEM)
                .criterion(FabricRecipeProvider.hasItem(PlayerCollarsMod.PAWS_ITEMS[0]),
                        FabricRecipeProvider.conditionsFromTag(PlayerCollarsMod.PAWS_TAG))
                .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, PlayerCollarsMod.COLLAR_LOCKER_ITEM)
                .input(ConventionalItemTags.REDSTONE_DUSTS)
                .input(Items.CHAIN)
                .input(Items.CHAIN)
                .input(Items.IRON_BARS)
                .criterion(FabricRecipeProvider.hasItem(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED),
                        FabricRecipeProvider.conditionsFromItem(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED))
                .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, PlayerCollarsMod.DEED_OF_OWNERSHIP)
                .input(Items.PAPER)
                .input(Items.LEAD)
                .input(Items.INK_SAC)
                .input(Items.FEATHER)
                .criterion(hasItem(Items.PAPER), conditionsFromItem(Items.PAPER))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, PlayerCollarsMod.INVISIBLE_FENCE_BLOCK_ITEM, 3).pattern("grg").pattern("srs")
                .input('r', Items.REDSTONE)
                .input('g', Items.GLASS_PANE)
                .input('s', Items.STONE)
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, PlayerCollarsMod.SPATULA_ITEM).pattern("  g").pattern(" g ").pattern("s  ")
                .input('g', ConventionalItemTags.GOLD_INGOTS)
                .input('s', Items.STICK)
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromTag(ConventionalItemTags.GOLD_INGOTS))
                .offerTo(exporter);
        for (DyeColor c : DyeColor.values()) {
            generateBed(exporter, PlayerCollarsMod.DOG_BED_ITEMS[c.ordinal()], DatagenEntrypoint.WOOLS[c.ordinal()]);
            generateBowl(exporter, PlayerCollarsMod.DOG_BOWL_ITEMS[c.ordinal()], DatagenEntrypoint.TERRACOTTAS[c.ordinal()]);
        }
        for (int i = 0; i < PlayerCollarsMod.PAWS_DYE_COLORS.length; i++) {
            generatePaws(exporter, PlayerCollarsMod.PAWS_ITEMS[i], DatagenEntrypoint.WOOLS[PlayerCollarsMod.PAWS_DYE_COLORS[i].ordinal()]);
            generateFootPaws(exporter, PlayerCollarsMod.FOOT_PAWS_ITEMS[i], DatagenEntrypoint.WOOLS[PlayerCollarsMod.PAWS_DYE_COLORS[i].ordinal()]);
        }
    }

    private void generateBed(RecipeExporter exporter, BedItem output, Item input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).pattern("w w").pattern("www")
                .input('w', input)
                .criterion(FabricRecipeProvider.hasItem(input),
                        FabricRecipeProvider.conditionsFromItem(input))
                .group("dog_bed")
                .offerTo(exporter);
    }

    private void generatePaws(RecipeExporter exporter, PawsItem output, Item input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, output).pattern(" w ").pattern("wlw").pattern(" w ")
                .input('w', input)
                .input('l', Items.LEATHER)
                .criterion(FabricRecipeProvider.hasItem(input),
                        FabricRecipeProvider.conditionsFromItem(input))
                .group("paws")
                .offerTo(exporter);
    }

    private void generateFootPaws(RecipeExporter exporter, FootPawsItem output, Item input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, output).pattern(" w ").pattern(" w ").pattern("wlw")
                .input('w', input)
                .input('l', Items.LEATHER)
                .criterion(FabricRecipeProvider.hasItem(Items.LEATHER),
                        FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
                .group("foot_paws")
                .offerTo(exporter);
    }

    private void generateBowl(RecipeExporter exporter, Item output, Item input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).pattern("w w").pattern("www")
                .input('w', input)
                .criterion(FabricRecipeProvider.hasItem(input),
                        FabricRecipeProvider.conditionsFromItem(input))
                .group("dog_bowl")
                .offerTo(exporter);
    }
}
