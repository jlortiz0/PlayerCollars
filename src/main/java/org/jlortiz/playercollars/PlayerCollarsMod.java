package org.jlortiz.playercollars;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jlortiz.playercollars.item.ClickerItem;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.item.CollarRecipe;
import top.theillusivec4.curios.api.SlotTypeMessage;

import java.util.Optional;

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
	public static final CreativeModeTab TAB = new CreativeModeTab(MOD_ID) {
		@Override
		public @NotNull ItemStack makeIcon() {
			return new ItemStack(COLLAR_ITEM.get());
		}
	};
	public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "collar_channel"), () -> "", String::isEmpty, String::isEmpty);
	private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);
	public static final RegistryObject<SoundEvent> CLICKER_ON = SOUNDS.register("clicker_on", () -> new SoundEvent(new ResourceLocation(MOD_ID, "clicker_on")));
	public static final RegistryObject<SoundEvent> CLICKER_OFF = SOUNDS.register("clicker_off", () -> new SoundEvent(new ResourceLocation(MOD_ID, "clicker_off")));

	public PlayerCollarsMod() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(eventBus);
		RECIPE_SERIALIZERS.register(eventBus);
		RECIPE_TYPES.register(eventBus);
		SOUNDS.register(eventBus);
		eventBus.register(this);
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("necklace").cosmetic().build());
		NETWORK.registerMessage(1, PacketUpdateCollar.class, PacketUpdateCollar::encode, PacketUpdateCollar::new, PacketUpdateCollar::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
	}

	@SubscribeEvent
	public void registerItemColors(RegisterColorHandlersEvent.Item event) {
		event.register(((itemStack, i) -> switch (i) {
			case 0 -> COLLAR_ITEM.get().getColor(itemStack);
			case 1 -> COLLAR_ITEM.get().getTagColor(itemStack);
			case 2 -> COLLAR_ITEM.get().getPawColor(itemStack);
			default -> -1;
		}
		), COLLAR_ITEM.get());

		event.register((itemStack, i) -> i == 0 ? CLICKER_ITEM.get().getColor(itemStack) : -1, CLICKER_ITEM.get());
	}
}
