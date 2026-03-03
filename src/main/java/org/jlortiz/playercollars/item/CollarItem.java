package org.jlortiz.playercollars.item;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketEnums;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.CollarDyeScreen;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.List;
import java.util.UUID;

public class CollarItem extends Item implements DyeableItem, Trinket {
    public final boolean tagless;

    public CollarItem(boolean tagless) {
        super(new Item.Settings().maxCount(1));
        this.tagless = tagless;
        TrinketsApi.registerTrinket(this, this);
    }

    @Override
    public int getEnchantability() {
        return 60;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
    }

    @Override
    public int getColor(ItemStack itemStack) {
        NbtCompound $$1 = itemStack.getSubNbt("display");
        return $$1 != null && $$1.contains("color", 99) ? $$1.getInt("color") : MapColor.RED.color;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack is = player.getStackInHand(hand);
        if (player.isSneaking() && world.isClient) {
            MinecraftClient.getInstance().setScreen(new CollarDyeScreen(is, player.getUuid()));
            return TypedActionResult.success(is, false);
        }
        return TypedActionResult.pass(is);
    }

    @Override
    public Text getName(ItemStack stack) {
        var owner = NbtUtil.getOwner(stack);
        if (owner != null)
            return Text.translatable("item.playercollars.collar.named", owner.getRight());
        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, @NotNull TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (context.isAdvanced() && !this.tagless) {
            tooltip.add(Text.translatable("item.playercollars.collar.paw_color", Integer.toHexString(NbtUtil.getPawColor(stack)))
                    .setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
        var owner = NbtUtil.getOwner(stack);
        if (owner != null) {
            tooltip.add(Text.translatable("item.playercollars.collar.owner", owner.getRight()).setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = Trinket.super.getModifiers(stack, slot, entity, uuid);
        int loyalty = EnchantmentHelper.getLoyalty(stack);
        modifiers.put(PlayerCollarsMod.ATTR_LEASH_DISTANCE, new EntityAttributeModifier(getTranslationKey(), -loyalty, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(PlayerCollarsMod.ATTR_CLICKER_DISTANCE, new EntityAttributeModifier(getTranslationKey(), loyalty, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }

    @Override
    public TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return TrinketEnums.DropRule.KEEP;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
}
