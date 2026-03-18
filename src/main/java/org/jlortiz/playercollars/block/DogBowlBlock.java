package org.jlortiz.playercollars.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.Optional;

public class DogBowlBlock extends BaseEntityBlock {
    public static final IntegerProperty LEVEL = net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_3;
    public static final BooleanProperty MILK = BlockStateProperties.SNOWY;

    private static final VoxelShape SHAPE_BASE = Shapes.or(
            box(2, 0, 1, 14, 5, 2), box(2, 0, 14, 14, 5, 15),
            box(1, 0, 1, 2, 5, 15), box(14, 0, 1, 15, 5, 15));
    private static final VoxelShape[] SHAPE = {
            Shapes.or(SHAPE_BASE, box(2, 0, 2, 14, 1, 14)),
            Shapes.or(SHAPE_BASE, box(2, 0, 2, 14, 2, 14)),
            Shapes.or(SHAPE_BASE, box(2, 0, 2, 14, 4, 14)),
            Shapes.or(SHAPE_BASE, box(2, 0, 2, 14, 6, 14))
    };

    public final DyeColor color;

    public DogBowlBlock(DyeColor c, Properties settings) {
        super(settings);
        this.color = c;
        registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0).setValue(MILK, false));
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DogBowlBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(world, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos below = pos.below();
        return canSupportRigidBlock(world, below) || canSupportCenter(world, below, Direction.UP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        int level = state.getValue(LEVEL);
        return (level < 0 || level > 3) ? SHAPE_BASE : SHAPE[level];
    }

    private InteractionResult onUseWithItem(ItemStack stack, BlockState state, Level world, BlockPos pos,
                                            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return InteractionResult.PASS;
        if (!(world.getBlockEntity(pos) instanceof DogBowlBlockEntity be)) return InteractionResult.CONSUME;

        if (stack.is(Items.MILK_BUCKET) && be.getCount() == 0) {
            be.insert(stack, false);
            world.setBlock(pos, state.setValue(MILK, true), 2);
            if (!player.isCreative()) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            world.playSound(null, pos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem().getFoodProperties(stack, player) == null) return InteractionResult.PASS;

        int decr = be.insert(stack, player.isCrouching());
        if (decr > 0) {
            if (!player.isCreative()) stack.shrink(decr);
            world.setBlock(pos, state.setValue(LEVEL, Math.min((be.getCount() + 20) / 21, 3)), 2);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof DogBowlBlockEntity be) be.drop();
        super.destroy(world, pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        var item = player.getItemInHand(hand);
        var result = onUseWithItem(item, state, world, pos, player, hand, hit);
        if (result != InteractionResult.PASS) return result;

        if (!(world.getBlockEntity(pos) instanceof DogBowlBlockEntity be)) return InteractionResult.PASS;
        ItemStack is = be.take();
        if (is.isEmpty()) return InteractionResult.PASS;

        if (is.is(Items.MILK_BUCKET)) {
            world.setBlock(pos, state.setValue(MILK, false), 2);
            if (!world.isClientSide()) player.removeAllEffects();
            player.playSound(net.minecraft.sounds.SoundEvents.GENERIC_DRINK, 1, 1);
            return InteractionResult.SUCCESS;
        }

        world.setBlock(pos, state.setValue(LEVEL, Math.min((be.getCount() + 20) / 21, 3)), 2);
        FoodProperties food = is.getItem().getFoodProperties(is, player);
        if (food != null && player.canEat(food.canAlwaysEat())) {
            player.eat(world, is);
            return InteractionResult.SUCCESS;
        } else if (!player.getInventory().add(is)) {
            player.drop(is, true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, MILK);
    }

    public static class DogBowlBlockEntity extends BlockEntity {
        private ItemStack inBowl = ItemStack.EMPTY;

        public DogBowlBlockEntity(BlockPos pos, BlockState state) {
            super(PlayerCollarsMod.DOG_BOWL_BLOCK_ENTITY.get(), pos, state);
        }

        @Override
        public void load(CompoundTag nbt) {
            super.load(nbt);
            this.inBowl = Optional.of(nbt.getCompound("item"))
                    .map(ItemStack::of).orElse(ItemStack.EMPTY);
        }

        @Override
        protected void saveAdditional(CompoundTag nbt) {
            super.saveAdditional(nbt);
            if (!this.inBowl.isEmpty()) nbt.put("item", this.inBowl.save(new CompoundTag()));
        }

        protected int getCount() { return this.inBowl.getCount(); }

        protected int insert(ItemStack is, boolean moveAll) {
            if (this.inBowl.isEmpty()) {
                this.inBowl = is.copy();
                if (!moveAll) this.inBowl.setCount(1);
                setChanged();
                return moveAll ? is.getCount() : 1;
            }
            int remaining = this.inBowl.getMaxStackSize() - this.inBowl.getCount();
            if (is.is(this.inBowl.getItem()) && remaining > 0) {
                int amount = moveAll ? Math.min(is.getCount(), remaining) : 1;
                this.inBowl.grow(amount);
                setChanged();
                return amount;
            }
            return 0;
        }

        protected ItemStack take() {
            if (this.inBowl.isEmpty()) return ItemStack.EMPTY;
            ItemStack is = this.inBowl.copyWithCount(1);
            this.inBowl.shrink(1);
            setChanged();
            return is;
        }

        protected void drop() {
            if (this.inBowl.isEmpty() || this.inBowl.is(Items.MILK_BUCKET) || this.level == null) return;
            this.level.addFreshEntity(new ItemEntity(this.level, this.worldPosition.getX(),
                    this.worldPosition.getY(), this.worldPosition.getZ(), this.inBowl));
            this.inBowl = ItemStack.EMPTY;
        }
    }
}
