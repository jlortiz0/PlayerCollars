package org.jlortiz.playercollars.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Predicate;

public final class LeashedUtil {
    private LeashedUtil() {}

    public static List<Mob> collectLeashablesAround(Level world, BlockPos pos, Predicate<Mob> predicate) {
        AABB box = new AABB(pos).inflate(7.0);
        return world.getEntitiesOfClass(Mob.class, box, predicate);
    }
}
