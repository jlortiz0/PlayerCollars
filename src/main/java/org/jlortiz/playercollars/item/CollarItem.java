package org.jlortiz.playercollars.item;

import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.CollarDyeScreen;
import org.jlortiz.playercollars.util.NbtUtil;
import org.jlortiz.playercollars.enchantment.HealingEnchantment;
import org.jlortiz.playercollars.enchantment.SpikedEnchantment;
import org.jlortiz.playercollars.enchantment.TightLeashEnchantment;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.List;
import java.util.UUID;

public class CollarItem extends Item implements DyeableLeatherItem {
    private static final int DEFAULT_COLOR = 0xA00000;
    private static final int DEFAULT_PAW_COLOR = 0x0000A0;
    public final boolean tagless;

    public CollarItem(boolean tagless) {
        super(new Item.Properties().stacksTo(1));
        this.tagless = tagless;
    }

    public static boolean isAcceptableEnchantment(Enchantment enchantment) {
        return enchantment instanceof BindingCurseEnchantment ||
               enchantment instanceof HealingEnchantment ||
               enchantment instanceof TightLeashEnchantment ||
               enchantment instanceof SpikedEnchantment ||
               enchantment instanceof MendingEnchantment;
    }

    /** Build an ICurio capability for a given stack of this item. */
    public ICurio buildCurio(ItemStack stack) {
        return new ICurio() {
            @Override
            public ItemStack getStack() { return stack; }

            @Override
            public void curioTick(SlotContext slotContext) {
                LivingEntity entity = slotContext.entity();
                if (entity.level().isClientSide) return;
                int healingLevel = EnchantmentHelper.getItemEnchantmentLevel(
                        PlayerCollarsMod.HEALING_ENCHANTMENT.get(), stack);
                if (healingLevel > 0) {
                    HealingEnchantment.tick(entity, stack, healingLevel);
                }
            }

            @Override
            public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
                var modifiers = ICurio.super.getAttributeModifiers(slotContext, uuid);
                int loyalty = EnchantmentHelper.getItemEnchantmentLevel(
                        PlayerCollarsMod.TIGHT_LEASH_ENCHANTMENT.get(), stack);
                modifiers.put(PlayerCollarsMod.ATTR_LEASH_DISTANCE.get(),
                        new AttributeModifier(uuid, CollarItem.this.getDescriptionId(), -loyalty,
                                AttributeModifier.Operation.ADDITION));
                return modifiers;
            }

            @Override
            public ICurio.DropRule getDropRule(SlotContext slotContext,
                    net.minecraft.world.damagesource.DamageSource source, int lootingLevel, boolean recentlyHit) {
                return ICurio.DropRule.ALWAYS_KEEP;
            }

            @Override
            public boolean canEquipFromUse(SlotContext slotContext) {
                // Let use() handle shift+right-click for the dye screen
                return !slotContext.entity().isCrouching();
            }
        };
    }

    @Override
    public boolean isEnchantable(net.minecraft.world.item.ItemStack stack) { return true; }

    @Override
    public int getEnchantmentValue() { return 60; }
    @Override
    public boolean isFoil(ItemStack stack) { return false; }

    @Override
    public int getColor(ItemStack stack) { return NbtUtil.getColor(stack, DEFAULT_COLOR); }

    @Override
    public void setColor(ItemStack stack, int color) { NbtUtil.setColor(stack, color); }

    public int getTagColor(ItemStack stack) { return NbtUtil.getTagColor(stack, DEFAULT_PAW_COLOR); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && world.isClientSide) {
            Minecraft.getInstance().setScreen(new CollarDyeScreen(stack, player.getUUID()));
            return InteractionResultHolder.success(stack);
        }
        return super.use(world, player, hand);
    }

    @Override
    public Component getName(ItemStack stack) {
        var owner = NbtUtil.getDeedOwner(stack);
        if (owner != null && owner.ownedName().isPresent())
            return Component.translatable("item.playercollars.collar.named", owner.ownedName().get());
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        if (context.isAdvanced() && !this.tagless)
            tooltip.add(Component.translatable("item.playercollars.collar.paw_color",
                    Integer.toHexString(getTagColor(stack))).setStyle(Style.EMPTY.withColor(0x808080)));
        var owner = NbtUtil.getDeedOwner(stack);
        if (owner != null)
            tooltip.add(Component.translatable("item.playercollars.collar.owner", owner.name())
                    .setStyle(Style.EMPTY.withColor(0x808080)));
    }

    @Override
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(
            net.minecraft.world.item.ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.nbt.CompoundTag nbt) {
        return top.theillusivec4.curios.api.CuriosApi.createCurioProvider(buildCurio(stack));
    }
}