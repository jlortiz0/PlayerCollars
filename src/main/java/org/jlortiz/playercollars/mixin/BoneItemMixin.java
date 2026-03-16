package org.jlortiz.playercollars.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jlortiz.playercollars.item.DummyBoneItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Items.class)
public class BoneItemMixin {
    @Redirect(
            method = "<clinit>",
            at = @At(value = "NEW", target = "net/minecraft/item/Item"),
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=bone"),
                    to = @At(value = "CONSTANT", args = "stringValue=sugar")
            )
    )
    private static Item makeEquippableBone(Item.Settings settings) {
        return new DummyBoneItem(settings);
    }
}
