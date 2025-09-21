package org.jlortiz.playercollars.item;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.PawsSelectScreen;

public class PawSetupItem extends Item {
    public static final RegistryKey<Item> REGISTRY_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "paw_configurator"));

    public PawSetupItem() {
        super(new Item.Settings().maxCount(1).registryKey(REGISTRY_KEY));
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return stack;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack is = user.getStackInHand(hand);
        if (!user.isSneaking() || !world.isClient) return ActionResult.PASS;
        return useOnEntity(is, user, user, hand);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof PlayerEntity player) || !user.getWorld().isClient) return ActionResult.PASS;
        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return ActionResult.PASS;

        ItemStack collarStack = PlayerCollarsMod.filterStacksByOwner(cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.COLLAR_TAG)), user.getUuid(), player.getUuid());
        if (collarStack == null) {
            user.sendMessage(Text.translatable("item.playercollars.paw_configurator.no_set_non_owner").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        MinecraftClient.getInstance().setScreen(new PawsSelectScreen(player));
        return ActionResult.SUCCESS;
    }
}
