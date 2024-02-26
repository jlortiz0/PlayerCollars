package org.jlortiz.playercollars.leash.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jlortiz.playercollars.PlayerCollarItem;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.UUID;
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
        if (holder.getLevel() != player.getLevel()) return;

        float distance = player.distanceTo(holder);
        if (distance < 4f) {
            return;
        }
        if (distance > 10f) {
            leashplayers$detach();
            leashplayers$drop();
            return;
        }

        double dx = (holder.getX() - player.getX()) / (double) distance;
        double dy = (holder.getY() - player.getY()) / (double) distance;
        double dz = (holder.getZ() - player.getZ()) / (double) distance;

        player.push(
                Math.copySign(dx * dx * 0.4D, dx),
                Math.copySign(dy * dy * 0.4D, dy),
                Math.copySign(dz * dz * 0.4D, dz)
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
            leashplayers$self.getLevel().addFreshEntity(leashplayers$proxy);
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

    @Unique
    private boolean playerCollars$filterStacksByOwner(IDynamicStackHandler stacks, UUID plr) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack is = stacks.getStackInSlot(i);
            if (is.getItem() instanceof PlayerCollarItem item) {
                Pair<UUID, String> owner = item.getOwner(is);
                if (owner != null && owner.getFirst().equals(plr)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResult leashplayers$interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == Items.LEAD && leashplayers$holder == null) {
            AtomicBoolean found = new AtomicBoolean(false);
            CuriosApi.getCuriosHelper().getCuriosHandler((Player) (Object) this).ifPresent((handler) -> {
                handler.getStacksHandler("necklace").ifPresent((slot) -> {
                    found.set(playerCollars$filterStacksByOwner(slot.getStacks(), player.getUUID()));
                    if (!found.get()) {
                        found.set(playerCollars$filterStacksByOwner(slot.getCosmeticStacks(), player.getUUID()));
                    }
                });
            });
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
}