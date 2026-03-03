package org.jlortiz.playercollars.mixin;

import com.mojang.authlib.GameProfile;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.ThornsEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    protected ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(at = @At("TAIL"), method = "damage")
    private void checkCollarThorns(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        var player = (ServerPlayerEntity) (Object) this;
        if (source.getAttacker() instanceof LivingEntity attacker) {
            var trinkets = TrinketsApi.getTrinketComponent(attacker)
                    .map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)))
                    .orElseGet(List::of);

            for (Pair<SlotReference, ItemStack> p : trinkets) {
                var stack = p.getRight();
                var owner = NbtUtil.getOwner(stack);
                if (owner != null && !player.getUuid().equals(owner.getLeft())) {
                    continue;
                }

                EnchantmentHelper.Consumer consumer = (enchantment, level) -> {
                    var adjustedLevel = level;
                    // at level 7, it's guaranteed for the player to be damaged
                    if (enchantment instanceof ThornsEnchantment)
                        adjustedLevel = level * 2 + 1;
                    enchantment.onUserDamaged(player, attacker, adjustedLevel);
                };
                EnchantmentHelper.forEachEnchantment(consumer, stack);
            }
        }
    }
}
