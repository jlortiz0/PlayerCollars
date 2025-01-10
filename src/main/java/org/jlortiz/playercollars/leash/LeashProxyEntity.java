package org.jlortiz.playercollars.leash;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public final class LeashProxyEntity extends TurtleEntity {
    private final LivingEntity target;

    private boolean proxyUpdate() {
        if (proxyIsRemoved()) return false;

        if (target == null) return true;
        if (target.getWorld() != getWorld() || !target.isAlive()) return true;

        Vec3d posActual = this.getPos();
        Vec3d posTarget = target.getPos().add(0.0D, 1.3D, -0.15D);

        if (!Objects.equals(posActual, posTarget)) {
            setRotation(0.0F, 0.0F);
            setPos(posTarget.x, posTarget.y, posTarget.z);
            setBoundingBox(getDimensions(EntityPose.DYING).getBoxAt(posTarget));
        }

        // updateLeash();

        return false;
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

    public LeashProxyEntity(LivingEntity target) {
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
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
    }
}