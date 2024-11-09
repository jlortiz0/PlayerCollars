package org.jlortiz.playercollars.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.DyingStationScreen;

import java.util.List;
import java.util.UUID;

public class CollarItem extends TrinketItem implements DyeableItem {

    public CollarItem() {
        super(new Item.Settings().maxCount(1));
    }

    // FIXME: the enchantmnet tags didn't work. figure out how to properly roll enchantments for non-armor items
    @Override
    public int getEnchantability() {
        return 40;
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity.getWorld().isClient) return;
        if (EnchantmentHelper.getLevel(Enchantments.MENDING, stack) > 0) {
            Pair<UUID, String> owner = this.getOwner(stack);
            if (owner == null || owner.getLeft().equals(entity.getUuid())) return;
            PlayerEntity own = entity.getWorld().getPlayerByUuid(owner.getLeft());
            if (own != null && own.distanceTo(entity) < 16) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0, false, false, false));
            }
        }
    }

    @Override
    public ItemStack getDefaultStack() {
        return getInstance(TagType.GOLD);
    }

    public enum TagType {
        GOLD(0xFDF55F, Items.GOLD_INGOT),
        IRON(0xD8D8D8, Items.IRON_INGOT),
        COPPER(0xE77C56, Items.COPPER_INGOT),
        NETHERITE(0x5A575A, Items.NETHERITE_INGOT),
        DIAMOND(0x4AEDD9, Items.DIAMOND),
        STONE(0x6B6B6B, ItemTags.STONE_TOOL_MATERIALS),
        WOOD(0x907549, ItemTags.PLANKS),
        AMETHYST(0xCFA0F3, Items.AMETHYST_SHARD),
        EMERALD(0x17DD62, Items.EMERALD);
        public final int color;
        public final Ingredient ingredient;
        TagType(int color, TagKey<Item> tag) {
            this.color = color;
            this.ingredient = Ingredient.fromTag(tag);
        }

        TagType(int color, Item item) {
            this.color = color;
            this.ingredient = Ingredient.ofItems(item);
        }
    }

    @Override
    public int getColor(ItemStack itemStack) {
        NbtCompound $$1 = itemStack.getSubNbt("display");
        return $$1 != null && $$1.contains("color", 99) ? $$1.getInt("color") : MapColor.RED.color;
    }

    public int getTagColor(ItemStack itemStack) {
        NbtCompound $$1 = itemStack.getSubNbt("display");
        if ($$1 == null || !$$1.contains("tagType")) {
            return TagType.GOLD.color;
        }
        int $$2 = $$1.getInt("tagType");
        return $$2 >= TagType.values().length ? 0 : TagType.values()[$$2].color;
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

    public static ItemStack getInstance(TagType tag) {
        ItemStack is = new ItemStack(PlayerCollarsMod.COLLAR_ITEM);
        NbtCompound $$1 = is.getOrCreateSubNbt("display");
        $$1.putInt("tagType", tag.ordinal());
        return is;
    }

    public static ItemStack getInstance(TagType tag, int paw) {
        ItemStack is = new ItemStack(PlayerCollarsMod.COLLAR_ITEM);
        NbtCompound $$1 = is.getOrCreateSubNbt("display");
        $$1.putInt("paw", paw);
        $$1.putInt("tagType", tag.ordinal());
        return is;
    }

    @Override
    public TypedActionResult<ItemStack> use(World p_41432_, PlayerEntity p_41433_, Hand p_41434_) {
        TypedActionResult<ItemStack> ir = super.use(p_41432_, p_41433_, p_41434_);
        if (ir.getResult() == ActionResult.PASS && p_41433_.isSneaking() && p_41432_.isClient) {
            MinecraftClient.getInstance().setScreen(new DyingStationScreen(ir.getValue(), p_41433_.getUuid()));
            return new TypedActionResult<>(ActionResult.SUCCESS, ir.getValue());
        }
        return ir;
    }

    @Override
    public void appendTooltip(ItemStack p_41421_, @Nullable World p_41422_, List<Text> p_41423_, @NotNull TooltipContext p_41424_) {
        super.appendTooltip(p_41421_, p_41422_, p_41423_, p_41424_);
        if (p_41424_.isAdvanced()) {
            p_41423_.add(Text.translatable("item.playercollars.collar.paw_color", Integer.toHexString(getPawColor(p_41421_))).setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
        Pair<UUID, String> owner = getOwner(p_41421_);
        if (owner != null) {
            p_41423_.add(Text.translatable("item.playercollars.collar.owner", owner.getRight()).setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
    }

    @Override
    public Text getName(ItemStack p_41458_) {
        NbtCompound $$1 = p_41458_.getSubNbt("display");
        if ($$1 == null || !$$1.contains("tagType")) {
            return super.getName(p_41458_);
        }
        int $$2 = $$1.getInt("tagType");
        if ($$2 >= TagType.values().length) {
            return super.getName(p_41458_);
        }
        return Text.translatable("item.playercollars.tag." + TagType.values()[$$2].name()).append(" ").append(super.getName(p_41458_));
    }

    @Override
    public boolean isEnchantable(ItemStack p_41456_) {
        return true;
    }
}
