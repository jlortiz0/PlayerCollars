package org.jlortiz.playercollars.block;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.List;
import java.util.Optional;

public class InvisibleFenceBlock extends FenceBlock {
    public static final RegistryKey<Block> REGISTRY_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(PlayerCollarsMod.MOD_ID, "invisible_fence"));
    public static final RegistryKey<Item> ITEM_REGISTRY_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "invisible_fence"));
    public static final BooleanProperty POWERED = Properties.POWERED;

    public InvisibleFenceBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(this.getStateManager().getDefaultState().with(POWERED, false).with(WATERLOGGED, false));
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        state = super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        if (neighborState.isOf(this) && neighborState.get(POWERED) != state.get(POWERED)) {
            state = state.with(POWERED, neighborState.get(POWERED));
        }
        return state;
    }

    @Override
    protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        BlockState neighbor = ctx.getWorld().getBlockState(ctx.getBlockPos().north());
        boolean shouldPower = neighbor.isOf(this) && neighbor.get(POWERED);
        if (!shouldPower) {
            neighbor =  ctx.getWorld().getBlockState(ctx.getBlockPos().east());
            shouldPower = neighbor.isOf(this) && neighbor.get(POWERED);
        }
        if (!shouldPower) {
            neighbor =  ctx.getWorld().getBlockState(ctx.getBlockPos().south());
            shouldPower = neighbor.isOf(this) && neighbor.get(POWERED);
        }
        if (!shouldPower) {
            neighbor =  ctx.getWorld().getBlockState(ctx.getBlockPos().west());
            shouldPower = neighbor.isOf(this) && neighbor.get(POWERED);
        }
        if (shouldPower) state = state.with(POWERED, true);
        return state;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext e) {
            if (state.get(POWERED) && e.getEntity() instanceof LivingEntity livingEntity) {
                Optional<TrinketComponent> optComponent = TrinketsApi.getTrinketComponent(livingEntity);
                if (optComponent.isEmpty()) return VoxelShapes.empty();
                TrinketComponent component = optComponent.get();

                return component.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)).isEmpty() ?
                        VoxelShapes.empty() : super.getCollisionShape(state, world, pos, context);
            }
            // Vertical collision is cached using EntityShapeContext.ABSENT.
            // This will be re-checked if something actually lands on the fence, so this is safe for players.
            // It can cause unusual behaviour if something tries to pathfind through it, so that is left disabled.
            if (e.getEntity() == null) return super.getCollisionShape(state, world, pos, context);
        }
        return VoxelShapes.empty();
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (state.get(POWERED) && random.nextFloat() < 0.25)
            ParticleUtil.spawnParticlesAround(world, pos, 1, 0.5, 0.5, true, DustParticleEffect.DEFAULT);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.PASS;
        if (!TrinketsApi.getTrinketComponent(player).map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)))
                .map(List::isEmpty).orElse(true)) {
            player.sendMessage(Text.translatable("block.playercollars.invisible_fence.toggle_fail").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        state = state.with(POWERED, !state.get(POWERED));
        world.setBlockState(pos, state, 7);
        player.sendMessage(Text.translatable(
                state.get(POWERED) ? "block.playercollars.invisible_fence.toggle_on"
                        : "block.playercollars.invisible_fence.toggle_off")
                .formatted(Formatting.GREEN), true);
        return ActionResult.SUCCESS;
    }
}