package org.jlortiz.playercollars.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.DeedItemScreen;

public class DeedItem extends Item {
    public static final RegistryKey<Item> REGISTRY_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "deed_of_ownership"));

    public DeedItem() {
        super(new Settings().maxCount(1).registryKey(REGISTRY_KEY));
    }

    @Override
    public ActionResult use(World p_41432_, PlayerEntity p_41433_, Hand p_41434_) {
        ItemStack is = p_41433_.getStackInHand(p_41434_);
        if (p_41432_.isClient) {
            OwnerComponent owner = is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
            if (owner != null && owner.owned().isEmpty()) {
                if (owner.uuid().equals(p_41433_.getUuid())) {
                    p_41433_.sendMessage(Text.translatable("item.playercollars.deed_of_ownership.no_self_own"), true);
                    return ActionResult.PASS;
                }
                openTheScreen(is, p_41433_);
                return ActionResult.CONSUME;
            }
        } else if (is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE) == null) {
            is.set(PlayerCollarsMod.OWNER_COMPONENT_TYPE, new OwnerComponent(p_41433_.getUuid(), p_41433_.getName().getString()));
            p_41433_.sendMessage(Text.translatable("item.playercollars.deed_of_ownership.filled_out"), true);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Environment(EnvType.CLIENT)
    private void openTheScreen(ItemStack is, PlayerEntity plr) {
        MinecraftClient.getInstance().setScreen(new DeedItemScreen(is, plr));
    }

    @Override
    public Text getName(ItemStack stack) {
        if (stack.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE) != null)
            return Text.translatable("item.playercollars.deed_of_ownership.filled");
        return super.getName(stack);
    }
}
