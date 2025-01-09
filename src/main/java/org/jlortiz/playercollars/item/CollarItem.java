package org.jlortiz.playercollars.item;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
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
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.client.CollarDyeScreen;

import java.util.List;

public class CollarItem extends TrinketItem {

    public CollarItem() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public int getEnchantability() {
        return 60;
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity.getWorld().isClient) return;
        if (EnchantmentHelper.getLevel(Enchantments.MENDING, stack) > 0) {
            OwnerComponent owner = this.getOwner(stack);
            if (owner == null || owner.uuid().equals(entity.getUuid())) return;
            PlayerEntity own = entity.getWorld().getPlayerByUuid(owner.uuid());
            if (own != null && own.distanceTo(entity) < 16) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 40, 0, false, false, false));
            }
        }
    }

    public int getColor(ItemStack itemStack) {
        DyedColorComponent $$1 = itemStack.get(DataComponentTypes.DYED_COLOR);
        return $$1 != null ? $$1.rgb() : MapColor.RED.color;
    }

    public int getPawColor(ItemStack itemStack) {
        MapColorComponent $$1 = itemStack.get(DataComponentTypes.MAP_COLOR);
        return $$1 != null ? $$1.rgb() : MapColor.BLUE.color;
    }

    public @Nullable OwnerComponent getOwner(ItemStack is) {
        return is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
    }

    @Override
    public TypedActionResult<ItemStack> use(World p_41432_, PlayerEntity p_41433_, Hand p_41434_) {
        ItemStack is = p_41433_.getStackInHand(p_41434_);
        if (p_41433_.isSneaking() && p_41432_.isClient) {
            MinecraftClient.getInstance().setScreen(new CollarDyeScreen(is, p_41433_.getUuid()));
            return TypedActionResult.success(is, false);
        }
        return TypedActionResult.pass(is);
    }

    @Override
    public void appendTooltip(ItemStack p_41421_, @Nullable TooltipContext p_41422_, List<Text> p_41423_, @NotNull TooltipType p_41424_) {
        super.appendTooltip(p_41421_, p_41422_, p_41423_, p_41424_);
        if (p_41424_.isAdvanced()) {
            p_41423_.add(Text.translatable("item.playercollars.collar.paw_color", Integer.toHexString(getPawColor(p_41421_))).setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
        OwnerComponent owner = getOwner(p_41421_);
        if (owner != null) {
            p_41423_.add(Text.translatable("item.playercollars.collar.owner", owner.name()).setStyle(Style.EMPTY.withColor(Colors.GRAY)));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack p_41456_) {
        return true;
    }
}
