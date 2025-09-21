package org.jlortiz.playercollars.item;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PacketLookAtLerped;

import java.util.List;

public class ClickerItem extends Item {
    public static final RegistryKey<Item> REGISTRY_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "clicker"));
    public ClickerItem() {
        super(new Item.Settings().maxCount(1).registryKey(REGISTRY_KEY)
                .component(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(45)));
    }

    @Override
    public ActionResult use(World p_41432_, PlayerEntity p_41433_, Hand p_41434_) {
        p_41433_.setCurrentHand(p_41434_);
        if (!p_41432_.isClient) {
            double distance = p_41433_.getAttributeValue(PlayerCollarsMod.ATTR_CLICKER_DISTANCE);
            if (distance > 0) {
                List<ServerPlayerEntity> plrs = ((ServerWorld) p_41432_).getPlayers((p) -> !p.isPartOf(p_41433_) && p.isInRange(p_41433_, distance));
                PacketLookAtLerped packet = new PacketLookAtLerped(p_41433_);
                for (ServerPlayerEntity p : plrs) {
                    AccessoriesCapability cap = AccessoriesCapability.get(p);
                    if (cap != null) {
                        ItemStack is = PlayerCollarsMod.filterStacksByOwner(cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.COLLAR_TAG)), p_41433_.getUuid(), p.getUuid());
                        if (is != null) {
                            ServerPlayNetworking.send(p, packet);
                        }
                    }
                }
            }
            p_41432_.playSoundFromEntity(null, p_41433_, PlayerCollarsMod.CLICKER_ON, SoundCategory.PLAYERS, 1, 1);
        }
        return ActionResult.FAIL;
    }

    @Override
    public int getMaxUseTime(ItemStack p_41454_, LivingEntity user) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean onStoppedUsing(ItemStack p_41412_, World p_41413_, LivingEntity p_41414_, int p_41415_) {
        if (!p_41413_.isClient) {
            p_41413_.playSoundFromEntity(null, p_41414_, PlayerCollarsMod.CLICKER_OFF, SoundCategory.PLAYERS, 1, 1);
        }
        return false;
    }
}
