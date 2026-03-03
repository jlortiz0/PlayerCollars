package org.jlortiz.playercollars.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.List;

public class StampedDeedItem extends Item {
    public StampedDeedItem() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public Text getName(ItemStack stack) {
        OwnerComponent owner = NbtUtil.getDeedOwner(stack);
        if (owner == null || owner.owned().isEmpty()) return Text.translatable("item.playercollars.deed_of_ownership");
        return Text.translatable("item.playercollars.stamped_deed_of_ownership", owner.ownedName().get());
    }


    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        OwnerComponent owner = NbtUtil.getDeedOwner(stack);
        if (owner != null) {
            tooltip.add(Text.translatable("item.playercollars.collar.owner", owner.name()).formatted(Formatting.GRAY));
        }
    }
}
