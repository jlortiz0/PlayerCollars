package org.jlortiz.playercollars.item;

import io.wispforest.accessories.api.core.AccessoryItem;
import io.wispforest.accessories.api.events.DropRule;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class FootPawsItem extends AccessoryItem {
    public final int color, beansColor;

    public FootPawsItem(RegistryKey<Item> key, int color, int beansColor) {
        super(new Item.Settings().maxCount(1).registryKey(key)
                .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color | 0xFF000000))
                .component(DataComponentTypes.MAP_COLOR, new MapColorComponent(beansColor))
        );
        this.color = color | 0xFF000000;
        this.beansColor = beansColor;
    }

    public static RegistryKey<Item> getRegistryKey(DyeColor c) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, c.getId() + "_foot_paws"));
    }

    @Override
    public DropRule getDropRule(ItemStack stack, SlotReference reference, DamageSource source) {
        return DropRule.KEEP;
    }
}
