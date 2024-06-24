package org.jlortiz.playercollars.compat.mixin;

import com.hakimen.kawaiidishes.items.UnbindingCookie;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

@Mixin(UnbindingCookie.class)
public class KawaiiDishesMixin {
    @Inject(method = "onCraftedBy", at=@At("TAIL"))
    private void checkCuriosToo(ItemStack pStack, Level pLevel, Player pPlayer, CallbackInfo ci) {
        CuriosApi.getCuriosInventory(pPlayer).map(ICuriosItemHandler::getCurios).ifPresent((items) -> {
            for (ICurioStacksHandler m : items.values()) {
                for (int i = 0; i < m.getStacks().getSlots(); i++) {
                    if (m.getStacks().getStackInSlot(i).getEnchantmentLevel(Enchantments.BINDING_CURSE) != 0) {
                        pStack.getOrCreateTag().putBoolean("activated", true);
                        return;
                    }
                }
                for (int i = 0; i < m.getCosmeticStacks().getSlots(); i++) {
                    if (m.getCosmeticStacks().getStackInSlot(i).getEnchantmentLevel(Enchantments.BINDING_CURSE) != 0) {
                        pStack.getOrCreateTag().putBoolean("activated", true);
                        return;
                    }
                }
            }
        });
    }

    @Inject(method="finishUsingItem", at=@At("HEAD"))
    private void dropCuriosToo(ItemStack pStack, Level pLevel, LivingEntity plr, CallbackInfoReturnable<ItemStack> cir) {
        CuriosApi.getCuriosInventory(plr).map(ICuriosItemHandler::getCurios).ifPresent((handler) -> handler.forEach((ident, m) -> {
            for (int i = 0; i < m.getStacks().getSlots(); i++) {
                pLevel.addFreshEntity(new ItemEntity(pLevel, plr.getX(), plr.getY(), plr.getZ(), m.getStacks().getStackInSlot(i)));
                m.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                plr.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 1));
                plr.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
            }
            for (int i = 0; i < m.getCosmeticStacks().getSlots(); i++) {
                pLevel.addFreshEntity(new ItemEntity(pLevel, plr.getX(), plr.getY(), plr.getZ(), m.getCosmeticStacks().getStackInSlot(i)));
                m.getCosmeticStacks().setStackInSlot(i, ItemStack.EMPTY);
                plr.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 1));
                plr.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
            }
        }));
    }
}
