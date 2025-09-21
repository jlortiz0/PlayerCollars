package org.jlortiz.playercollars.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.BedBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.data.*;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.tint.ConstantTintSource;
import net.minecraft.client.render.item.tint.DyeTintSource;
import net.minecraft.client.render.item.tint.MapColorTintSource;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.block.DogBedBlock;
import org.jlortiz.playercollars.block.DogBowlBlock;
import org.jlortiz.playercollars.item.FootPawsItem;

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
                                        .put(VariantSettings.Y, VariantSettings.Rotation.values()[dir.getHorizontalQuarterTurns()])
                                        .put(VariantSettings.X, VariantSettings.Rotation.R0)
                                        .put(VariantSettings.MODEL, Identifier.of(PlayerCollarsMod.MOD_ID, "block/" + bed.getColor().getName() + "_dog_bed"));
                            }
                    )));
            if (bed.getColor() != DyeColor.WHITE)
                baseModel.upload(bed, TextureMap.particle(DatagenEntrypoint.WOOLS[bed.getColor().ordinal()].getBlock()), blockStateModelGenerator.modelCollector);
        }

        Model[] bowlModels = new Model[4];
        for (int i = 0; i < 4; i++) {
            bowlModels[i] = new Model(Optional.of(Identifier.of(PlayerCollarsMod.MOD_ID, "block/red_dog_bowl_"+i)), Optional.empty(), TextureKey.PARTICLE);
        }
        for (DogBowlBlock bowl : PlayerCollarsMod.DOG_BOWLS) {
            blockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(bowl)
                    .coordinate(BlockStateVariantMap.create(DogBowlBlock.LEVEL).register((level) -> BlockStateVariant.create()
                            .put(VariantSettings.MODEL, Identifier.of(PlayerCollarsMod.MOD_ID, "block/" + bowl.color.getName() + "_dog_bowl_" + level)))));
            if (bowl.color != DyeColor.RED)
                for (int i = 0; i < 4; i++)
                    bowlModels[i].upload(Identifier.of(PlayerCollarsMod.MOD_ID, "block/" + bowl.color.getName() + "_dog_bowl_"+i),
                            TextureMap.particle(DatagenEntrypoint.TERRACOTTAS[bowl.color.ordinal()].getBlock()), blockStateModelGenerator.modelCollector);
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        Item whiteBed = PlayerCollarsMod.DOG_BED_ITEMS[DyeColor.WHITE.ordinal()];
        Identifier bedItemModel = itemModelGenerator.uploadWithTextureSource(whiteBed, whiteBed, Models.GENERATED);
        for (int i = 0; i < DyeColor.values().length; i++) {
            ItemModel.Unbaked m = ItemModels.tinted(bedItemModel, new ConstantTintSource(DyeColor.values()[i].getFireworkColor()));
            itemModelGenerator.output.accept(PlayerCollarsMod.DOG_BED_ITEMS[i], m);
        }

        Identifier pawsModel = Identifier.of(PlayerCollarsMod.MOD_ID, "item/paws");
        for (FootPawsItem i : PlayerCollarsMod.PAWS_ITEMS) {
            itemModelGenerator.output.accept(i, ItemModels.tinted(pawsModel, new DyeTintSource(i.color), new MapColorTintSource(i.beansColor)));
        }
        pawsModel = Identifier.of(PlayerCollarsMod.MOD_ID, "item/foot_paws");
        for (FootPawsItem i : PlayerCollarsMod.FOOT_PAWS_ITEMS) {
            itemModelGenerator.output.accept(i, ItemModels.tinted(pawsModel, new DyeTintSource(i.color), new MapColorTintSource(i.beansColor)));
        }

        itemModelGenerator.register(PlayerCollarsMod.DEED_OF_OWNERSHIP, Models.GENERATED);
        itemModelGenerator.register(PlayerCollarsMod.DEED_OF_OWNERSHIP_STAMPED, Models.GENERATED);
        itemModelGenerator.register(PlayerCollarsMod.SPATULA_ITEM, Models.HANDHELD);
        itemModelGenerator.register(PlayerCollarsMod.COLLAR_LOCKER_ITEM, Models.GENERATED);

        for (DyeColor c : DyeColor.values()) {
            Model baseBowl = new Model(Optional.of(Identifier.of(PlayerCollarsMod.MOD_ID, "block/" + c.getName() + "_dog_bowl_3")), Optional.empty());
            itemModelGenerator.register(PlayerCollarsMod.DOG_BOWL_ITEMS[c.ordinal()], baseBowl);
        }
    }
}
