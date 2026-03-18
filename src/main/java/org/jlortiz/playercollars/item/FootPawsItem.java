package org.jlortiz.playercollars.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jlortiz.playercollars.PlayerCollarsMod;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class FootPawsItem extends Item {
    private final int color;
    private final int pawColor;

    public FootPawsItem(int color, int pawColor) {
        super(new Item.Properties().stacksTo(1));
        this.color = color;
        this.pawColor = pawColor;
    }

    @Override public boolean isFoil(ItemStack stack) { return false; }

    /** Build an ICurio capability for a given stack of this item. */
    public ICurio buildCurio(ItemStack stack) {
        return new ICurio() {
            @Override
            public ItemStack getStack() { return stack; }

            @Override
            public void curioTick(SlotContext slotContext) {}

            @Override
            public ICurio.DropRule getDropRule(SlotContext slotContext,
                    net.minecraft.world.damagesource.DamageSource source, int lootingLevel, boolean recentlyHit) {
                return ICurio.DropRule.ALWAYS_KEEP;
            }
        };
    }

    public static ResourceLocation getIdentifier(DyeColor c) {
        return new ResourceLocation(PlayerCollarsMod.MOD_ID, c.getName() + "_foot_paws");
    }

    public int getColor() { return this.color; }
    public int getPawColor() { return this.pawColor; }

    @Override
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(
            net.minecraft.world.item.ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.nbt.CompoundTag nbt) {
        return top.theillusivec4.curios.api.CuriosApi.createCurioProvider(buildCurio(stack));
    }
}