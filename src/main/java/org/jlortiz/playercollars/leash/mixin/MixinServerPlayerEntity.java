package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerEntity implements LeashImpl {
    @Unique
    private final ServerPlayer leashplayers$self = (ServerPlayer) (Object) this;

    @Unique
    private LeashProxyEntity leashplayers$proxy;
    @Unique
    private Entity leashplayers$holder;

    @Unique
    private int leashplayers$lastage;

    @Unique
    private int leashplayer$loyalty;


    @Unique
    private void leashplayers$update() {
        if (
                leashplayers$holder != null && (
                        !leashplayers$holder.isAlive()
                                || !leashplayers$self.isAlive()
                                || leashplayers$self.hasDisconnected()
                                || leashplayers$self.isVehicle()
                )
        ) {
            leashplayers$detach();
            leashplayers$drop();
        }

        if (leashplayers$proxy != null) {
            if (leashplayers$proxy.proxyIsRemoved()) {
                leashplayers$proxy = null;
            }
            else {
                Entity holderActual = leashplayers$holder;
                Entity holderTarget = leashplayers$proxy.getLeashHolder();

                if (holderTarget == null && holderActual != null) {
                    leashplayers$detach();
                    leashplayers$drop();
                }
                else if (holderTarget != holderActual) {
                    leashplayers$attach(holderTarget);
                }
            }
        }

        leashplayers$apply();
    }

    @Unique
    private void leashplayers$apply() {
        ServerPlayer player = leashplayers$self;
        Entity holder = leashplayers$holder;
        if (holder == null) return;
        if (holder.level() != player.level()) return;

        float distance = player.distanceTo(holder);
        if (distance < 4 - leashplayer$loyalty) {
            return;
        }
        if (distance > 10f - leashplayer$loyalty) {
            leashplayers$detach();
            leashplayers$drop();
            return;
        }

        double dx = (holder.getX() - player.getX()) / (double) distance;
        double dy = (holder.getY() - player.getY()) / (double) distance;
        double dz = (holder.getZ() - player.getZ()) / (double) distance;
        final double factor = 0.4d + 0.1d * leashplayer$loyalty;

        player.push(
                Math.copySign(dx * dx * factor, dx),
                Math.copySign(dy * dy * factor, dy),
                Math.copySign(dz * dz * factor, dz)
        );

        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        player.hasImpulse = false;
    }

    @Unique
    private void leashplayers$attach(Entity entity) {
        leashplayers$holder = entity;

        if (leashplayers$proxy == null) {
            leashplayers$proxy = new LeashProxyEntity(leashplayers$self);
            leashplayers$proxy.setPos(leashplayers$self.getX(), leashplayers$self.getY(), leashplayers$self.getZ());
            leashplayers$self.level().addFreshEntity(leashplayers$proxy);
        }
        leashplayers$proxy.setLeashedTo(leashplayers$holder, true);

        if (leashplayers$self.isVehicle()) {
            leashplayers$self.stopRiding();
        }

        leashplayers$lastage = leashplayers$self.tickCount;
    }

    @Unique
    private void leashplayers$detach() {
        leashplayers$holder = null;

        if (leashplayers$proxy != null) {
            if (leashplayers$proxy.isAlive() || !leashplayers$proxy.proxyIsRemoved()) {
                leashplayers$proxy.proxyRemove();
            }
            leashplayers$proxy = null;
        }
    }

    @Unique
    private void leashplayers$drop() {
        leashplayers$self.drop(new ItemStack(Items.LEAD), false, true);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void leashplayers$tick(CallbackInfo info) {
        leashplayers$update();
    }

    @Override
    public InteractionResult leashplayers$interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == Items.LEAD && leashplayers$holder == null) {
            AtomicBoolean found = new AtomicBoolean(false);
            CuriosApi.getCuriosInventory((Player) (Object) this).ifPresent((handler) -> handler.getStacksHandler("necklace").ifPresent((slot) -> {
                ItemStack is = PlayerCollarsMod.filterStacksByOwner(slot.getStacks(), player.getUUID());
                if (is == null) {
                    is = PlayerCollarsMod.filterStacksByOwner(slot.getCosmeticStacks(), player.getUUID());
                }
                if (is != null) {
                    found.set(true);
                    leashplayer$loyalty = Mth.clamp(PlayerCollarsMod.COLLAR_ITEM.get().getEnchantmentLevel(is, Enchantments.LOYALTY), 0, 2);
                }
            }));
            if (!found.get()) return InteractionResult.PASS;
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            leashplayers$attach(player);
            return InteractionResult.SUCCESS;
        }

        if (leashplayers$holder == player && leashplayers$lastage + 20 < leashplayers$self.tickCount) {
            if (!player.isCreative()) {
                leashplayers$drop();
            }
            leashplayers$detach();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Inject(at=@At("TAIL"), method="hurt")
    private void checkCollarThorns(DamageSource p_9037_, float p_9038_, CallbackInfoReturnable<Boolean> cir) {
        if (p_9037_.getEntity() != null) {
            LivingEntity self = ((LivingEntity) (Object) this);
            CollarItem item = PlayerCollarsMod.COLLAR_ITEM.get();
            CuriosApi.getCuriosInventory(self).ifPresent((handler) -> {
                for (SlotResult sr : handler.findCurios("necklace")) {
                    int l = item.getEnchantmentLevel(sr.stack(), Enchantments.THORNS);
                    if (l > 0) {
                        Enchantments.THORNS.doPostHurt(self, p_9037_.getEntity(), l);
                    }
                }
            });
        }
    }
}