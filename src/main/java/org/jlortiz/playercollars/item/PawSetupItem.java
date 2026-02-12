package org.jlortiz.playercollars.item;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.PawsSelectScreen;

import java.util.Optional;

public class PawSetupItem extends Item {
    public PawSetupItem() {
        super(new Settings().maxCount(1));
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return stack;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack is = user.getStackInHand(hand);
        if (!user.isSneaking() || !world.isClient) return TypedActionResult.pass(is);
        return switch (useOnEntity(is, user, user, hand)) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> TypedActionResult.success(is);
            case CONSUME, CONSUME_PARTIAL -> TypedActionResult.consume(is);
            case PASS -> TypedActionResult.pass(is);
            case FAIL -> TypedActionResult.fail(is);
        };
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof PlayerEntity player) || !user.getWorld().isClient) return ActionResult.PASS;
        Optional<TrinketComponent> optComponent = TrinketsApi.getTrinketComponent(player);
        if (optComponent.isEmpty()) return ActionResult.PASS;
        TrinketComponent component = optComponent.get();

        ItemStack collarStack = PlayerCollarsMod.filterStacksByOwner(component.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)), user.getUuid(), player.getUuid());
        if (collarStack == null) {
            user.sendMessage(Text.translatable("item.playercollars.paw_configurator.no_set_non_owner").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        MinecraftClient.getInstance().setScreen(new PawsSelectScreen(player));
        return ActionResult.SUCCESS;
    }
}
