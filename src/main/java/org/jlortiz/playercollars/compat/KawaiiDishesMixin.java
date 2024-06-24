package org.jlortiz.playercollars.compat;

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
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@Mixin(UnbindingCookie.class)
public class KawaiiDishesMixin {
    @Inject(method = "onCraftedBy", at=@At("TAIL"))
    private void checkCuriosToo(ItemStack pStack, Level pLevel, Player pPlayer, CallbackInfo ci) {
        CuriosApi.getCuriosInventory(pPlayer).map(ICuriosItemHandler::findCurios).ifPresent((items) -> {
            for (SlotResult sr : items) {
                if (sr.stack().getEnchantmentLevel(Enchantments.BINDING_CURSE) != 0) {
                    pStack.getOrCreateTag().putBoolean("activated", true);
                    return;
                }
            }
        });
    }

    @Inject(method="finishUsingItem", at=@At("HEAD"))
    private void dropCuriosToo(ItemStack pStack, Level pLevel, LivingEntity plr, CallbackInfoReturnable<ItemStack> cir) {
        CuriosApi.getCuriosInventory(plr).ifPresent((handler) -> {
            for (SlotResult sr : handler.findCurios()) {
                if (sr.stack().getEnchantmentLevel(Enchantments.BINDING_CURSE) != 0) {
                    pLevel.addFreshEntity(new ItemEntity(pLevel, plr.getX(), plr.getY(), plr.getZ(), sr.stack()));
                    handler.setEquippedCurio(sr.slotContext().identifier(), sr.slotContext().index(), ItemStack.EMPTY);
                    plr.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 1));
                    plr.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100));
                }
            }
        });
    }
}
