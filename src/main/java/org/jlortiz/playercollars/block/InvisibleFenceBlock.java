package org.jlortiz.playercollars.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import org.jlortiz.playercollars.PlayerCollarsMod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

public class InvisibleFenceBlock extends FenceBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public InvisibleFenceBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(this.stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        state = super.updateShape(state, direction, neighborState, world, pos, neighborPos);
        if (neighborState.is(this) && neighborState.getValue(POWERED) != state.getValue(POWERED))
            state = state.setValue(POWERED, neighborState.getValue(POWERED));
        return state;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        boolean power = false;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockState n = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(d));
            if (n.is(this) && n.getValue(POWERED)) { power = true; break; }
        }
        return power ? state.setValue(POWERED, true) : state;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext eCtx) {
            if (state.getValue(POWERED) && eCtx.getEntity() instanceof LivingEntity living) {
                return CuriosApi.getCuriosInventory(living)
                        .map(h -> h.findCurios(s -> s.is(PlayerCollarsMod.COLLAR_TAG)))
                        .filter(list -> !list.isEmpty())
                        .map(list -> super.getCollisionShape(state, world, pos, context))
                        .orElse(Shapes.empty());
            }
            if (eCtx.getEntity() == null) return super.getCollisionShape(state, world, pos, context);
        }
        return Shapes.empty();
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        super.animateTick(state, world, pos, random);
        if (state.getValue(POWERED) && random.nextFloat() < 0.25f) {
            world.addParticle(DustParticleOptions.REDSTONE,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + random.nextFloat() * 0.5,
                    pos.getZ() + random.nextFloat() * 0.5,
                    0, 0, 0);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        var result = super.use(state, world, pos, player, hand, hit);
        if (result != InteractionResult.PASS) return result;

        if (world.isClientSide()) return InteractionResult.SUCCESS;

        boolean hasCollar = CuriosApi.getCuriosInventory(player)
                .map(h -> !h.findCurios(s -> s.is(PlayerCollarsMod.COLLAR_TAG)).isEmpty())
                .orElse(false);
        if (hasCollar) {
            player.displayClientMessage(Component.translatable("block.playercollars.invisible_fence.toggle_fail")
                    .withStyle(net.minecraft.ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        state = state.setValue(POWERED, !state.getValue(POWERED));
        world.setBlock(pos, state, 7);
        player.displayClientMessage(Component.translatable(
                state.getValue(POWERED) ? "block.playercollars.invisible_fence.toggle_on"
                        : "block.playercollars.invisible_fence.toggle_off")
                .withStyle(net.minecraft.ChatFormatting.GREEN), true);
        return InteractionResult.SUCCESS;
    }
}
