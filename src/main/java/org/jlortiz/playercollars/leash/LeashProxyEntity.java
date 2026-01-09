package org.jlortiz.playercollars.leash;

import net.minecraft.entity.*;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathConstants;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.Objects;

public final class LeashProxyEntity extends TurtleEntity {
    private final LivingEntity target;
    private static final EntityDimensions DIMENSIONS = EntityDimensions.fixed(MathConstants.EPSILON, MathConstants.EPSILON);

    private boolean proxyUpdate() {
        if (proxyIsRemoved()) return false;

        if (target == null) return true;
        if (target.getWorld() != getWorld() || !target.isAlive()) return true;

        Vec3d posActual = this.getPos();
        Vec3d posTarget = switch (target.getPose()) {
            // No point in making cases for SPIN_ATTACK since leashed players can't use it
            case CROUCHING: yield new Vec3d(0.0D, 1.1D, -0.15D);
            case SWIMMING: yield Vec3d.fromPolar(0, target.getBodyYaw()).multiply(0.35).add(0, 0.2, -0.1);
            case GLIDING: yield new Vec3d(0, 1.3, -0.15).rotateX(-Math.toRadians(90 + target.getPitch()))
                    .rotateY(-Math.toRadians(target.getBodyYaw()));
            case SLEEPING: if (target.getSleepingDirection() != null)
                    yield new Vec3d(target.getSleepingDirection().getUnitVector().mul(-0.2f)).add(0, 0.1, -0.15);
            default: yield new Vec3d(0.0D, 1.3D, -0.15D);
        };
        posTarget = posTarget.multiply(target.getScale()).add(target.getPos());

        if (!Objects.equals(posActual, posTarget)) {
            setRotation(0.0F, 0.0F);
            setPos(posTarget.x, posTarget.y, posTarget.z);
            setBoundingBox(DIMENSIONS.getBoxAt(target.getPos()));
        }

        return false;
    }

    @NotNull
    public LivingEntity getLeashTarget() {
        return target;
    }

    @Override
    public void tick() {
        if (this.getWorld().isClient) return;
        if (proxyUpdate() && !proxyIsRemoved()) {
            proxyRemove();
        }
    }

    public boolean proxyIsRemoved() {
        return this.isRemoved();
    }

    public void proxyRemove() {
        super.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void remove(RemovalReason reason) {
    }

    public static final String TEAM_NAME = "leashplayersimpl";

    public LeashProxyEntity(@NotNull LivingEntity target) {
        super(EntityType.TURTLE, target.getWorld());
        this.target = target;

        setHealth(1.0F);
        setInvulnerable(true);
        setBaby(true);
        setInvisible(true);
        noClip = true;

        MinecraftServer server = getServer();
        if (server != null) {
            ServerScoreboard scoreboard = server.getScoreboard();

            Team team = scoreboard.getTeam(TEAM_NAME);
            if (team == null) {
                team = scoreboard.addTeam(TEAM_NAME);
            }
            if (team.getCollisionRule() != Team.CollisionRule.NEVER) {
                team.setCollisionRule(Team.CollisionRule.NEVER);
            }

            scoreboard.addScoreHolderToTeam(getNameForScoreboard(), team);
        }
        proxyUpdate();
    }

    @Override
    public float getHealth() {
        return 1.0F;
    }

    @Override
    public void detachLeash() {
    }

    @Override
    public void detachLeashWithoutDrop() {
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected void initGoals() {
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("Team", TEAM_NAME);
    }

    @Override
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
    }
}