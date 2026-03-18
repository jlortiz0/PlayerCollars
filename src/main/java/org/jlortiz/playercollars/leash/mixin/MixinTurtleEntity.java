package org.jlortiz.playercollars.leash.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import net.minecraft.nbt.CompoundTag;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Turtle.class)
public abstract class MixinTurtleEntity extends Animal {
    protected MixinTurtleEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void leashplayers$onReadCustomData(CompoundTag nbt, CallbackInfo info) {
        MinecraftServer server = getServer();
        if (server == null) return;
        Team team = server.getScoreboard().getPlayerTeam(getScoreboardName());
        if (team != null && Objects.equals(team.getName(), LeashProxyEntity.TEAM_NAME)) {
            dropLeash(true, true);
            setInvulnerable(false);
            kill();
        }
    }
}
