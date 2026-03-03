package org.jlortiz.playercollars.leash.mixin;

import com.mojang.authlib.GameProfile;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements LeashImpl {
    @Unique
    private static final double FIREWORK_SEARCH_RADIUS = 128.0;
    @Shadow
    public ServerPlayNetworkHandler networkHandler;
    @Unique
    private LeashProxyEntity leashplayers$proxy;
    @Unique
    private Entity leashplayers$holder;
    @Unique
    private int leashplayers$lastage;
    @Unique
    private double leashplayer$loyalty;

    protected MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow
    public abstract boolean isDisconnected();

    @Shadow
    public abstract ServerWorld getServerWorld();

    @Unique
    private void leashplayers$update() {
        if (
                this.leashplayers$holder != null && (
                        !this.leashplayers$holder.isAlive()
                        || !isAlive()
                        || isDisconnected()
                )
        ) {
            leashplayers$detach();
            leashplayers$drop();
        }

        if (this.leashplayers$proxy != null) {
            if (this.leashplayers$proxy.proxyIsRemoved()) {
                this.leashplayers$proxy = null;
            } else {
                Entity holderActual = this.leashplayers$holder;
                Entity holderTarget = this.leashplayers$proxy.getHoldingEntity();

                if (holderTarget == null && holderActual != null) {
                    leashplayers$detach();
                    leashplayers$drop();
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
        if (holder.getWorld() != getWorld()) {
            leashplayers$detach();
            leashplayers$drop();
            return;
        }

        ActionResult result;
        if (Math.abs(getY() - holder.getY()) > 6 + this.leashplayer$loyalty) {
            result = ActionResult.FAIL;
        } else {
            // Don't pull on the Y axis - it'll make the unfortunate player fly all over the place
            Vec3d pos = new Vec3d(holder.getX(), getY(), holder.getZ());
            result = PlayerCollarsMod.pullPlayerTowards((ServerPlayerEntity) (Object) this, pos,
                    this.leashplayer$loyalty, this.leashplayer$loyalty + 6, (x) -> Math.min(0.15 * (x - this.leashplayer$loyalty), 0.375) / x);
        }

        if (result == ActionResult.FAIL) {
            if (getServerWorld().getGameRules().getBoolean(PlayerCollarsMod.PLAYER_LEASHES_BREAK_RULE)) {
                leashplayers$detach();
                leashplayers$drop();
            } else {
                // leashplayers$killFireworksOfPlayer(); // Ended up not using this
                this.setVelocity(Vec3d.ZERO);
                this.leashplayers$proxy.refreshPositionAndAngles(holder.getBlockPos(), this.leashplayers$proxy.getYaw(), this.leashplayers$proxy.getPitch());
                this.networkHandler.requestTeleport(holder.getX(), holder.getY(), holder.getZ(), getYaw(), getPitch());
            }
        }
    }

    @Unique
    private void leashplayers$killFireworksOfPlayer() {
        for (FireworkRocketEntity rocket : getServerWorld().getEntitiesByClass(
                FireworkRocketEntity.class,
                getBoundingBox().expand(FIREWORK_SEARCH_RADIUS),
                rocket -> true
        )) {
            Entity owner = rocket.getOwner();
            if (owner != null) {
                UUID ownerUUID = owner.getUuid();
                if (ownerUUID != null && ownerUUID.equals(this.getUuid())) {
                    rocket.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }
    }

    @Unique
    private void leashplayers$attach(Entity entity) {
        this.leashplayers$holder = entity;

        if (this.leashplayers$proxy == null) {
            this.leashplayers$proxy = new LeashProxyEntity(this);
            getWorld().spawnEntity(this.leashplayers$proxy);
        }
        this.leashplayers$proxy.attachLeash(this.leashplayers$holder, true);

        if (hasVehicle() && getServerWorld().getGameRules().getBoolean(PlayerCollarsMod.PLAYER_LEASHES_BREAK_RULE)) {
            stopRiding();
        }

        this.leashplayers$lastage = this.age;
    }

    @Unique
    private void leashplayers$detach() {
        this.leashplayers$holder = null;

        if (this.leashplayers$proxy != null) {
            if (this.leashplayers$proxy.isAlive() || !this.leashplayers$proxy.proxyIsRemoved()) {
                this.leashplayers$proxy.proxyRemove();
            }
            this.leashplayers$proxy = null;
        }
    }

    @Unique
    private void leashplayers$drop() {
        dropItem(new ItemStack(Items.LEAD), false, true);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void leashplayers$tick(CallbackInfo info) {
        leashplayers$update();
    }

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("HEAD"), cancellable = true)
    private void leashplayers$startriding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {

        boolean isLeashed = this.leashplayers$getProxyLeashHolder() != null;
        boolean disallowMount = !this.getServerWorld().getGameRules().getBoolean(PlayerCollarsMod.LEASHED_PLAYERS_RIDE_ENTITIES);

        if (isLeashed && disallowMount) {
            this.sendMessage(Text.translatable("message.playercollars.no_ride_entity"), true);
            cir.cancel();
        }
    }

    @Override
    public Entity leashplayers$getProxyLeashHolder() {
        return this.leashplayers$proxy == null ? null : this.leashplayers$proxy.getHoldingEntity();
    }

    @Override
    public ActionResult leashplayers$interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() == Items.LEAD && this.leashplayers$holder == null) {
            AtomicBoolean found = new AtomicBoolean(false);
            TrinketsApi.getTrinketComponent(this).map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)))
                    .map((x) -> PlayerCollarsMod.filterStacksByOwner(x, player.getUuid(), getUuid()))
                    .ifPresent((stack1) -> {
                        found.set(true);
                        this.leashplayer$loyalty = getAttributeValue(PlayerCollarsMod.ATTR_LEASH_DISTANCE);
                    });
            if (!found.get()) return ActionResult.PASS;
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            leashplayers$attach(player);
            return ActionResult.SUCCESS;
        }

        if (this.leashplayers$holder == player && this.leashplayers$lastage + 20 < this.age) {
            if (!player.isCreative()) {
                leashplayers$drop();
            }
            leashplayers$detach();
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
