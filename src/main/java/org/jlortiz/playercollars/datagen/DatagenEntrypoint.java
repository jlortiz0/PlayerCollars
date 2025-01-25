package org.jlortiz.playercollars.datagen;

import dev.emi.trinkets.TrinketsMain;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.BedBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.DogBedBlock;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class DatagenEntrypoint implements DataGeneratorEntrypoint {
    public static final BlockItem[] WOOLS = new BlockItem[DyeColor.values().length];
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        for (DyeColor c : DyeColor.values())
            WOOLS[c.ordinal()] = (BlockItem) Registries.ITEM.get(Identifier.ofVanilla(c.getName() + "_wool"));

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(RecipeDataGenerator::new);
        pack.addProvider(ModelDataGenerator::new);
        pack.addProvider(LootTableGenerator::new);
        pack.addProvider(ItemTagGenerator::new);
        pack.addProvider(EnglishLangProvider::new);
    }

    private static class LootTableGenerator extends FabricBlockLootTableProvider {
        protected LootTableGenerator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generate() {
            for (int i = 0; i < PlayerCollarsMod.DOG_BEDS.length; i++)
                addDrop(PlayerCollarsMod.DOG_BEDS[i], dropsWithProperty(PlayerCollarsMod.DOG_BEDS[i], BedBlock.PART, BedPart.HEAD));
        }
    }

    private static class ItemTagGenerator extends FabricTagProvider<Item> {
        public ItemTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.ITEM, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            getOrCreateTagBuilder(ItemTags.DYEABLE).add(PlayerCollarsMod.COLLAR_ITEM).add(PlayerCollarsMod.CLICKER_ITEM);
            getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, Identifier.of(TrinketsMain.MOD_ID, "chest/necklace"))).add(PlayerCollarsMod.COLLAR_ITEM);
        }
    }

    private static class EnglishLangProvider extends FabricLanguageProvider {
        protected EnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
            for (DogBedBlock bed : PlayerCollarsMod.DOG_BEDS) {
                String pre = bed.getColor().getName();
                char[] buf = new char[pre.length() + " Human-Sized Dog Bed".length()];
                boolean newWord = true;
                for (int i = 0; i < pre.length(); i++) {
                    char c = pre.charAt(i);
                    if (c == '_') {
                        c = ' ';
                        newWord = true;
                    } else if (newWord) {
                        c = Character.toUpperCase(c);
                        newWord = false;
                    }
                    buf[i] = c;
                }
                " Human-Sized Dog Bed".getChars(0, buf.length - pre.length(), buf, pre.length());
                translationBuilder.add(bed,  String.valueOf(buf));
            }

            try {
                Path existingFilePath = dataOutput.getModContainer().findPath("assets/" + PlayerCollarsMod.MOD_ID + "/lang/en_us.existing.json").get();
                translationBuilder.add(existingFilePath);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add existing language file!", e);
            }
        }
    }
}
