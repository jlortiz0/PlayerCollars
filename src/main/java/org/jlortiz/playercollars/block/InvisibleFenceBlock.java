package org.jlortiz.playercollars.block;

import com.mojang.serialization.MapCodec;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class InvisibleFenceBlock extends HorizontalFacingBlock {
    public static final RegistryKey<Block> REGISTRY_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(PlayerCollarsMod.MOD_ID, "invisible_fence"));
    public static final RegistryKey<Item> ITEM_REGISTRY_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "invisible_fence"));
    private static final VoxelShape COLLISION_SHAPE_UPPER = Block.createCuboidShape(0, 0, 0, 16, 8, 16);
    private static final VoxelShape[] OUTLINE_SHAPES = new VoxelShape[] {
        VoxelShapes.union(
            Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
            Block.createCuboidShape(6.0, 1.0, 2.5, 10.0, 2.0, 13.5),
            Block.createCuboidShape(3.0, 1.0, 8.5, 13.0, 2.0, 10.0),
            Block.createCuboidShape(4.5, 1.0, 10.0, 11.5, 2.0, 11.5)
        ), VoxelShapes.union(
            Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
            Block.createCuboidShape(2.5, 1.0, 6.0, 13.5, 2.0, 10.0),
            Block.createCuboidShape(5.5, 1.0, 3.0, 7.0, 2.0, 13.0),
            Block.createCuboidShape(4.0, 1.0, 4.5, 5.5, 2.0, 11.5)
        ), VoxelShapes.union(
            Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
            Block.createCuboidShape(6.0, 1.0, 2.5, 10.0, 2.0, 13.5),
            Block.createCuboidShape(3.0, 1.0, 5.5, 13.0, 2.0, 7.0),
            Block.createCuboidShape(4.5, 1.0, 4.0, 11.5, 2.0, 5.5)
        ), VoxelShapes.union(
            Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
            Block.createCuboidShape(2.5, 1.0, 6.0, 13.5, 2.0, 10.0),
            Block.createCuboidShape(8.5, 1.0, 3.0, 10.0, 2.0, 13.0),
            Block.createCuboidShape(10.0, 1.0, 4.5, 11.5, 2.0, 11.5)
        )
    };
    private static final MapCodec<InvisibleFenceBlock> CODEC = createCodec(InvisibleFenceBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    public InvisibleFenceBlock(AbstractBlock.Settings settings) {
        super(settings.registryKey(REGISTRY_KEY));
        setDefaultState(this.getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected VoxelShape getInsideCollisionShape(BlockState state, World world, BlockPos pos) {
        return state.get(HALF) == DoubleBlockHalf.LOWER ? VoxelShapes.fullCube() : COLLISION_SHAPE_UPPER;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(HALF) == DoubleBlockHalf.LOWER ? OUTLINE_SHAPES[state.get(FACING).getHorizontalQuarterTurns()] : VoxelShapes.empty();
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof ClientPlayerEntity plr)) return;
        BlockPos redstoneQueryPos = pos;
        if (state.get(HALF) == DoubleBlockHalf.UPPER) redstoneQueryPos = redstoneQueryPos.down();
        if (world.getReceivedRedstonePower(redstoneQueryPos) == 0) return;

        AccessoriesCapability cap = AccessoriesCapability.get(plr);
        if (cap == null || cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.COLLAR_TAG)).isEmpty()) return;

        Vec3d pushBack = state.get(FACING).getDoubleVector();
        double dp = plr.getVelocity().dotProduct(pushBack.negate());
        if (dp > 0) {
            // This still stutters even though the multiplier is very small, but 1 is too small.
            // Not sure if there's a way to balance it.
            plr.move(MovementType.SELF, pushBack.multiply(1.0009765625 * dp));
        }
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        DoubleBlockHalf half = state.get(HALF);
        if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            return neighborState.getBlock() instanceof InvisibleFenceBlock && neighborState.get(HALF) != half ? neighborState.with(HALF, half) : Blocks.AIR.getDefaultState();
        } else {
            return half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        return state.get(HALF) == DoubleBlockHalf.LOWER ?
                blockState.isSideSolidFullSquare(world, blockPos, Direction.UP)
                        && world.getBlockState(pos.up()).isAir() : blockState.isOf(this);
    }
}