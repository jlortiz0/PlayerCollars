package org.jlortiz.playercollars.mixin;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;
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
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
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
    @Shadow @Final PlayerInventory inventory;

    @Shadow @Nullable public abstract ItemEntity dropItem(ItemStack stack, boolean retainOwnership);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "getBlockBreakingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F"), require=0)
    private float getBlockBreakingSpeed(PlayerInventory instance, BlockState block) {
        float ret = instance.getBlockBreakingSpeed(block);
        AccessoriesCapability cap = AccessoriesCapability.get(this);
        if (cap == null) return ret;
        if (cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.PAWS_TAG)).isEmpty()) return ret;
        if (TagUtil.isIn(BlockTags.SHOVEL_MINEABLE, block.getBlock())) {
            return ToolMaterial.IRON.speed();
        }
        return (ret - 1) * 0.125f + 1;
    }

    @Redirect(method="attack", at= @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D", ordinal=0), require=0)
    private double getAttributeValue(PlayerEntity instance, RegistryEntry<EntityAttribute> registryEntry) {
        double ret = instance.getAttributeValue(registryEntry);
        AccessoriesCapability cap = AccessoriesCapability.get(this);
        if (cap == null) return ret;
        if (cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.PAWS_TAG)).isEmpty()) return ret;
        return (ret - 1) * 0.75f + 1;
    }

    @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
    private static void playercollars$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.getReturnValue().add(PlayerCollarsMod.ATTR_LEASH_DISTANCE).add(PlayerCollarsMod.ATTR_CLICKER_DISTANCE);
    }

    @Inject(method = "tickMovement", at= @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;updateItems()V", shift = At.Shift.AFTER))
    private void playercollars$dropPawItems(CallbackInfo ci) {
        AccessoriesCapability cap = AccessoriesCapability.get(this);
        if (cap == null) return;
        for (SlotEntryReference sr : cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.PAWS_TAG))) {
            if (PawsItem.shouldDrop(sr.stack(), inventory.getMainHandStack())) {
                ItemStack stack = inventory.dropSelectedItem(true);
                if (!stack.isEmpty()) dropItem(stack, true);
            }
            if (PawsItem.shouldDrop(sr.stack(), inventory.getStack(PlayerInventory.OFF_HAND_SLOT))){
                ItemStack stack = inventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                if (!stack.isEmpty()) dropItem(stack, true);
            }
        }
    }

    @Redirect(method="updatePose", at= @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"))
    private void playercollars$forceCrawl(PlayerEntity instance, EntityPose entityPose) {
        if (!instance.getAbilities().flying && (entityPose == EntityPose.CROUCHING || entityPose == EntityPose.STANDING)) {
            AccessoriesCapability cap = AccessoriesCapability.get(this);
            if (cap != null && !cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.FOOT_PAWS_TAG)).isEmpty())
                entityPose = EntityPose.SWIMMING;
        }
        instance.setPose(entityPose);
    }
}
