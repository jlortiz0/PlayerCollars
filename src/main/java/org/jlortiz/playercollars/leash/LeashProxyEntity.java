package org.jlortiz.playercollars.leash;

import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Objects;

public final class LeashProxyEntity extends Turtle {
    private final LivingEntity target;

    private boolean proxyUpdate() {
        if (proxyIsRemoved()) return false;

        if (target == null) return true;
        if (target.getLevel() != getLevel() || !target.isAlive()) return true;

        Vec3 posActual = this.position();
        Vec3 posTarget = target.position().add(0.0D, 1.3D, -0.15D);

        if (!Objects.equals(posActual, posTarget)) {
            setRot(0.0F, 0.0F);
            setPos(posTarget.x(), posTarget.y(), posTarget.z());
            setBoundingBox(getDimensions(Pose.DYING).makeBoundingBox(posTarget));
        }

        tickLeash();

        return false;
    }

    @Override
    public void tick() {
        if (this.getLevel().isClientSide) return;
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
        super(EntityType.TURTLE, target.getLevel());

        this.target = target;

        setHealth(1.0F);
        setInvulnerable(true);

        setBaby(true);
        setInvisible(true);
        noPhysics = true;

        MinecraftServer server = getServer();
        if (server != null) {
            ServerScoreboard scoreboard = server.getScoreboard();

            PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
            if (team == null) {
                team = scoreboard.addPlayerTeam(TEAM_NAME);
            }
            if (team.getCollisionRule() != PlayerTeam.CollisionRule.NEVER) {
                team.setCollisionRule(PlayerTeam.CollisionRule.NEVER);
            }

            scoreboard.addPlayerToTeam(getScoreboardName(), team);
        }
    }

    @Override
    public float getHealth() {
        return 1.0F;
    }

    @Override
    public void dropLeash(boolean sendPacket, boolean dropItem) {
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void playerTouch(Player player) {
    }
}