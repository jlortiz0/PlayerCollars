package org.jlortiz.playercollars;

import dev.emi.trinkets.api.SlotReference;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jlortiz.playercollars.item.ClickerItem;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.item.CollarRecipe;

import java.util.List;
import java.util.UUID;

public class PlayerCollarsMod implements ModInitializer {
	public static final String MOD_ID = "playercollars";
	public static final CollarItem COLLAR_ITEM = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "collar"), new CollarItem());
	public static final ClickerItem CLICKER_ITEM = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "clicker"), new ClickerItem());
	public static final SoundEvent CLICKER_ON = Registry.register(Registries.SOUND_EVENT, new Identifier(MOD_ID, "clicker_on"),
			SoundEvent.of(new Identifier(MOD_ID, "clicker_on")));
	public static final SoundEvent CLICKER_OFF = Registry.register(Registries.SOUND_EVENT, new Identifier(MOD_ID, "clicker_off"),
			SoundEvent.of(new Identifier(MOD_ID, "clicker_off")));

	public static ItemStack filterStacksByOwner(List<Pair<SlotReference, ItemStack>> stacks, UUID plr) {
		for (Pair<SlotReference, ItemStack> p : stacks) {
			ItemStack is = p.getRight();
			if (is.getItem() instanceof CollarItem item) {
				Pair<UUID, String> owner = item.getOwner(is);
				if (owner != null && owner.getLeft().equals(plr)) {
					return is;
				}
			}
		}
		return null;
	}

	@Override
	public void onInitialize() {
		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(MOD_ID, CollarRecipe.Type.ID), CollarRecipe.Serializer);
		Registry.register(Registries.RECIPE_TYPE, new Identifier(MOD_ID, CollarRecipe.Type.ID), CollarRecipe.Type.INSTANCE);
		ServerPlayNetworking.registerGlobalReceiver(new Identifier(MOD_ID, "update_collar"), PacketUpdateCollar::handle);
	}
}
