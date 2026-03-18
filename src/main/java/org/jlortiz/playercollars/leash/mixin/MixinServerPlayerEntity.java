package org.jlortiz.playercollars.leash.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerEntity extends Player implements LeashImpl {
    @Unique private static final double FIREWORK_SEARCH_RADIUS = 128.0;
    @Shadow public ServerGamePacketListenerImpl connection;
    @Unique private @Nullable LeashProxyEntity leashplayers$proxy;
    @Unique private @Nullable Entity leashplayers$holder;
    @Unique private int leashplayers$lastage;
    @Unique private double leashplayer$loyalty;

    protected MixinServerPlayerEntity(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract boolean hasDisconnected();
    @Shadow public abstract ServerLevel serverLevel();

    @Unique
    private void leashplayers$update() {
        if (this.leashplayers$holder != null && (!this.leashplayers$holder.isAlive() || !isAlive() || hasDisconnected())) {
            leashplayers$detach();
            leashplayers$drop();
        }
        if (this.leashplayers$proxy != null) {
            if (this.leashplayers$proxy.proxyIsRemoved()) {
                this.leashplayers$proxy = null;
            } else {
                Entity holderActual = this.leashplayers$holder;
                Entity holderTarget = this.leashplayers$proxy.getLeashHolder();
                if (holderTarget == null && holderActual != null) {
                    leashplayers$detach(); leashplayers$drop();
                } else if (holderTarget != holderActual) {
                    leashplayers$attach(holderTarget);
                }
            }
        }
        leashplayers$apply();
    }

    @Unique
    private void leashplayers$apply() {
        Entity holder = this.leashplayers$holder;
        if (holder == null) return;
        if (holder.level() != level()) { leashplayers$detach(); leashplayers$drop(); return; }

        InteractionResult result;
        if (Math.abs(getY() - holder.getY()) > 6 + this.leashplayer$loyalty) {
            result = InteractionResult.FAIL;
        } else {
            Vec3 pos = new Vec3(holder.getX(), getY(), holder.getZ());
            result = PlayerCollarsMod.pullPlayerTowards((ServerPlayer)(Object)this, pos,
                    this.leashplayer$loyalty, this.leashplayer$loyalty + 6,
                    x -> Math.min(0.15 * (x - this.leashplayer$loyalty), 0.375) / x);
        }

        // noinspection ConstantValue
        if (result == InteractionResult.FAIL) {
            if (serverLevel().getGameRules().getBoolean(PlayerCollarsMod.PLAYER_LEASHES_BREAK_RULE)) {
                leashplayers$detach(); leashplayers$drop();
            } else {
                this.setDeltaMovement(Vec3.ZERO);
                Objects.requireNonNull(this.leashplayers$proxy)
                        .moveTo(holder.blockPosition(), this.leashplayers$proxy.getYRot(),
                                this.leashplayers$proxy.getXRot());
                this.connection.teleport(holder.getX(), holder.getY(), holder.getZ(), getYRot(), getXRot());
            }
        }
    }

    @Unique
    private void leashplayers$attach(Entity entity) {
        this.leashplayers$holder = entity;
        if (this.leashplayers$proxy == null) {
            this.leashplayers$proxy = new LeashProxyEntity(this);
            level().addFreshEntity(this.leashplayers$proxy);
        }
        this.leashplayers$proxy.setLeashedTo(this.leashplayers$holder, true);
        if (isPassenger() && serverLevel().getGameRules().getBoolean(PlayerCollarsMod.PLAYER_LEASHES_BREAK_RULE))
            stopRiding();
        this.leashplayers$lastage = this.tickCount;
    }

    @Unique
    private void leashplayers$detach() {
        this.leashplayers$holder = null;
        if (this.leashplayers$proxy != null) {
            if (this.leashplayers$proxy.isAlive() || !this.leashplayers$proxy.proxyIsRemoved())
                this.leashplayers$proxy.proxyRemove();
            this.leashplayers$proxy = null;
        }
    }

    @Unique
    private void leashplayers$drop() { drop(new ItemStack(Items.LEAD), false); }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void leashplayers$tick(CallbackInfo info) { leashplayers$update(); }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("HEAD"), cancellable = true)
    private void leashplayers$startRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
        boolean isLeashed = leashplayers$getProxyLeashHolder() != null;
        boolean disallowMount = !serverLevel().getGameRules().getBoolean(PlayerCollarsMod.LEASHED_PLAYERS_RIDE_ENTITIES);
        if (isLeashed && disallowMount) {
            sendSystemMessage(Component.translatable("message.playercollars.no_ride_entity"));
            cir.cancel();
        }
    }

    @Override
    public Entity leashplayers$getProxyLeashHolder() {
        return this.leashplayers$proxy == null ? null : this.leashplayers$proxy.getLeashHolder();
    }

    @Override
    public InteractionResult leashplayers$interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.LEAD) && this.leashplayers$holder == null) {
            AtomicBoolean found = new AtomicBoolean(false);
            CuriosApi.getCuriosInventory(this).ifPresent(h -> {
                var collars = h.findCurios(s -> s.is(PlayerCollarsMod.COLLAR_TAG));
                if (PlayerCollarsMod.filterStacksByOwner(collars, player.getUUID(), getUUID()) != null) {
                    found.set(true);
                    this.leashplayer$loyalty = getAttributeValue(PlayerCollarsMod.ATTR_LEASH_DISTANCE.get());
                }
            });
            if (!found.get()) return InteractionResult.PASS;
            if (!player.isCreative()) stack.shrink(1);
            leashplayers$attach(player);
            return InteractionResult.SUCCESS;
        }
        if (this.leashplayers$holder == player && this.leashplayers$lastage + 20 < this.tickCount) {
            if (!player.isCreative()) leashplayers$drop();
            leashplayers$detach();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
