package org.jlortiz.playercollars.mixin;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.PawsItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    @Final
    PlayerInventory inventory;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
    private static void playercollars$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue().add(PlayerCollarsMod.ATTR_LEASH_DISTANCE).add(PlayerCollarsMod.ATTR_CLICKER_DISTANCE);
    }

    @Shadow
    @Nullable
    public abstract ItemEntity dropItem(ItemStack stack, boolean retainOwnership);

    @Redirect(
            method = "getBlockBreakingSpeed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F"),
            require = 0
    )
    private float getBlockBreakingSpeed(PlayerInventory instance, BlockState block) {
        var hasPaws = TrinketsApi.getTrinketComponent(this)
                .map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.PAWS_TAG)))
                .filter((x) -> !x.isEmpty())
                .isPresent();

        if (hasPaws) {
            if (block.isIn(BlockTags.SHOVEL_MINEABLE)) {
                return ToolMaterials.IRON.getMiningSpeedMultiplier();
            }
            var originalSpeed = instance.getBlockBreakingSpeed(block);
            if (originalSpeed == 1)
                return 1.0f;
            return (originalSpeed - 1) * 0.125f + 1;
        } else {
            return instance.getBlockBreakingSpeed(block);
        }
    }

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D",
                    ordinal = 0
            ),
            require = 0
    )
    private double getAttributeValue(PlayerEntity instance, EntityAttribute entityAttribute) {
        double ret = instance.getAttributeValue(entityAttribute);
        return TrinketsApi.getTrinketComponent(this)
                .map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.PAWS_TAG)))
                .filter((x) -> !x.isEmpty()).map((x) -> (ret - 1) * 0.75f + 1).orElse(ret);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;updateItems()V", shift = At.Shift.AFTER))
    private void playercollars$dropPawItems(CallbackInfo ci) {
        TrinketsApi.getTrinketComponent(this)
                .map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.PAWS_TAG)))
                .ifPresent((ls) -> {
                    for (Pair<SlotReference, ItemStack> p : ls) {
                        if (PawsItem.shouldDrop(p.getRight(), this.inventory.getMainHandStack())) {
                            ItemStack stack = this.inventory.dropSelectedItem(true);
                            if (!stack.isEmpty()) dropItem(stack, true);
                        }
                        if (PawsItem.shouldDrop(p.getRight(), this.inventory.getStack(PlayerInventory.OFF_HAND_SLOT))) {
                            ItemStack stack = this.inventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                            if (!stack.isEmpty()) dropItem(stack, true);
                        }
                    }
                });
    }

    @Redirect(
            method = "updatePose",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setPose(Lnet/minecraft/entity/EntityPose;)V")
    )
    private void playercollars$forceCrawl(PlayerEntity instance, EntityPose entityPose) {
        if (!instance.getAbilities().flying && (entityPose == EntityPose.CROUCHING || entityPose == EntityPose.STANDING)) {
            if (TrinketsApi.getTrinketComponent(this).map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.FOOT_PAWS_TAG)))
                    .filter((x) -> !x.isEmpty()).isPresent()) {
                entityPose = EntityPose.SWIMMING;
            }
        }
        instance.setPose(entityPose);
    }
}
