package org.jlortiz.playercollars.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.BedBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.data.client.*;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.DogBedBlock;

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
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        Item whiteBed = PlayerCollarsMod.DOG_BED_ITEMS[DyeColor.WHITE.ordinal()];
        for (Item i : PlayerCollarsMod.DOG_BED_ITEMS)
            itemModelGenerator.register(i, whiteBed, Models.GENERATED);
    }
}
