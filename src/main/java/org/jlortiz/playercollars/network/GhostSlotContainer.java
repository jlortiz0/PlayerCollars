package org.jlortiz.playercollars.network;

import net.minecraft.screen.slot.Slot;

import java.util.stream.Stream;

public interface GhostSlotContainer {
    boolean isGhostSlot(int id);

    Stream<Slot> getGhostSlots();
}
