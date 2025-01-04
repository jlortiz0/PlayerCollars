package org.jlortiz.playercollars.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.client.CollarDyeScreen;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

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
        super(new Item.Properties().stacksTo(1));
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
        if (ent.level().isClientSide) return;
        CuriosApi.getCuriosInventory(ent).ifPresent((handler) -> handler.findCurio(slotContext.identifier(), slotContext.index()).ifPresent((sr) -> {
            if (this.getEnchantmentLevel(sr.stack(), Enchantments.MENDING) == 0) return;
            Pair<UUID, String> owner = this.getOwner(sr.stack());
            if (owner == null || owner.getFirst().equals(ent.getUUID())) return;
            Player own = ent.level().getPlayerByUUID(owner.getFirst());
            if (own != null && own.distanceTo(ent) < 16) {
                ent.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, false));
            }
        }));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction direction) {
        if (capability == CuriosCapability.ITEM) {
            return LazyOptional.of(() -> (T) this);
        }
        return LazyOptional.empty();
    }

    @Override
    public int getColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        return $$1 != null && $$1.contains("color", 99) ? $$1.getInt("color") : MapColor.COLOR_RED.col;
    }

    public int getPawColor(ItemStack itemStack) {
        CompoundTag $$1 = itemStack.getTagElement("display");
        return $$1 != null && $$1.contains("paw", 99) ? $$1.getInt("paw") : MapColor.COLOR_BLUE.col;
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        InteractionResultHolder<ItemStack> ir = super.use(p_41432_, p_41433_, p_41434_);
        if (ir.getResult() == InteractionResult.PASS && p_41433_.isCrouching() && p_41432_.isClientSide) {
            Minecraft.getInstance().setScreen(new CollarDyeScreen(ir.getObject(), p_41433_.getUUID()));
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
    public boolean isEnchantable(ItemStack p_41456_) {
        return true;
    }
}
