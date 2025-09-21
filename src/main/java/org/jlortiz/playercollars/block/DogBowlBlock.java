package org.jlortiz.playercollars.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
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
    public final DyeColor color;

    public DogBowlBlock(DyeColor c, Settings settings) {
        super(settings);
        color = c;
    }

    public static RegistryKey<Block> getRegistryKey(DyeColor c) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(PlayerCollarsMod.MOD_ID, c.getName() + "_dog_bowl"));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DogBowlBlockEntity(pos, state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() :
                super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
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

    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isEmpty() || stack.get(DataComponentTypes.FOOD) == null) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        if (!(world.getBlockEntity(pos) instanceof DogBowlBlockEntity be)) return ActionResult.FAIL;
        int decr = be.insert(stack);
        if (decr > 0) {
            stack.decrement(decr);
            state = state.with(LEVEL, Math.min((be.getCount() + 20) / 21, 3));
            world.setBlockState(pos, state, 0);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
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
        state = state.with(LEVEL, Math.min((be.getCount() + 20) / 21, 3));
        world.setBlockState(pos, state, 0);

        FoodComponent food = is.get(DataComponentTypes.FOOD);
        if (food != null && player.canConsume(food.canAlwaysEat())) {
            ConsumableComponent consume = is.get(DataComponentTypes.CONSUMABLE);
            if (consume == null) {
                player.getHungerManager().eat(food);
            } else {
                consume.finishConsumption(world, player, is);
            }
            return ActionResult.SUCCESS;
        } else if (!player.giveItemStack(is)) {
            player.dropItem(is, true);
        }
        return ActionResult.CONSUME;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
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
                nbt.put("item", inBowl.toNbt(registryLookup));
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
            if (inBowl.isEmpty() || world == null) return;
            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), inBowl));
            inBowl = ItemStack.EMPTY;
        }
    }
}
