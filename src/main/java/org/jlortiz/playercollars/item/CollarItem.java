package org.jlortiz.playercollars.item;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketEnums;
import dev.emi.trinkets.api.TrinketItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.BindingCurseEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.LoyaltyEnchantment;
import net.minecraft.enchantment.MendingEnchantment;
import net.minecraft.enchantment.ThornsEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

public class CollarItem extends TrinketItem implements DyeableItem {
    private static final int DEFAULT_COLOR = MapColor.RED.color;
    private static final int DEFAULT_PAW_COLOR = MapColor.BLUE.color;
    public final boolean tagless;

    public CollarItem(boolean tagless) {
        super(new Item.Settings().maxCount(1));
        this.tagless = tagless;
    }

    public static boolean isAcceptableEnchantment(Enchantment enchantment) {
        return enchantment instanceof BindingCurseEnchantment ||
               enchantment instanceof LoyaltyEnchantment ||
               enchantment instanceof ThornsEnchantment ||
               enchantment instanceof MendingEnchantment;
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
        if (entity.getWorld().isClient)
            return;

        if (EnchantmentHelper.getLevel(Enchantments.MENDING, stack) > 0) {
            var owner = NbtUtil.getDeedOwner(stack);

            // TODO 2026-03-04 (solonovamax): why is there a check for if the owner is equal to the entity here?
            if (owner == null || owner.uuid().equals(entity.getUuid()))
                return;

            PlayerEntity own = entity.getWorld().getPlayerByUuid(owner.uuid());
            if (own != null && own.squaredDistanceTo(entity) < 256 /* 16^2 */) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0, false, false, false));
            }
        }
    }

    @Override
    public int getColor(ItemStack itemStack) {
        return NbtUtil.getColor(itemStack, CollarItem.DEFAULT_COLOR);
    }

    public int getTagColor(ItemStack stack) {
        return NbtUtil.getTagColor(stack, CollarItem.DEFAULT_PAW_COLOR);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.isSneaking() && world.isClient) {
            MinecraftClient.getInstance().setScreen(new CollarDyeScreen(stack, player.getUuid()));
            return TypedActionResult.success(stack, false);
        }
        return super.use(world, player, hand);
    }

    @Override
    public Text getName(ItemStack stack) {
        var owner = NbtUtil.getDeedOwner(stack);
        if (owner != null && owner.ownedName().isPresent())
            return Text.translatable("item.playercollars.collar.named", owner.ownedName().get());
        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, @NotNull TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (context.isAdvanced() && !this.tagless) {
            tooltip.add(Text.translatable("item.playercollars.collar.paw_color", Integer.toHexString(getTagColor(stack)))
                    .setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
        var owner = NbtUtil.getDeedOwner(stack);
        if (owner != null) {
            tooltip.add(Text.translatable("item.playercollars.collar.owner", owner.name()).setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        var modifiers = super.getModifiers(stack, slot, entity, uuid);
        var loyalty = EnchantmentHelper.getLoyalty(stack);
        modifiers.put(PlayerCollarsMod.ATTR_LEASH_DISTANCE, new EntityAttributeModifier(uuid, getTranslationKey(), -loyalty, EntityAttributeModifier.Operation.ADDITION));
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
