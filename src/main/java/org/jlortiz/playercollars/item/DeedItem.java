package org.jlortiz.playercollars.item;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.client.screen.DeedItemScreen;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.List;

public class DeedItem extends Item {
    public DeedItem() { super(new Item.Properties().stacksTo(1)); }

    @OnlyIn(Dist.CLIENT)
    private static void openTheScreen(ItemStack is, Player plr) {
        Minecraft.getInstance().setScreen(new DeedItemScreen(is, plr));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack is = player.getItemInHand(hand);
        if (world.isClientSide) {
            OwnerComponent owner = NbtUtil.getDeedOwner(is);
            if (owner != null && owner.owned().isEmpty()) {
                if (owner.uuid().equals(player.getUUID())) {
                    player.displayClientMessage(Component.translatable("item.playercollars.deed_of_ownership.no_self_own"), true);
                    return InteractionResultHolder.pass(is);
                }
                openTheScreen(is, player);
                return InteractionResultHolder.success(is);
            }
        } else if (NbtUtil.getDeedOwner(is) == null) {
            NbtUtil.setDeedOwner(is, new OwnerComponent(player.getUUID(), player.getName().getString()));
            player.displayClientMessage(Component.translatable("item.playercollars.deed_of_ownership.filled_out"), true);
            return InteractionResultHolder.consume(is);
        }
        return InteractionResultHolder.pass(is);
    }

    @Override
    public Component getName(ItemStack stack) {
        OwnerComponent owner = NbtUtil.getDeedOwner(stack);
        return owner != null ? Component.translatable(getDescriptionId(stack) + ".filled") : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        OwnerComponent owner = NbtUtil.getDeedOwner(stack);
        if (owner != null)
            tooltip.add(Component.translatable("item.playercollars.collar.owner", owner.name())
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
