package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(TurtleEntity.class)
public abstract class MixinTurtleEntity extends AnimalEntity {
    protected MixinTurtleEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void leashplayers$onReadCustomDataFromNbt(CallbackInfo info) {
        MinecraftServer server = getServer();
        if (server == null) return;

        Team team = server.getScoreboard().getScoreHolderTeam(getNameForScoreboard());
        if (team != null && Objects.equals(team.getName(), LeashProxyEntity.TEAM_NAME)) {
            detachLeash();
            setInvulnerable(false);
            kill();
        }
    }
}