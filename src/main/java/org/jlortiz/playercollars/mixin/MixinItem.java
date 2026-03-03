package org.jlortiz.playercollars.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.item.Item;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.item.FootPawsItem;
import org.jlortiz.playercollars.item.PawsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class MixinItem {
    @ModifyReturnValue(method = "hasGlint", at = @At("RETURN"))
    public boolean hasGlint(boolean original) {
        var item = (Item) (Object) this;
        if (item instanceof FootPawsItem || item instanceof PawsItem || item instanceof CollarItem)
            return false;
        return original;
    }
}
