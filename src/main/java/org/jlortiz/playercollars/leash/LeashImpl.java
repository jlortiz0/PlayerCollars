package org.jlortiz.playercollars.leash;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public interface LeashImpl {
    ActionResult leashplayers$interact(PlayerEntity plr, Hand hand);
}
