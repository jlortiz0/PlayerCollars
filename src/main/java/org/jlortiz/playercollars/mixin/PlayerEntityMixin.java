package org.jlortiz.playercollars.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
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
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow @Final public Inventory inventory;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void playercollars$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue()
                .add(PlayerCollarsMod.ATTR_LEASH_DISTANCE.get())
                .add(PlayerCollarsMod.ATTR_CLICKER_DISTANCE.get());
    }

    @Shadow public abstract @Nullable ItemEntity drop(ItemStack stack, boolean retainOwnership);

    // Redirect block-breaking speed through paws check
    @Redirect(
            method = "getDestroySpeed",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"),
            require = 0
    )
    private float getDestroySpeed(Inventory instance, BlockState block) {
        var hasPaws = CuriosApi.getCuriosInventory(this)
                .map(h -> !h.findCurios(s -> s.is(PlayerCollarsMod.PAWS_TAG)).isEmpty())
                .orElse(false);

        if (hasPaws) {
            if (block.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL))
                return Tiers.IRON.getSpeed();

            float originalSpeed = instance.getDestroySpeed(block);
            // noinspection FloatingPointEquality
            if (originalSpeed == 1.0f) return 1.0f;
            return (originalSpeed - 1) * 0.125f + 1;
        }
        return instance.getDestroySpeed(block);
    }

    // Reduce attack damage while wearing paws
    @Redirect(
            method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D",
                    ordinal = 0),
            require = 0
    )
    private double getAttackDamage(Player instance,
                                    net.minecraft.world.entity.ai.attributes.Attribute attribute) {
        double ret = instance.getAttributeValue(attribute);
        return CuriosApi.getCuriosInventory(this)
                .map(h -> !h.findCurios(s -> s.is(PlayerCollarsMod.PAWS_TAG)).isEmpty())
                .filter(b -> b)
                .map(b -> (ret - 1) * 0.75 + 1)
                .orElse(ret);
    }

    // Drop slippery items from paws each tick
    @Inject(method = "aiStep",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;tick()V",
                    shift = At.Shift.AFTER))
    private void playercollars$dropPawItems(CallbackInfo ci) {
        CuriosApi.getCuriosInventory(this).ifPresent(h -> {
            for (var sr : h.findCurios(s -> s.is(PlayerCollarsMod.PAWS_TAG))) {
                ItemStack pawsStack = sr.stack();
                if (PawsItem.shouldDrop(pawsStack, this.inventory.getSelected())) {
                    ItemStack stack = this.inventory.removeItem(this.inventory.selected, 1);
                    if (!stack.isEmpty()) drop(stack, true);
                }
                ItemStack offhand = this.inventory.getItem(40);
                if (PawsItem.shouldDrop(pawsStack, offhand)) {
                    ItemStack stack = this.inventory.removeItem(40, offhand.getCount());
                    if (!stack.isEmpty()) drop(stack, true);
                }
            }
        });
    }

    // Force crawl pose when wearing foot paws
    @Redirect(
            method = "updatePlayerPose",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;setPose(Lnet/minecraft/world/entity/Pose;)V")
    )
    private void playercollars$forceCrawl(Player instance, Pose pose) {
        Pose updated = pose;
        if (!instance.getAbilities().flying && (pose == Pose.CROUCHING || pose == Pose.STANDING)) {
            boolean hasFootPaws = CuriosApi.getCuriosInventory(this)
                    .map(h -> !h.findCurios(s -> s.is(PlayerCollarsMod.FOOT_PAWS_TAG)).isEmpty())
                    .orElse(false);
            if (hasFootPaws) updated = Pose.SWIMMING;
        }
        instance.setPose(updated);
    }
}
