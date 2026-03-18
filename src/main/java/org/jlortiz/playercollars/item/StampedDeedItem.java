package org.jlortiz.playercollars.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.List;

public class StampedDeedItem extends Item {
    public StampedDeedItem() { super(new Properties().stacksTo(1)); }

    @Override
    public Component getName(ItemStack stack) {
        OwnerComponent owner = NbtUtil.getDeedOwner(stack);
        if (owner == null || owner.owned().isEmpty())
            return Component.translatable("item.playercollars.deed_of_ownership");
        return Component.translatable("item.playercollars.stamped_deed_of_ownership", owner.ownedName().get());
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) { return stack.copy(); }
    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        OwnerComponent owner = NbtUtil.getDeedOwner(stack);
        if (owner != null)
            tooltip.add(Component.translatable("item.playercollars.collar.owner", owner.name())
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
