package org.jlortiz.playercollars.util;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public final class LeashedUtil {
    private LeashedUtil() {
    }

    public static List<MobEntity> collectLeashablesAround(World world, BlockPos pos, Predicate<MobEntity> predicate) {
        Box box = new Box(pos, pos).expand(7.0);
        return world.getEntitiesByClass(MobEntity.class, box, predicate);
    }
}
