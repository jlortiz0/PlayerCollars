package org.jlortiz.playercollars.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class DogBedBlock extends BedBlock {
    private static final VoxelShape SHAPE = createCuboidShape(0, 0, 0, 16, 6, 16);

    public DogBedBlock(DyeColor color, RegistryKey<Block> key) {
        super(color, AbstractBlock.Settings.create()
                .sounds(BlockSoundGroup.WOOL).strength(0.2F).nonOpaque().burnable()
                .pistonBehavior(PistonBehavior.DESTROY).registryKey(key));
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    public static RegistryKey<Block> getRegistryKey(DyeColor c) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(PlayerCollarsMod.MOD_ID, c.getId() + "_dog_bed"));
    }
}
