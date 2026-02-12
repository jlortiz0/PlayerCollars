package org.jlortiz.playercollars.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketEnums;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class FootPawsItem extends Item implements Trinket {
    public FootPawsItem(int color, int pawColor) {
        super(new Settings().maxCount(1)
                .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color | 0xFF000000, false))
                .component(DataComponentTypes.MAP_COLOR, new MapColorComponent(pawColor))
        );
    }

    public static int getColor(ItemStack is) {
        DyedColorComponent color = is.get(DataComponentTypes.DYED_COLOR);
        return color != null ? color.rgb() | 0xFF000000 : 0xFFFFFFFF;
    }

    public static int getBeanColor(ItemStack is) {
        MapColorComponent color = is.get(DataComponentTypes.MAP_COLOR);
        return color != null ? color.rgb() | 0xFF000000 : 0xFFF196CF;
    }

    public static Identifier getIdentifier(DyeColor c) {
        return Identifier.of(PlayerCollarsMod.MOD_ID, c.getName() + "_foot_paws");
    }

    @Override
    public TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return TrinketEnums.DropRule.KEEP;
    }
}
