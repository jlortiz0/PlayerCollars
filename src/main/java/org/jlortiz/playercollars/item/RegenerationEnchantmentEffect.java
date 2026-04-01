package org.jlortiz.playercollars.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.List;

public record RegenerationEnchantmentEffect(EnchantmentLevelBasedValue level) implements EnchantmentEntityEffect {
    public static final MapCodec<RegenerationEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                EnchantmentLevelBasedValue.CODEC.fieldOf("level").forGetter(RegenerationEnchantmentEffect::level)
            ).apply(instance, RegenerationEnchantmentEffect::new));

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        if (context.owner() == null) return;
        List<SlotEntryReference> ls = PlayerCollarsMod.getEquippedCollars(context.owner());
        for (SlotEntryReference p : ls) {
            OwnerComponent oc = p.stack().get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
            if (oc != null) {
                PlayerEntity own = world.getPlayerByUuid(oc.uuid());
                if (own != null && own.distanceTo(user) < 16) {
                    context.owner().addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, level, false, false, false));
                    return;
                }
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
