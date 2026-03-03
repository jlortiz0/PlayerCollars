package org.jlortiz.playercollars.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketEnums;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class FootPawsItem extends Item implements Trinket {
    private final int color;
    private final int pawColor;

    public FootPawsItem(int color, int pawColor) {
        super(new Settings().maxCount(1));
        this.color = color;
        this.pawColor = pawColor;
    }

    public static Identifier getIdentifier(DyeColor c) {
        return Identifier.of(PlayerCollarsMod.MOD_ID, c.getName() + "_foot_paws");
    }

    @Override
    public TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return TrinketEnums.DropRule.KEEP;
    }

    public int getColor() {
        return this.color;
    }

    public int getPawColor() {
        return this.pawColor;
    }
}
