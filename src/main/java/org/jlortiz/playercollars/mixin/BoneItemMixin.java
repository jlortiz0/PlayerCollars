package org.jlortiz.playercollars.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jlortiz.playercollars.item.DummyBoneItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Items.class)
public class BoneItemMixin {
    @Redirect(method="<clinit>", at=@At(value="NEW", target = "net/minecraft/item/Item", ordinal=44))
    private static Item makeEquippableBone(Item.Settings settings) {
        return new DummyBoneItem(settings);
    }
}
