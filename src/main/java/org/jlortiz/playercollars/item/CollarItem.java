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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.screen.CollarDyeScreen;

import java.util.List;

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
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {}

    @Override
    public int getColor(ItemStack itemStack) {
        NbtCompound $$1 = itemStack.getSubNbt("display");
        return $$1 != null && $$1.contains("color", 99) ? $$1.getInt("color") : MapColor.RED.color;
    }

    public int getPawColor(ItemStack itemStack) {
        NbtCompound $$1 = itemStack.getSubNbt("display");
        return $$1 != null && $$1.contains("paw", 99) ? $$1.getInt("paw") : MapColor.BLUE.color;
    }

    public void setPawColor(ItemStack itemStack, int col) {
        NbtCompound $$1 = itemStack.getOrCreateSubNbt("display");
        $$1.putInt("paw", col);
    }

    public @Nullable Pair<UUID, String> getOwner(ItemStack is) {
        NbtCompound $$1 = is.getSubNbt("owner");
        if ($$1 == null || !$$1.contains("uuid") || !$$1.contains("name")) return null;
        return new Pair<>($$1.getUuid("uuid"), $$1.getString("name"));
    }

    public void setOwner(ItemStack is, @Nullable UUID uuid, @Nullable String name) {
        if (uuid == null || name == null) {
            is.removeSubNbt("owner");
            return;
        }
        NbtCompound $$1 = is.getOrCreateSubNbt("owner");
        $$1.putUuid("uuid", uuid);
        $$1.putString("name", name);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public TypedActionResult<ItemStack> use(World p_41432_, PlayerEntity p_41433_, Hand p_41434_) {
        ItemStack is = p_41433_.getStackInHand(p_41434_);
        if (p_41433_.isSneaking() && p_41432_.isClient) {
            MinecraftClient.getInstance().setScreen(new CollarDyeScreen(is, p_41433_.getUuid()));
            return TypedActionResult.success(is, false);
        }
        return TypedActionResult.pass(is);
    }

    @Override
    public Text getName(ItemStack stack) {
        OwnerComponent owner = stack.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
        if (owner != null && owner.ownedName().isPresent())
            return Text.translatable("item.playercollars.collar.named", owner.ownedName().get());
        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, @NotNull TooltipContext context) {
        super.appendTooltip(stack, world, context, tooltip, context);
        if (type.isAdvanced() && !tagless) {
            tooltip.add(Text.translatable("item.playercollars.collar.paw_color", Integer.toHexString(getPawColor(stack))).setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
        OwnerComponent owner = getOwner(stack);
        if (owner != null) {
            tooltip.add(Text.translatable("item.playercollars.collar.owner", owner.name()).setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = Trinket.super.getModifiers(stack, slot, entity, uuid);
        int loyalty = EnchantmentHelper.getLoyalty(stack);
        modifiers.put(PlayerCollarsMod.ATTR_LEASH_DISTANCE, new EntityAttributeModifier(getTranslationKey(), -loyalty, EntityAttributeModifier.Operation.ADDITION));
        return modifiers;
    }

    @Override
    public TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return TrinketEnums.DropRule.KEEP;
    }

    @Override
    public boolean isEnchantable(ItemStack p_41456_) {
        return true;
    }
}
