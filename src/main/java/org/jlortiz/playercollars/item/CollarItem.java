package org.jlortiz.playercollars.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.DyingStationScreen;
import org.jlortiz.playercollars.PlayerCollarsMod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CollarItem extends Item implements DyeableLeatherItem, ICurio, ICapabilityProvider {

    private static final Set<Enchantment> ALLOWED_ENCHANTMENTS = new HashSet<>();
    static {
        ALLOWED_ENCHANTMENTS.add(Enchantments.LOYALTY);
        ALLOWED_ENCHANTMENTS.add(Enchantments.BINDING_CURSE);
        ALLOWED_ENCHANTMENTS.add(Enchantments.THORNS);
        ALLOWED_ENCHANTMENTS.add(Enchantments.MENDING);
    }
    public CollarItem() {
        super(new Item.Properties().stacksTo(1).tab(PlayerCollarsMod.TAB));
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 40;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return ALLOWED_ENCHANTMENTS.contains(enchantment);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return this;
    }

    @Override
    public ItemStack getStack() {
        return new ItemStack(this);
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        LivingEntity ent = slotContext.entity();
        if (ent.level.isClientSide) return;
        Optional<SlotResult> sr = CuriosApi.getCuriosHelper().findCurio(ent, slotContext.identifier(), slotContext.index());
        if (sr.isEmpty()) return;
        ItemStack is = sr.get().stack();
        if (this.getEnchantmentLevel(is, Enchantments.MENDING) == 0) return;
        Pair<UUID, String> owner = this.getOwner(is);
        if (owner == null) return;
        Player own = ent.level.getPlayerByUUID(owner.getFirst());
        if (own != null && own.distanceTo(ent) < 16) {
            ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, false));
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
        if (capability == CuriosCapability.ITEM) {
            return LazyOptional.of(() -> (T) this);
        }
        return LazyOptional.empty();
    }

    public enum TagType {
        GOLD(0xFDF55F, Tags.Items.INGOTS_GOLD),
        IRON(0xD8D8D8, Tags.Items.INGOTS_IRON),
        COPPER(0xE77C56, Tags.Items.INGOTS_COPPER),
        NETHERITE(0x5A575A, Tags.Items.INGOTS_NETHERITE),
        DIAMOND(0x4AEDD9, Tags.Items.GEMS_DIAMOND),
        STONE(0x6B6B6B, Tags.Items.STONE),
        WOOD(0x907549, ItemTags.PLANKS),
        AMETHYST(0xCFA0F3, Tags.Items.GEMS_AMETHYST),
        EMERALD(0x17DD62, Tags.Items.GEMS_EMERALD);
        public final int color;
        public final TagKey<Item> item;
        TagType(int color, TagKey<Item> item) {
            this.color = color;
            this.item = item;
        }

        public static Ingredient getIngredient() {
            return Ingredient.fromValues(Stream.of(Arrays.stream(TagType.values()).map((i) -> new Ingredient.TagValue(i.item))).flatMap(Function.identity()));
        }
    }

    @Override
    public int getColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        return $$1 != null && $$1.contains("color", 99) ? $$1.getInt("color") : MaterialColor.COLOR_RED.col;
    }

    public int getTagColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        if ($$1 == null || !$$1.contains("tagType")) {
            return TagType.GOLD.color;
        }
        int $$2 = $$1.getInt("tagType");
        return $$2 >= TagType.values().length ? 0 : TagType.values()[$$2].color;
    }

    public int getPawColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        return $$1 != null && $$1.contains("paw", 99) ? $$1.getInt("paw") : MaterialColor.COLOR_BLUE.col;
    }

    public void setPawColor(ItemStack itemStack, int col) {
        CompoundTag $$1 = itemStack.getOrCreateTagElement("display");
        $$1.putInt("paw", col);
    }

    public @Nullable Pair<UUID, String> getOwner(ItemStack is) {
        CompoundTag $$1 = is.getTagElement("owner");
        if ($$1 == null || !$$1.contains("uuid") || !$$1.contains("name")) return null;
        return new Pair<>($$1.getUUID("uuid"), $$1.getString("name"));
    }

    public void setOwner(ItemStack is, @Nullable UUID uuid, @Nullable String name) {
        if (uuid == null || name == null) {
            is.removeTagKey("owner");
            return;
        }
        CompoundTag $$1 = is.getOrCreateTagElement("owner");
        $$1.putUUID("uuid", uuid);
        $$1.putString("name", name);
    }

    public static ItemStack getInstance(TagType tag) {
        ItemStack is = new ItemStack(PlayerCollarsMod.COLLAR_ITEM.get());
        CompoundTag $$1 = is.getOrCreateTagElement("display");
        $$1.putInt("tagType", tag.ordinal());
        return is;
    }

    public static ItemStack getInstance(TagType tag, int paw) {
        ItemStack is = new ItemStack(PlayerCollarsMod.COLLAR_ITEM.get());
        CompoundTag $$1 = is.getOrCreateTagElement("display");
        $$1.putInt("paw", paw);
        $$1.putInt("tagType", tag.ordinal());
        return is;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        InteractionResultHolder<ItemStack> ir = super.use(p_41432_, p_41433_, p_41434_);
        if (ir.getResult() == InteractionResult.PASS && p_41433_.isCrouching() && p_41432_.isClientSide) {
            Minecraft.getInstance().setScreen(new DyingStationScreen(ir.getObject(), p_41433_.getUUID()));
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, ir.getObject());
        }
        return ir;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        if (p_41424_.isAdvanced()) {
            p_41423_.add(Component.translatable("item.playercollars.collar.paw_color", Integer.toHexString(getPawColor(p_41421_))).withStyle(ChatFormatting.GRAY));
        }
        Pair<UUID, String> owner = getOwner(p_41421_);
        if (owner != null) {
            p_41423_.add(Component.translatable("item.playercollars.collar.owner", owner.getSecond()).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Component getName(ItemStack p_41458_) {
        CompoundTag $$1 = p_41458_.getTagElement("display");
        if ($$1 == null || !$$1.contains("tagType")) {
            return super.getName(p_41458_);
        }
        int $$2 = $$1.getInt("tagType");
        if ($$2 >= TagType.values().length) {
            return super.getName(p_41458_);
        }
        return Component.translatable("item.playercollars.tag." + TagType.values()[$$2].name()).append(" ").append(super.getName(p_41458_));
    }

    @Override
    public boolean isEnchantable(ItemStack p_41456_) {
        return true;
    }

    @Override
    public void fillItemCategory(CreativeModeTab p_41391_, NonNullList<ItemStack> p_41392_) {
        if (this.allowedIn(p_41391_)) {
            for (TagType t : TagType.values())
                p_41392_.add(getInstance(t));
        }
    }
}
