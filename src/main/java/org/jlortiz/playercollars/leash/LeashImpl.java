package org.jlortiz.playercollars.leash;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public interface LeashImpl {
    InteractionResult leashplayers$interact(Player plr, InteractionHand hand);
}
