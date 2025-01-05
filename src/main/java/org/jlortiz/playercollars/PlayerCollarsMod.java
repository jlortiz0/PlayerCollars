package org.jlortiz.playercollars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Uuids;
import org.jlortiz.playercollars.item.RegenerationEnchantmentEffect;
import org.jlortiz.playercollars.item.ClickerItem;
import org.jlortiz.playercollars.item.CollarItem;

import java.util.List;
import java.util.UUID;

public class PlayerCollarsMod implements ModInitializer {
	public static final String MOD_ID = "playercollars";
	public static final CollarItem COLLAR_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "collar"), new CollarItem());
	public static final ClickerItem CLICKER_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "clicker"), new ClickerItem());
	public static final SoundEvent CLICKER_ON = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "clicker_on"),
			SoundEvent.of(Identifier.of(MOD_ID, "clicker_on")));
	public static final SoundEvent CLICKER_OFF = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "clicker_off"),
			SoundEvent.of(Identifier.of(MOD_ID, "clicker_off")));

	private static final Codec<OwnerComponent> OWNER_COMPONENT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
			Uuids.CODEC.fieldOf("uuid").forGetter(OwnerComponent::uuid),
            Codec.STRING.fieldOf("name").forGetter(OwnerComponent::name)
    ).apply(builder, OwnerComponent::new));
	public static final ComponentType<OwnerComponent> OWNER_COMPONENT_TYPE = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			Identifier.of(MOD_ID, "owner_component"),
			ComponentType.<OwnerComponent>builder().codec(OWNER_COMPONENT_CODEC).build());
	public static final RegistryEntry<EntityAttribute> ATTR_CLICKER_DISTANCE = Registry.registerReference(
			Registries.ATTRIBUTE, Identifier.of(PlayerCollarsMod.MOD_ID, "clicker_distance"),
			new ClampedEntityAttribute("attribute.playercollars.clicker_distance", 4, 0, 32));
	public static final RegistryEntry<EntityAttribute> ATTR_LEASH_DISTANCE = Registry.registerReference(
			Registries.ATTRIBUTE, Identifier.of(PlayerCollarsMod.MOD_ID, "leash_distance"),
			new ClampedEntityAttribute("attribute.playercollars.leash_distance", 4, 2, 4));

	public static ItemStack filterStacksByOwner(List<Pair<SlotReference, ItemStack>> stacks, UUID plr) {
		for (Pair<SlotReference, ItemStack> p : stacks) {
			ItemStack is = p.getRight();
			if (is.getItem() instanceof CollarItem item) {
				OwnerComponent owner = item.getOwner(is);
				if (owner != null && owner.uuid().equals(plr)) {
					return is;
				}
			}
		}
		return null;
	}

	@Override
	public void onInitialize() {
		Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of(PlayerCollarsMod.MOD_ID, "regeneration_effect"), RegenerationEnchantmentEffect.CODEC);
		PayloadTypeRegistry.playC2S().register(PacketUpdateCollar.ID, PacketUpdateCollar.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PacketUpdateCollar.ID, PacketUpdateCollar::handle);
		PayloadTypeRegistry.playS2C().register(PacketLookAtLerped.ID, PacketLookAtLerped.CODEC);
		TrinketsApi.registerTrinket(PlayerCollarsMod.COLLAR_ITEM, PlayerCollarsMod.COLLAR_ITEM);
	}
}
