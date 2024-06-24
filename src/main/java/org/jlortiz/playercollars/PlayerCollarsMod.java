package org.jlortiz.playercollars;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jlortiz.playercollars.item.ClickerItem;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.item.CollarRecipe;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;
import java.util.UUID;

@Mod(PlayerCollarsMod.MOD_ID)
public class PlayerCollarsMod {
	public static final String MOD_ID = "playercollars";
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	public static final RegistryObject<CollarItem> COLLAR_ITEM = ITEMS.register("collar", CollarItem::new);
	public static final RegistryObject<ClickerItem> CLICKER_ITEM = ITEMS.register("clicker", ClickerItem::new);
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
	public static final RegistryObject<RecipeSerializer<CollarRecipe>> COLLAR_SERIALIZER =
			RECIPE_SERIALIZERS.register(CollarRecipe.Type.ID, () -> CollarRecipe.Serializer);
	private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MOD_ID);
	public static final RegistryObject<RecipeType<CollarRecipe>> COLLAR_TYPE =
			RECIPE_TYPES.register(CollarRecipe.Type.ID, () -> CollarRecipe.Type.INSTANCE);
	public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "collar_channel"), () -> "", String::isEmpty, String::isEmpty);
	private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);
	public static final RegistryObject<SoundEvent> CLICKER_ON = SOUNDS.register("clicker_on", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "clicker_on")));
	public static final RegistryObject<SoundEvent> CLICKER_OFF = SOUNDS.register("clicker_off", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "clicker_off")));

	public PlayerCollarsMod() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(eventBus);
		RECIPE_SERIALIZERS.register(eventBus);
		RECIPE_TYPES.register(eventBus);
		SOUNDS.register(eventBus);
		eventBus.register(this);
		NETWORK.registerMessage(1, PacketUpdateCollar.class, PacketUpdateCollar::encode, PacketUpdateCollar::new, PacketUpdateCollar::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		NETWORK.registerMessage(2, PacketLookAtLerped.class, PacketLookAtLerped::write, PacketLookAtLerped::new, PacketLookAtLerped::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	public static ItemStack filterStacksByOwner(IDynamicStackHandler stacks, UUID plr) {
		for (int i = 0; i < stacks.getSlots(); i++) {
			ItemStack is = stacks.getStackInSlot(i);
			if (is.getItem() instanceof CollarItem item) {
				Pair<UUID, String> owner = item.getOwner(is);
				if (owner != null && owner.getFirst().equals(plr)) {
					return is;
				}
			}
		}
		return null;
	}
}
