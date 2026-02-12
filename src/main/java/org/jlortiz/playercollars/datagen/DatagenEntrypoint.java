package org.jlortiz.playercollars.datagen;

import dev.emi.trinkets.TrinketsMain;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class DatagenEntrypoint implements DataGeneratorEntrypoint {
    public static final BlockItem[] WOOLS = new BlockItem[DyeColor.values().length];
    public static final BlockItem[] TERRACOTTAS = new BlockItem[DyeColor.values().length];
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        for (DyeColor c : DyeColor.values()) {
            WOOLS[c.ordinal()] = (BlockItem) Registries.ITEM.get(Identifier.ofVanilla(c.getName() + "_wool"));
            TERRACOTTAS[c.ordinal()] = (BlockItem) Registries.ITEM.get(Identifier.ofVanilla(c.getName() + "_terracotta"));
        }

        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(RecipeDataGenerator::new);
        pack.addProvider(ModelDataGenerator::new);
        pack.addProvider(LootTableGenerator::new);
        pack.addProvider(ItemTagGenerator::new);
        pack.addProvider(BlockTagGenerator::new);
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
            for (int i = 0; i < PlayerCollarsMod.DOG_BOWLS.length; i++)
                addDrop(PlayerCollarsMod.DOG_BOWLS[i], drops(PlayerCollarsMod.DOG_BOWL_ITEMS[i]));
            addDrop(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK, drops(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK));
        }
    }

    private static class ItemTagGenerator extends FabricTagProvider<Item> {
        public ItemTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.ITEM, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            getOrCreateTagBuilder(ItemTags.DYEABLE).add(PlayerCollarsMod.COLLAR_ITEM).add(PlayerCollarsMod.CLICKER_ITEM).add(PlayerCollarsMod.TAGLESS_COLLAR_ITEM);
            getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, Identifier.of(TrinketsMain.MOD_ID, "chest/necklace"))).add(PlayerCollarsMod.COLLAR_ITEM).add(PlayerCollarsMod.TAGLESS_COLLAR_ITEM);
            getOrCreateTagBuilder(PlayerCollarsMod.COLLAR_TAG).add(PlayerCollarsMod.COLLAR_ITEM).add(PlayerCollarsMod.TAGLESS_COLLAR_ITEM)
                    .addOptionalTag(TagKey.of(RegistryKeys.ITEM, Identifier.of("petworks", "collars")));
            getOrCreateTagBuilder(PlayerCollarsMod.PAWS_TAG).add(PlayerCollarsMod.PAWS_ITEMS);
            getOrCreateTagBuilder(PlayerCollarsMod.FOOT_PAWS_TAG).add(PlayerCollarsMod.FOOT_PAWS_ITEMS);
            getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, Identifier.of(TrinketsMain.MOD_ID, "hand/glove"))).addTag(PlayerCollarsMod.PAWS_TAG);
            getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, Identifier.of(TrinketsMain.MOD_ID, "feet/shoes"))).addTag(PlayerCollarsMod.FOOT_PAWS_TAG);
        }
    }

    private static class BlockTagGenerator extends FabricTagProvider<Block> {
        public BlockTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.BLOCK, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            getOrCreateTagBuilder(BlockTags.BEDS).add(PlayerCollarsMod.DOG_BEDS);
//            getOrCreateTagBuilder(PlayerCollarsMod.PAWS_ALLOW_INTERACT).addTag(BlockTags.BUTTONS)
//                    .add(Blocks.LEVER).addTag(BlockTags.CROPS).addTag(BlockTags.BEDS)
//                    .addTag(BlockTags.GEODE_INVALID_BLOCKS).addTag(BlockTags.CAULDRONS);
            getOrCreateTagBuilder(BlockTags.FENCES).add(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK);
        }
    }

    private static class EnglishLangProvider extends FabricLanguageProvider {
        protected EnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, registryLookup);
        }

        private static void generateColorNames(TranslationBuilder translationBuilder, String suffix, Function<Integer, DyeColor> getColor, Item... items) {
            for (int i = 0; i < items.length; i++) {
                String pre = getColor.apply(i).getName();
                char []buf = new char[pre.length() + suffix.length()];
                boolean newWord = true;
                for (int j = 0; j < pre.length(); j++) {
                    char c = pre.charAt(j);
                    if (c == '_') {
                        c = ' ';
                        newWord = true;
                    } else if (newWord) {
                        c = Character.toUpperCase(c);
                        newWord = false;
                    }
                    buf[j] = c;
                }
                suffix.getChars(0, buf.length - pre.length(), buf, pre.length());
                translationBuilder.add(items[i], String.valueOf(buf));
            }
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
            generateColorNames(translationBuilder, " Human-Sized Dog Bed", DyeColor::byId, PlayerCollarsMod.DOG_BED_ITEMS);
            generateColorNames(translationBuilder, " Paws", (i) -> PlayerCollarsMod.PAWS_DYE_COLORS[i], PlayerCollarsMod.PAWS_ITEMS);
            generateColorNames(translationBuilder, " Foot Paws", (i) -> PlayerCollarsMod.PAWS_DYE_COLORS[i], PlayerCollarsMod.FOOT_PAWS_ITEMS);
            generateColorNames(translationBuilder, " Dog Bowl", DyeColor::byId, PlayerCollarsMod.DOG_BOWL_ITEMS);

            try {
                Path existingFilePath = dataOutput.getModContainer().findPath("assets/" + PlayerCollarsMod.MOD_ID + "/lang/en_us.existing.json").get();
                translationBuilder.add(existingFilePath);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add existing language file!", e);
            }
        }
    }
}
