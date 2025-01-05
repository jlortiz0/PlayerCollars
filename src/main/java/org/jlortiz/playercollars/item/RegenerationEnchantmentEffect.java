package org.jlortiz.playercollars.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.List;
import java.util.Optional;

public record RegenerationEnchantmentEffect(EnchantmentLevelBasedValue level) implements EnchantmentEntityEffect {
    public static final MapCodec<RegenerationEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                EnchantmentLevelBasedValue.CODEC.fieldOf("level").forGetter(RegenerationEnchantmentEffect::level)
            ).apply(instance, RegenerationEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        Optional<List<Pair<SlotReference, ItemStack>>> o = TrinketsApi.getTrinketComponent(context.owner()).map((x) -> x.getEquipped(PlayerCollarsMod.COLLAR_ITEM));
        if (o.isPresent()) {
            List<Pair<SlotReference, ItemStack>> ls = o.get();
            for (Pair<SlotReference, ItemStack> p : ls) {
                OwnerComponent oc = p.getRight().get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
                if (oc != null) {
                    PlayerEntity own = world.getPlayerByUuid(oc.uuid());
                    if (own != null && own.distanceTo(user) < 16) {
                        context.owner().addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, level, false, false, false));
                        return;
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
