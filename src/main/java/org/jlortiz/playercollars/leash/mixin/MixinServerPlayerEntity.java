package org.jlortiz.playercollars.leash.mixin;

import com.mojang.authlib.GameProfile;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements LeashImpl {
    @Shadow public abstract boolean isDisconnected();

    @Shadow public ServerPlayNetworkHandler networkHandler;
    @Shadow @Final private MinecraftServer server;
    @Unique
    private LeashProxyEntity leashplayers$proxy;
    @Unique
    private Entity leashplayers$holder;
    @Unique
    private int leashplayers$lastage;
    @Unique
    private double leashplayer$loyalty;
    @Unique
    private static final double FIREWORK_SEARCH_RADIUS = 128.0;

    public MixinServerPlayerEntity(World world, GameProfile profile) {
        super(world, profile);
    }

    @Unique
    private void leashplayers$update() {
        if (
                leashplayers$holder != null && (
                        !leashplayers$holder.isAlive()
                                || !isAlive()
                                || isDisconnected()
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
        Entity holder = leashplayers$holder;
        if (holder == null) return;
        if (holder.getEntityWorld() != getEntityWorld()) {
            leashplayers$detach();
            leashplayers$drop();
            return;
        }

        ActionResult result;
        if (Math.abs(getY() - holder.getY()) > 6 + leashplayer$loyalty) {
            result = ActionResult.FAIL;
        } else {
            // Don't pull on the Y axis - it'll make the unfortunate player fly all over the place
            Vec3d pos = new Vec3d(holder.getX(), getY(), holder.getZ());
            result = PlayerCollarsMod.pullPlayerTowards((ServerPlayerEntity) (Object) this, pos,
                    leashplayer$loyalty, leashplayer$loyalty + 6, (x) -> Math.min(0.15 * (x - leashplayer$loyalty), 0.375) / x);
        }

        if (result == ActionResult.FAIL) {
            if (server.getOverworld().getGameRules().getValue(PlayerCollarsMod.PLAYER_LEASHES_BREAK_RULE)) {
                leashplayers$detach();
                leashplayers$drop();
            } else {
                // leashplayers$killFireworksOfPlayer(); // Ended up not using this
                this.setVelocity(Vec3d.ZERO);
                leashplayers$proxy.refreshPositionAndAngles(holder.getEntityPos(), leashplayers$proxy.getYaw(), leashplayers$proxy.getPitch());
                networkHandler.requestTeleport(holder.getX(), holder.getY(), holder.getZ(), getYaw(), getPitch());
            }
        }
    }

    @Unique
    private void leashplayers$killFireworksOfPlayer() {
        for (FireworkRocketEntity rocket : server.getOverworld().getEntitiesByClass(
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
        leashplayers$holder = entity;

        if (leashplayers$proxy == null) {
            leashplayers$proxy = new LeashProxyEntity(this);
            getEntityWorld().spawnEntity(leashplayers$proxy);
        }
        leashplayers$proxy.attachLeash(leashplayers$holder, true);

        if (this.hasVehicle() && !this.server.getOverworld().getGameRules().getValue(PlayerCollarsMod.LEASHED_PLAYERS_RIDE_ENTITIES)) {
            this.stopRiding();
        }

        leashplayers$lastage = age;
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
        dropItem(new ItemStack(Items.LEAD), false, true);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void leashplayers$tick(CallbackInfo info) {
        leashplayers$update();
    }

    @Inject(method = "startRiding", at = @At("HEAD"), cancellable = true)
    private void leashplayers$startriding(Entity entity, boolean force, boolean emitEvent, CallbackInfoReturnable<Boolean> cir) {

        boolean isLeashed = this.leashplayers$getProxyLeashHolder() != null;
        boolean disallowMount = !this.server.getOverworld().getGameRules().getValue(PlayerCollarsMod.LEASHED_PLAYERS_RIDE_ENTITIES);

        if (isLeashed && disallowMount) {
            this.sendMessage(Text.translatable("message.playercollars.no_ride_entity"), true);
            cir.cancel();
        }
    }

    @Override
    public Entity leashplayers$getProxyLeashHolder() {
        return leashplayers$proxy == null ? null : leashplayers$proxy.getLeashHolder();
    }

    @Override
    public ActionResult leashplayers$interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() == Items.LEAD && leashplayers$holder == null) {
            AccessoriesCapability cap = AccessoriesCapability.get(this);
            if (cap == null) return ActionResult.PASS;
            ItemStack is = PlayerCollarsMod.filterStacksByOwner(cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.COLLAR_TAG)), player.getUuid(), getUuid());
            if (is == null) return ActionResult.PASS;
            leashplayer$loyalty = getAttributeValue(PlayerCollarsMod.ATTR_LEASH_DISTANCE);
            if (!player.isCreative()) {
                stack.decrement(1);
            }
            leashplayers$attach(player);
            return ActionResult.SUCCESS;
        }

        if (leashplayers$holder == player && leashplayers$lastage + 20 < age) {
            if (!player.isCreative()) {
                leashplayers$drop();
            }
            leashplayers$detach();
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}