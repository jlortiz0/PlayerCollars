package org.jlortiz.playercollars.leash;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.Objects;

public final class LeashProxyEntity extends Turtle {
    public static final String TEAM_NAME = "leashplayersimpl";
    private static final EntityDimensions DIMENSIONS = EntityDimensions.fixed(1e-6f, 1e-6f);
    private final LivingEntity target;

    public LeashProxyEntity(@NotNull LivingEntity target) {
        super(EntityType.TURTLE, target.level());
        this.target = target;

        setHealth(1.0F);
        setInvulnerable(true);
        setBaby(true);
        setInvisible(true);
        this.noPhysics = true;

        MinecraftServer server = getServer();
        if (server != null) {
            ServerScoreboard scoreboard = server.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
            if (team == null) team = scoreboard.addPlayerTeam(TEAM_NAME);
            if (team.getCollisionRule() != Team.CollisionRule.NEVER)
                team.setCollisionRule(Team.CollisionRule.NEVER);
            scoreboard.addPlayerToTeam(getScoreboardName(), team);
        }
        proxyUpdate();
    }

    private boolean proxyUpdate() {
        if (proxyIsRemoved()) return false;
        if (this.target == null) return true;
        if (this.target.level() != level() || !this.target.isAlive()) return true;

        Vec3 posTarget = switch (this.target.getPose()) {
            case CROUCHING -> new Vec3(0, 1.1, -0.15);
            case SWIMMING -> Vec3.directionFromRotation(0, this.target.yBodyRot).scale(0.35).add(0, 0.2, -0.1);
            case FALL_FLYING -> new Vec3(0, 1.3, -0.15)
                    .xRot(-(float) Math.toRadians(90 + this.target.getXRot()))
                    .yRot(-(float) Math.toRadians(this.target.yBodyRot));
            case SLEEPING -> this.target.getSleepingPos().map(sp ->
                    new Vec3(this.target.getDirection().step()).scale(-0.2).add(0, 0.1, -0.15))
                    .orElse(new Vec3(0, 1.3, -0.15));
            default -> new Vec3(0, 1.3, -0.15);
        };
        posTarget = posTarget.add(this.target.position());

        if (!Objects.equals(position(), posTarget)) {
            setYRot(0); setXRot(0);
            setPos(posTarget.x, posTarget.y, posTarget.z);
            setBoundingBox(DIMENSIONS.makeBoundingBox(this.target.position()));
        }
        // updateLeash() intentionally omitted - proxy handles its own leash state
        return false;
    }

    @NotNull
    public LivingEntity getLeashTarget() { return this.target; }

    @Override
    public void tick() {
        if (this.level().isClientSide) return;
        if (proxyUpdate() && !proxyIsRemoved()) proxyRemove();
    }

    public boolean proxyIsRemoved() { return this.isRemoved(); }
    public void proxyRemove() { super.remove(RemovalReason.DISCARDED); }

    @Override public void remove(RemovalReason reason) {}
    @Override public float getHealth() { return 1.0F; }
    @Override public void dropLeash(boolean sendPacket, boolean dropItem) {}
    @Override protected void registerGoals() {}
    @Override protected void doPush(Entity entity) {}
    @Override public void push(Entity entity) {}

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putString("Team", TEAM_NAME);
    }
}
