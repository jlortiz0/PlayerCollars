package org.jlortiz.playercollars.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.PawsSelectScreen;
import top.theillusivec4.curios.api.CuriosApi;

public class PawSetupItem extends Item {
    public PawSetupItem() { super(new Properties().stacksTo(1)); }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) { return stack; }
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) { return true; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack is = user.getItemInHand(hand);
        if (!user.isCrouching() || !world.isClientSide) return InteractionResultHolder.pass(is);
        return switch (interactLivingEntity(is, user, user, hand)) {
            case SUCCESS -> InteractionResultHolder.success(is);
            case CONSUME, CONSUME_PARTIAL -> InteractionResultHolder.consume(is);
            case PASS -> InteractionResultHolder.pass(is);
            case FAIL -> InteractionResultHolder.fail(is);
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof Player player) || !user.level().isClientSide) return InteractionResult.PASS;
        var collarResults = CuriosApi.getCuriosInventory(player)
                .map(h -> h.findCurios(s -> s.is(PlayerCollarsMod.COLLAR_TAG))).orElse(java.util.List.of());
        if (PlayerCollarsMod.filterStacksByOwner(collarResults, user.getUUID(), player.getUUID()) == null) {
            user.displayClientMessage(Component.translatable("item.playercollars.paw_configurator.no_set_non_owner")
                    .withStyle(net.minecraft.ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        Minecraft.getInstance().setScreen(new PawsSelectScreen(player));
        return InteractionResult.SUCCESS;
    }
}
