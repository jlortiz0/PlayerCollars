package org.jlortiz.playercollars.item;

import io.wispforest.accessories.api.AccessoriesCapability;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
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
        super(new Settings().maxCount(1).registryKey(REGISTRY_KEY));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack is = user.getStackInHand(hand);
        if (!user.isSneaking()) return ActionResult.PASS;
        return useOnEntity(is, user, user, hand);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof PlayerEntity player)) return ActionResult.PASS;

        AccessoriesCapability cap = AccessoriesCapability.get(player);
        if (cap == null) return ActionResult.PASS;

        ItemStack collarStack = PlayerCollarsMod.filterStacksByOwner(cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.COLLAR_TAG)), user.getUuid(), player.getUuid());
        if (collarStack == null) {
            if (user.getWorld().isClient) {
                user.sendMessage(Text.translatable("item.playercollars.paw_configurator.no_set_non_owner").formatted(Formatting.RED), true);
            }
            return ActionResult.FAIL;
        }

        // ✨ Musia's Dual Magic Toggle! ✨
        if (user.isSneaking()) {
            if (!user.getWorld().isClient) {
                // Check if the owner is holding food in their off-hand to toggle Diet Control
                if (user.getOffHandStack().contains(DataComponentTypes.FOOD)) {
                    boolean isDiet = collarStack.getOrDefault(PlayerCollarsMod.DIET_CONTROL_COMPONENT_TYPE, false);
                    collarStack.set(PlayerCollarsMod.DIET_CONTROL_COMPONENT_TYPE, !isDiet);
                    user.sendMessage(Text.literal("Pet diet control set to: " + !isDiet).formatted(Formatting.LIGHT_PURPLE), true);
                } else {
                    // Otherwise, toggle the Crawl state
                    boolean isCrawling = collarStack.getOrDefault(PlayerCollarsMod.FORCED_CRAWL_COMPONENT_TYPE, false);
                    collarStack.set(PlayerCollarsMod.FORCED_CRAWL_COMPONENT_TYPE, !isCrawling);
                    user.sendMessage(Text.literal("Pet forced crawl set to: " + !isCrawling).formatted(Formatting.LIGHT_PURPLE), true);
                }
            }
            return ActionResult.SUCCESS;
        } else {
            if (user.getWorld().isClient) {
                openPawsScreen(player);
            }
            return ActionResult.SUCCESS;
        }
    }

    @Environment(EnvType.CLIENT)
    private void openPawsScreen(PlayerEntity player) {
        MinecraftClient.getInstance().setScreen(new PawsSelectScreen(player));
    }
}