package org.jlortiz.playercollars.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
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
    public static final IntProperty LEVEL = Properties.AGE_3;
    public static final BooleanProperty MILK = Properties.SNOWY;
    private static final VoxelShape SHAPE_BASE = VoxelShapes.union(
            Block.createCuboidShape(2.0, 0.0, 1.0, 14.0, 5.0, 2.0),
            Block.createCuboidShape(2.0, 0.0, 14.0, 14.0, 5.0, 15.0),
            Block.createCuboidShape(1.0, 0.0, 1.0, 2.0, 5.0, 15.0),
            Block.createCuboidShape(14.0, 0.0, 1.0, 15.0, 5.0, 15.0)
    );
    private static final VoxelShape[] SHAPE = {
            VoxelShapes.union(SHAPE_BASE, Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 1.0, 14.0)),
            VoxelShapes.union(SHAPE_BASE, Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 2.0, 14.0)),
            VoxelShapes.union(SHAPE_BASE, Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 4.0, 14.0)),
            VoxelShapes.union(SHAPE_BASE, Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 6.0, 14.0))
    };
    public final DyeColor color;

    public DogBowlBlock(DyeColor c, Settings settings) {
        super(settings);
        this.color = c;
        setDefaultState(this.getStateManager().getDefaultState().with(LEVEL, 0).with(MILK, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DogBowlBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos,
                                                BlockPos neighborPos) {
        return direction == Direction.DOWN &&
               !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        return hasTopRim(world, blockPos) || sideCoversSmallSquare(world, blockPos, Direction.UP);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int level = state.get(LEVEL);
        return (level < 0 || level > 3) ? SHAPE_BASE : SHAPE[level];
    }

    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isEmpty())
            return ActionResult.PASS;
        if (!(world.getBlockEntity(pos) instanceof DogBowlBlockEntity be))
            return ActionResult.CONSUME_PARTIAL;

        if (stack.isOf(Items.MILK_BUCKET) && be.getCount() == 0) {
            be.insert(stack);
            state = state.with(MILK, true);
            world.setBlockState(pos, state, 2);
            if (!player.isCreative()) player.setStackInHand(hand, new ItemStack(Items.BUCKET));
            player.playSound(SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return ActionResult.SUCCESS;
        }

        if (stack.getItem().getFoodComponent() == null)
            return ActionResult.PASS;

        int decr = be.insert(stack);
        if (decr > 0) {
            stack.decrement(decr);

            state = state.with(LEVEL, Math.min((be.getCount() + 20) / 21, 3));
            world.setBlockState(pos, state, 2);
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME_PARTIAL;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.getBlockEntity(pos) instanceof DogBowlBlockEntity be) be.drop();
        super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var item = player.getStackInHand(hand);
        var result = onUseWithItem(item, state, world, pos, player, hand, hit);

        if (result != ActionResult.PASS)
            return result;

        if (!(world.getBlockEntity(pos) instanceof DogBowlBlockEntity be))
            return ActionResult.PASS;

        ItemStack is = be.take();
        if (is.isEmpty()) return ActionResult.PASS;
        if (is.isOf(Items.MILK_BUCKET)) {
            state = state.with(MILK, false);
            world.setBlockState(pos, state, 2);

            if (!world.isClient())
                player.clearStatusEffects();

            player.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);
            return ActionResult.SUCCESS;
        }

        state = state.with(LEVEL, Math.min((be.getCount() + 20) / 21, 3));
        world.setBlockState(pos, state, 2);

        FoodComponent food = is.getItem().getFoodComponent();
        if (food != null && player.canConsume(is.getItem().getFoodComponent().isAlwaysEdible())) {
            player.eatFood(world, is);
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
        public void readNbt(NbtCompound nbt) {
            super.readNbt(nbt);
            this.inBowl = Optional.of(nbt.getCompound("item"))
                    .map(ItemStack::fromNbt)
                    .orElse(ItemStack.EMPTY);
        }

        @Override
        protected void writeNbt(NbtCompound nbt) {
            super.writeNbt(nbt);
            if (!this.inBowl.isEmpty()) {
                nbt.put("item", this.inBowl.writeNbt(new NbtCompound()));
            }
        }

        protected int getCount() {
            return this.inBowl.getCount();
        }

        protected int insert(ItemStack is) {
            if (this.inBowl.isEmpty()) {
                this.inBowl = is.copy();
                markDirty();
                return is.getCount();
            }
            if (is.isOf(this.inBowl.getItem())) {
                int count = Math.min(is.getCount(), this.inBowl.getMaxCount() - this.inBowl.getCount());
                this.inBowl.increment(count);
                markDirty();
                return count;
            }
            return 0;
        }

        protected ItemStack take() {
            if (this.inBowl.isEmpty()) return ItemStack.EMPTY;
            ItemStack is = this.inBowl.copyWithCount(1);
            this.inBowl.decrement(1);
            markDirty();
            return is;
        }

        protected void drop() {
            if (this.inBowl.isEmpty() || this.inBowl.isOf(Items.MILK_BUCKET) || this.world == null) return;
            this.world.spawnEntity(new ItemEntity(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.inBowl));
            this.inBowl = ItemStack.EMPTY;
        }
    }
}
