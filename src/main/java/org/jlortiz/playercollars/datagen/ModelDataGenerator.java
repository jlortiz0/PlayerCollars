package org.jlortiz.playercollars.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.data.client.*;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.block.DogBedBlock;
import org.jlortiz.playercollars.block.DogBowlBlock;

import java.util.Optional;

public class ModelDataGenerator extends FabricModelProvider {
    public ModelDataGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        Model baseModel = new Model(Optional.of(Identifier.of(PlayerCollarsMod.MOD_ID, "block/white_dog_bed")), Optional.empty(), TextureKey.PARTICLE);
        for (DogBedBlock bed : PlayerCollarsMod.DOG_BEDS) {
            blockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(bed)
                    .coordinate(BlockStateVariantMap.create(BedBlock.FACING, BedBlock.PART).register(
                            (dir, part) -> {
                                if (part == BedPart.HEAD)
                                    dir = dir.getOpposite();
                                return BlockStateVariant.create()
                                        .put(VariantSettings.Y, VariantSettings.Rotation.values()[dir.getHorizontal()])
                                        .put(VariantSettings.X, VariantSettings.Rotation.R0)
                                        .put(VariantSettings.MODEL, Identifier.of(PlayerCollarsMod.MOD_ID, "block/" + bed.getColor().getName() + "_dog_bed"));
                            }
                    )));
            if (bed.getColor() != DyeColor.WHITE)
                baseModel.upload(bed, TextureMap.particle(DatagenEntrypoint.WOOLS[bed.getColor().ordinal()].getBlock()), blockStateModelGenerator.modelCollector);
        }

        Model[] bowlModels = new Model[5];
        for (int i = 0; i < 4; i++) {
            bowlModels[i] = new Model(Optional.of(Identifier.of(PlayerCollarsMod.MOD_ID, "block/red_dog_bowl_"+i)), Optional.empty(), TextureKey.PARTICLE);
        }
        bowlModels[4] = new Model(Optional.of(Identifier.of(PlayerCollarsMod.MOD_ID, "block/red_dog_bowl_milk")), Optional.empty(), TextureKey.PARTICLE);
        for (DogBowlBlock bowl : PlayerCollarsMod.DOG_BOWLS) {
            blockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(bowl)
                    .coordinate(BlockStateVariantMap.create(DogBowlBlock.MILK, DogBowlBlock.LEVEL).register((milk, level) -> BlockStateVariant.create()
                            .put(VariantSettings.MODEL, Identifier.of(PlayerCollarsMod.MOD_ID, "block/" + bowl.color.getName() + "_dog_bowl_" + (milk ? "milk" :level))))));
            if (bowl.color != DyeColor.RED) {
                for (int i = 0; i < 4; i++)
                    bowlModels[i].upload(Identifier.of(PlayerCollarsMod.MOD_ID, "block/" + bowl.color.getName() + "_dog_bowl_"+i),
                            TextureMap.particle(DatagenEntrypoint.TERRACOTTAS[bowl.color.ordinal()].getBlock()), blockStateModelGenerator.modelCollector);
                bowlModels[4].upload(Identifier.of(PlayerCollarsMod.MOD_ID, "block/" + bowl.color.getName() + "_dog_bowl_milk"),
                        TextureMap.particle(DatagenEntrypoint.TERRACOTTAS[bowl.color.ordinal()].getBlock()), blockStateModelGenerator.modelCollector);
            }
        }

        TextureMap glassTexture = TextureMap.all(Blocks.GLASS);
        Identifier glassPost = Models.FENCE_POST.upload(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK, glassTexture, blockStateModelGenerator.modelCollector);
        Identifier glassSide = Models.FENCE_SIDE.upload(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK, glassTexture, blockStateModelGenerator.modelCollector);
        blockStateModelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createFenceBlockState(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK, glassPost, glassSide));
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        Item whiteBed = PlayerCollarsMod.DOG_BED_ITEMS[DyeColor.WHITE.ordinal()];
        for (Item i : PlayerCollarsMod.DOG_BED_ITEMS)
            itemModelGenerator.register(i, whiteBed, Models.GENERATED);

        Model basePaws = new Model(Optional.of(Identifier.of(PlayerCollarsMod.MOD_ID, "item/white_paws")), Optional.empty());
        for (int i = 1; i < PlayerCollarsMod.PAWS_ITEMS.length; i++) {
            itemModelGenerator.register(PlayerCollarsMod.PAWS_ITEMS[i], basePaws);
        }
        basePaws = new Model(Optional.of(Identifier.of(PlayerCollarsMod.MOD_ID, "item/white_foot_paws")), Optional.empty());
        for (int i = 1; i < PlayerCollarsMod.FOOT_PAWS_ITEMS.length; i++) {
            itemModelGenerator.register(PlayerCollarsMod.FOOT_PAWS_ITEMS[i], basePaws);
        }

        itemModelGenerator.register(PlayerCollarsMod.DEED_OF_OWNERSHIP, Models.GENERATED);
        itemModelGenerator.register(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED, Models.GENERATED);
        itemModelGenerator.register(PlayerCollarsMod.SPATULA_ITEM, Models.HANDHELD);
        itemModelGenerator.register(PlayerCollarsMod.COLLAR_LOCKER_ITEM, Models.GENERATED);

        for (DyeColor c : DyeColor.values()) {
            Model baseBowl = new Model(Optional.of(Identifier.of(PlayerCollarsMod.MOD_ID, "block/" + c.getName() + "_dog_bowl_3")), Optional.empty());
            itemModelGenerator.register(PlayerCollarsMod.DOG_BOWL_ITEMS[c.ordinal()], baseBowl);
        }

        Models.FENCE_INVENTORY.upload(ModelIds.getItemModelId(PlayerCollarsMod.INVISIBLE_FENCE_BLOCK_ITEM),
                TextureMap.all(Blocks.GLASS), itemModelGenerator.writer);
    }
}
