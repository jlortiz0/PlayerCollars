package org.jlortiz.playercollars.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.client.screen.DeedItemScreen;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.List;

public class DeedItem extends Item {
    public DeedItem() {
        super(new Settings().maxCount(1));
    }

    @Environment(EnvType.CLIENT)
    private static void openTheScreen(ItemStack is, PlayerEntity plr) {
        MinecraftClient.getInstance().setScreen(new DeedItemScreen(is, plr));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack is = player.getStackInHand(hand);

        if (world.isClient) {
            OwnerComponent owner = NbtUtil.getDeedOwner(is);
            if (owner != null && owner.owned().isEmpty()) {
                if (owner.uuid().equals(player.getUuid())) {
                    player.sendMessage(Text.translatable("item.playercollars.deed_of_ownership.no_self_own"), true);
                    return TypedActionResult.pass(is);
                }
                openTheScreen(is, player);
                return TypedActionResult.success(is);
            }
        } else if (NbtUtil.getDeedOwner(is) == null) {
            NbtUtil.setDeedOwner(is, new OwnerComponent(player.getUuid(), player.getName().getString()));
            player.sendMessage(Text.translatable("item.playercollars.deed_of_ownership.filled_out"), true);
            return TypedActionResult.consume(is);
        }
        return TypedActionResult.pass(is);
    }

    @Override
    public Text getName(ItemStack stack) {
        OwnerComponent owner = NbtUtil.getDeedOwner(stack);
        if (owner != null)
            return Text.translatable(getTranslationKey(stack) + ".filled");
        else
            return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        OwnerComponent owner = NbtUtil.getDeedOwner(stack);
        if (owner != null) {
            tooltip.add(Text.translatable("item.playercollars.collar.owner", owner.name()).formatted(Formatting.GRAY));
        }
    }
}
