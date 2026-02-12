package org.jlortiz.playercollars.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.Optional;

public class DogBowlBlock extends Block implements BlockEntityProvider {
    private static final VoxelShape SHAPE_BASE = VoxelShapes.union(
            Block.createCuboidShape(2.0, 0.0, 1.0, 14.0, 5.0, 2.0),
            Block.createCuboidShape(2.0, 0.0, 14.0, 14.0, 5.0, 15.0),
            Block.createCuboidShape(1.0, 0.0, 1.0, 2.0, 5.0, 15.0),
            Block.createCuboidShape(14.0, 0.0, 1.0, 15.0, 5.0, 15.0)
            );
    private static final VoxelShape[] SHAPE = new VoxelShape[] {
            VoxelShapes.union(SHAPE_BASE, Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 1.0, 14.0)),
            VoxelShapes.union(SHAPE_BASE, Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 2.0, 14.0)),
            VoxelShapes.union(SHAPE_BASE, Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 4.0, 14.0)),
            VoxelShapes.union(SHAPE_BASE, Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 6.0, 14.0))
    };
    public static final IntProperty LEVEL = Properties.AGE_3;
    public static final BooleanProperty MILK = Properties.SNOWY;
    public final DyeColor color;

    public DogBowlBlock(DyeColor c, Settings settings) {
        super(settings);
        color = c;
        setDefaultState(this.getStateManager().getDefaultState().with(LEVEL, 0).with(MILK, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DogBowlBlockEntity(pos, state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        return hasTopRim(world, blockPos) || sideCoversSmallSquare(world, blockPos, Direction.UP);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int level = state.get(LEVEL);
        return (level < 0 || level > 3) ? SHAPE_BASE : SHAPE[level];
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(world.getBlockEntity(pos) instanceof DogBowlBlockEntity be)) return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (stack.isOf(Items.MILK_BUCKET) && be.getCount() == 0) {
            be.insert(stack);
            state = state.with(MILK, true);
            world.setBlockState(pos, state, 2);
            if (!player.isCreative()) player.setStackInHand(hand, new ItemStack(Items.BUCKET));
            player.playSound(SoundEvents.ITEM_BUCKET_EMPTY);
            return ItemActionResult.SUCCESS;
        }

        if (stack.get(DataComponentTypes.FOOD) == null) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        int decr = be.insert(stack);
        if (decr > 0) {
            stack.decrement(decr);
            state = state.with(LEVEL, Math.min((be.getCount() + 20) / 21, 3));
            world.setBlockState(pos, state, 2);
            return ItemActionResult.SUCCESS;
        }
        return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.getBlockEntity(pos) instanceof DogBowlBlockEntity be) be.drop();
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof DogBowlBlockEntity be)) return ActionResult.PASS;

        ItemStack is = be.take();
        if (is.isEmpty()) return ActionResult.PASS;
        if (is.isOf(Items.MILK_BUCKET)) {
            state = state.with(MILK, false);
            world.setBlockState(pos, state, 2);
            if (!world.isClient()) player.clearStatusEffects();
            player.playSound(SoundEvents.ENTITY_GENERIC_DRINK);
            return ActionResult.SUCCESS;
        }

        state = state.with(LEVEL, Math.min((be.getCount() + 20) / 21, 3));
        world.setBlockState(pos, state, 2);

        FoodComponent food = is.get(DataComponentTypes.FOOD);
        if (food != null && player.canConsume(food.canAlwaysEat())) {
            player.eatFood(world, is, food);
            return ActionResult.SUCCESS;
        } else if (!player.giveItemStack(is)) {
            player.dropItem(is, true);
        }
        return ActionResult.CONSUME;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, MILK);
    }

    public static class DogBowlBlockEntity extends BlockEntity {
        private ItemStack inBowl = ItemStack.EMPTY;

        public DogBowlBlockEntity(BlockPos pos, BlockState state) {
            super(PlayerCollarsMod.DOG_BOWL_BLOCK_ENTITY, pos, state);
        }

        @Override
        protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
            super.readNbt(nbt, registryLookup);
            inBowl = Optional.of(nbt.getCompound("item"))
                    .flatMap((x) -> ItemStack.fromNbt(registryLookup, x))
                    .orElse(ItemStack.EMPTY);
        }

        @Override
        protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
            super.writeNbt(nbt, registryLookup);
            if (!inBowl.isEmpty())
                nbt.put("item", inBowl.encode(registryLookup));
        }

        protected int getCount() {
            return inBowl.getCount();
        }

        protected int insert(ItemStack is) {
            if (inBowl.isEmpty()) {
                inBowl = is.copy();
                markDirty();
                return is.getCount();
            }
            if (is.isOf(inBowl.getItem())) {
                int count = Math.min(is.getCount(), inBowl.getMaxCount() - inBowl.getCount());
                inBowl.increment(count);
                markDirty();
                return count;
            }
            return 0;
        }

        protected ItemStack take() {
            if (inBowl.isEmpty()) return ItemStack.EMPTY;
            ItemStack is = inBowl.copyWithCount(1);
            inBowl.decrement(1);
            markDirty();
            return is;
        }

        protected void drop() {
            if (inBowl.isEmpty() || inBowl.isOf(Items.MILK_BUCKET) || world == null) return;
            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), inBowl));
            inBowl = ItemStack.EMPTY;
        }
    }
}
