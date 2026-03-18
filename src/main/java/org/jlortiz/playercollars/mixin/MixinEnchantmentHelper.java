package org.jlortiz.playercollars.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {

    // Apply collar enchantment effects (e.g. Thorns) when the wearer is hurt.
    // Injecting AFTER the normal armor iteration so collars get the same treatment.
    @Inject(
            method = "doPostHurtEffects",
            at = @At("TAIL"),
            require = 0
    )
    private static void applyCollarEnchantments(LivingEntity user, Entity attacker, CallbackInfo ci) {
        CuriosApi.getCuriosInventory(user).ifPresent(h -> {
            List<ItemStack> stacks = h.findCurios(s -> s.is(PlayerCollarsMod.COLLAR_TAG))
                    .stream().map(sr -> sr.stack()).toList();
            for (ItemStack stack : stacks) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    entry.getKey().doPostHurt(user, attacker, entry.getValue());
                }
            }
        });
    }
}
