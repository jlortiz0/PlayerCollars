package org.jlortiz.playercollars.item;

import io.wispforest.accessories.api.AccessoryItem;
import io.wispforest.accessories.api.DropRule;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.slot.SlotReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.CollarDyeScreen;

import java.util.List;

public class CollarItem extends AccessoryItem {
    public static final RegistryKey<Item> REGISTRY_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "collar"));
    public static final RegistryKey<Item> TAGLESS_REGISTRY_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "tagless_collar"));

    public CollarItem(boolean tagless) {
        super(new Item.Settings().maxCount(1).registryKey(tagless ? TAGLESS_REGISTRY_KEY : REGISTRY_KEY)
                .component(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(100))
                .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(MapColor.RED.color, false))
                .component(DataComponentTypes.MAP_COLOR, new MapColorComponent(MapColor.BLUE.color)));
    }

    public static int getColor(ItemStack itemStack) {
        DyedColorComponent $$1 = itemStack.get(DataComponentTypes.DYED_COLOR);
        return $$1 != null ? $$1.rgb() : MapColor.RED.color | 0xFF000000;
    }

    public static int getPawColor(ItemStack itemStack) {
        MapColorComponent $$1 = itemStack.get(DataComponentTypes.MAP_COLOR);
        return $$1 != null ? $$1.rgb() : MapColor.BLUE.color;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ActionResult use(World p_41432_, PlayerEntity p_41433_, Hand p_41434_) {
        ItemStack is = p_41433_.getStackInHand(p_41434_);
        if (p_41433_.isSneaking() && p_41432_.isClient) {
            MinecraftClient.getInstance().setScreen(new CollarDyeScreen(is, p_41433_.getUuid()));
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    public Text getName(ItemStack stack) {
        OwnerComponent owner = stack.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
        if (owner != null && owner.ownedName().isPresent())
            return Text.translatable("item.playercollars.collar.named", owner.ownedName().get());
        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        if (type.isAdvanced()) {
            tooltip.add(Text.translatable("item.playercollars.collar.paw_color", Integer.toHexString(getPawColor(stack))).setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
        OwnerComponent owner = stack.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
        if (owner != null) {
            tooltip.add(Text.translatable("item.playercollars.collar.owner", owner.name()).formatted(Formatting.GRAY));
        }
    }

    @Override
    public void getDynamicModifiers(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        EnchantmentHelper.applyAttributeModifiers(stack, AttributeModifierSlot.ANY, builder::addExclusive);
    }

    @Override
    public DropRule getDropRule(ItemStack stack, SlotReference reference, DamageSource source) {
        return DropRule.KEEP;
    }
}
