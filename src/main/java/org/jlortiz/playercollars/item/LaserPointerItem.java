package org.jlortiz.playercollars.item;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PacketLookAtLerped;

import java.util.List;
import java.util.Optional;

public class LaserPointerItem extends Item {
    // 💖 We declare the RegistryKey for our new custom enchantment!
    public static final RegistryKey<Enchantment> LASER_REACH_KEY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(PlayerCollarsMod.MOD_ID, "laser_reach"));

    public LaserPointerItem(Settings settings) {
        // ✨ We append the EnchantableComponent so the table accepts it!
        // 15 is the enchantability value (similar to iron tools!)
        super(settings.component(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(15)));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            ItemStack stack = user.getStackInHand(hand);

            // ✨ Calculate enchanted distance (Base 32 blocks, +32 per level)
            int reachLevel = 0;
            Optional<RegistryEntry.Reference<Enchantment>> enchantEntry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(LASER_REACH_KEY);
            if (enchantEntry.isPresent()) {
                reachLevel = net.minecraft.enchantment.EnchantmentHelper.getLevel(enchantEntry.get(), stack);
            }
            double maxDistance = 32.0 * (4.0 + reachLevel);

// Cast the ray!
            HitResult hit = user.raycast(maxDistance, 0.0F, false);

            if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
                Vec3d targetPos = hit.getPos();

                // ✨ Get ALL players in the world! (Infinite distance)
                List<ServerPlayerEntity> plrs = ((ServerWorld) world).getPlayers(p -> !p.isPartOf(user));
                PacketLookAtLerped packet = new PacketLookAtLerped(targetPos.x, targetPos.y, targetPos.z);

                for (ServerPlayerEntity p : plrs) {
                    AccessoriesCapability cap = AccessoriesCapability.get(p);
                    if (cap != null) {
                        ItemStack collar = PlayerCollarsMod.filterStacksByOwner(cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.COLLAR_TAG)), user.getUuid(), p.getUuid());
                        if (collar != null) {
                            ServerPlayNetworking.send(p, packet);
                        }
                    }
                }
            }
        }
        return ActionResult.SUCCESS;
    }
}