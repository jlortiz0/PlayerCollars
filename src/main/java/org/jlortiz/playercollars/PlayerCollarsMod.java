package org.jlortiz.playercollars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.trinkets.api.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.block.DogBedBlock;
import org.jlortiz.playercollars.block.DogBowlBlock;
import org.jlortiz.playercollars.block.InvisibleFenceBlock;
import org.jlortiz.playercollars.item.*;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.jlortiz.playercollars.network.PacketLookAtLerped;
import org.jlortiz.playercollars.network.PacketStampDeed;
import org.jlortiz.playercollars.network.PacketUpdateCollar;

import java.util.*;
import java.util.function.UnaryOperator;

public class PlayerCollarsMod implements ModInitializer {
	public static final String MOD_ID = "playercollars";
    public static final CollarItem COLLAR_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "collar"), new CollarItem());
    public static final ClickerItem CLICKER_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "clicker"), new ClickerItem());
    public static final DeedItem DEED_OF_OWNERSHIP = Registry.register(Registries.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "deed_of_ownership"), new DeedItem());
    public static final Item DEED_OF_OWNERSHIP_STAMPED = Registry.register(Registries.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "stamped_deed_of_ownership"), new StampedDeedItem());
    public static final InvisibleFenceBlock INVISIBLE_FENCE_BLOCK = Registry.register(Registries.BLOCK, InvisibleFenceBlock.REGISTRY_KEY,
            new InvisibleFenceBlock(AbstractBlock.Settings.create().noCollision().breakInstantly().sounds(BlockSoundGroup.STONE)));
    public static final BlockItem INVISIBLE_FENCE_BLOCK_ITEM = Registry.register(Registries.ITEM, InvisibleFenceBlock.ITEM_REGISTRY_KEY, new BlockItem(INVISIBLE_FENCE_BLOCK, new Item.Settings()));
    public static final PawSetupItem PAW_CONFIGURATION_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "paw_configurator"), new PawSetupItem());
	public static final SpatulaItem SPATULA_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "golden_spatula"), new SpatulaItem());

	public static final SoundEvent CLICKER_ON = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "clicker_on"),
			SoundEvent.of(Identifier.of(MOD_ID, "clicker_on")));
	public static final SoundEvent CLICKER_OFF = Registry.register(Registries.SOUND_EVENT, Identifier.of(MOD_ID, "clicker_off"),
			SoundEvent.of(Identifier.of(MOD_ID, "clicker_off")));

	private static final Codec<OwnerComponent> OWNER_COMPONENT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
			Uuids.CODEC.fieldOf("uuid").forGetter(OwnerComponent::uuid),
            Codec.STRING.fieldOf("name").forGetter(OwnerComponent::name),
			Codecs.optional(Uuids.CODEC).fieldOf("owned").forGetter(OwnerComponent::owned),
			Codecs.optional(Codec.STRING).fieldOf("owned_name").forGetter(OwnerComponent::ownedName)
    ).apply(builder, OwnerComponent::new));
	public static final ComponentType<OwnerComponent> OWNER_COMPONENT_TYPE = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			Identifier.of(MOD_ID, "owner_component"),
			ComponentType.<OwnerComponent>builder().codec(OWNER_COMPONENT_CODEC).build());

	private static final Codec<Set<Identifier>> CAN_INTERACT_COMPONENT_CODEC = new ListCodec<>(
			Identifier.CODEC, 0, 65535).xmap(Set::copyOf, List::copyOf);
	public static final ComponentType<Set<Identifier>> CAN_INTERACT_COMPONENT_TYPE = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			Identifier.of(MOD_ID, "can_interact_component"),
			ComponentType.<Set<Identifier>>builder().codec(CAN_INTERACT_COMPONENT_CODEC).build());

	public static final RegistryEntry<EntityAttribute> ATTR_CLICKER_DISTANCE = Registry.registerReference(
			Registries.ATTRIBUTE, Identifier.of(MOD_ID, "clicker_distance"),
			new ClampedEntityAttribute("attribute.playercollars.clicker_distance", 4, 0, 32));
	public static final RegistryEntry<EntityAttribute> ATTR_LEASH_DISTANCE = Registry.registerReference(
			Registries.ATTRIBUTE, Identifier.of(MOD_ID, "leash_distance"),
			new ClampedEntityAttribute("attribute.playercollars.leash_distance", 4, 2, 16));
    public static final GameRules.Key<GameRules.BooleanRule> PLAYER_LEASHES_BREAK_RULE = GameRuleRegistry.register(
            "playerLeashesBreak", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.BooleanRule> DROP_BINDING_CURSE_ON_DEATH_RULE = GameRuleRegistry.register(
            "dropBindingCurseTrinketItemsOnDeath", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    public static final TagKey<Item> BINDING_CURSE_ENCHANTABLE_TAG = TagKey.of(Registries.ITEM.getKey(), Identifier.of("playercollars", "enchantable/equippable"));

	public static final DogBedBlock[] DOG_BEDS = new DogBedBlock[DyeColor.values().length];
	public static final BedItem[] DOG_BED_ITEMS = new BedItem[DyeColor.values().length];
	public static final TagKey<Item> COLLAR_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "collars"));

	public static final DyeColor[] PAWS_DYE_COLORS = new DyeColor[]{DyeColor.WHITE, DyeColor.LIGHT_GRAY,
			DyeColor.GRAY, DyeColor.BLACK, DyeColor.BLUE, DyeColor.RED, DyeColor.PURPLE};
	public static final PawsItem[] PAWS_ITEMS = new PawsItem[PAWS_DYE_COLORS.length];
	public static final TagKey<Block> PAWS_ALLOW_INTERACT = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "paws_allow_interact"));
	public static final TagKey<Item> PAWS_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "paws"));
	public static final FootPawsItem[] FOOT_PAWS_ITEMS = new FootPawsItem[PAWS_DYE_COLORS.length];
	public static final TagKey<Item> FOOT_PAWS_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "foot_paws"));

	public static final DogBowlBlock[] DOG_BOWLS = new DogBowlBlock[DyeColor.values().length];
	public static final Item[] DOG_BOWL_ITEMS = new Item[DyeColor.values().length];
	public static final BlockEntityType<DogBowlBlock.DogBowlBlockEntity> DOG_BOWL_BLOCK_ENTITY;
	public static final ItemGroup GROUP;

	static {
		for (DyeColor c : DyeColor.values()) {
			DOG_BOWLS[c.ordinal()] = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, c.getName() + "_dog_bowl"),
					new DogBowlBlock(c, AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE).strength(0.6F).nonOpaque().pistonBehavior(PistonBehavior.DESTROY)));
			DOG_BOWL_ITEMS[c.ordinal()] = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, c.getName() + "_dog_bowl"),
					new BlockItem(DOG_BOWLS[c.ordinal()], new Item.Settings()));
		}
		DOG_BOWL_BLOCK_ENTITY = Registry.register(
				Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "dog_bowl"),
				BlockEntityType.Builder.create(DogBowlBlock.DogBowlBlockEntity::new, DOG_BOWLS).build()
		);

		GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "group"),
				FabricItemGroup.builder().displayName(Text.translatable("itemGroup.playercollars"))
				.icon(COLLAR_ITEM::getDefaultStack)
				.entries(((displayContext, entries) -> {
					entries.add(COLLAR_ITEM);
					entries.add(CLICKER_ITEM);
					entries.add(PAW_CONFIGURATION_ITEM);
					for (PawsItem p : PAWS_ITEMS)
						entries.add(p);
					for (FootPawsItem p : FOOT_PAWS_ITEMS)
						entries.add(p);
					entries.add(DEED_OF_OWNERSHIP);
					entries.add(SPATULA_ITEM);
					for (BedItem bed : DOG_BED_ITEMS)
						entries.add(bed);
					for (Item bowl : DOG_BOWL_ITEMS)
						entries.add(bowl);
					entries.add(INVISIBLE_FENCE_BLOCK_ITEM);
				})).build());
	}

	public static ItemStack filterStacksByOwner(List<Pair<SlotReference, ItemStack>> stacks, UUID plr, UUID entity) {
		for (Pair<SlotReference, ItemStack> p : stacks) {
			ItemStack is = p.getRight();
			OwnerComponent owner = CollarItem.getOwner(is);
			if (owner != null && owner.uuid().equals(plr) &&
					(owner.owned().isEmpty() || owner.owned().get().equals(entity))) {
				return is;
			}
		}
		return null;
	}

	public static ActionResult pullPlayerTowards(ServerPlayerEntity plr, Vec3d towards, double minDist, double maxDist, UnaryOperator<Double> getFactor) {
		Vec3d vecTo = towards.subtract(plr.getPos());
		double distance = vecTo.length();
		if (distance < minDist) return ActionResult.PASS;
		if (distance > maxDist) return ActionResult.FAIL;

		plr.addVelocity(vecTo.multiply(Math.abs(getFactor.apply(distance))));
		plr.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(plr));
		plr.velocityDirty = false;
		return ActionResult.SUCCESS;
	}

	@Override
	public void onInitialize() {
		Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of(MOD_ID, "regeneration_effect"), RegenerationEnchantmentEffect.CODEC);
		Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "owner_transfer"), OwnershipCraftingRecipe.Serializer.INSTANCE);
		PayloadTypeRegistry.playC2S().register(PacketUpdateCollar.ID, PacketUpdateCollar.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PacketUpdateCollar.ID, PacketUpdateCollar::handle);
		PayloadTypeRegistry.playC2S().register(PacketStampDeed.ID, PacketStampDeed.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PacketStampDeed.ID, PacketStampDeed::handle);
		PayloadTypeRegistry.playS2C().register(PacketLookAtLerped.ID, PacketLookAtLerped.CODEC);
		TrinketsApi.registerTrinket(PlayerCollarsMod.COLLAR_ITEM, PlayerCollarsMod.COLLAR_ITEM);

        for (int i = 0; i < PAWS_DYE_COLORS.length; i++) {
            DyeColor c = PAWS_DYE_COLORS[i];
            Identifier itemKey = PawsItem.getIdentifier(c);
            PAWS_ITEMS[i] = Registry.register(Registries.ITEM, itemKey,
                    new PawsItem(c.getFireworkColor(), 0xF196CF));
            TrinketsApi.registerTrinket(PAWS_ITEMS[i], PAWS_ITEMS[i]);
			itemKey = FootPawsItem.getIdentifier(c);
			FOOT_PAWS_ITEMS[i] = Registry.register(Registries.ITEM, itemKey,
					new FootPawsItem(c.getFireworkColor(), 0xF196CF));
			TrinketsApi.registerTrinket(FOOT_PAWS_ITEMS[i], FOOT_PAWS_ITEMS[i]);
        }

		for (DyeColor c : DyeColor.values()) {
			DOG_BEDS[c.ordinal()] = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, c.getName() + "_dog_bed"),
					new DogBedBlock(c, AbstractBlock.Settings.create().sounds(BlockSoundGroup.WOOL).strength(0.2F).nonOpaque().burnable().pistonBehavior(PistonBehavior.DESTROY)));
			DOG_BED_ITEMS[c.ordinal()] = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, c.getName() + "_dog_bed"),
					new BedItem(DOG_BEDS[c.ordinal()], (new Item.Settings()).maxCount(1)));
		}

		PlayerBlockBreakEvents.BEFORE.register((World var1, PlayerEntity player, BlockPos blockPos, BlockState var4, @Nullable BlockEntity var5) -> {
			if (var1.isClient) return true;
			if (player.isSpectator()) return true;
			Entity leashHolderEntity = ((LeashImpl) player).leashplayers$getProxyLeashHolder();
			if (leashHolderEntity instanceof LeashKnotEntity knot && blockPos.equals(knot.getAttachedBlockPos())) {
				player.sendMessage(Text.translatable("message.playercollars.no_break_fence").formatted(Formatting.RED), true);
				return false;
			}
			return true;
		});

		AttackEntityCallback.EVENT.register((PlayerEntity player, World var2, Hand var3, Entity var4, @Nullable EntityHitResult var5) -> {
			if (var2.isClient) return ActionResult.PASS;
			if (player.isSpectator()) return ActionResult.PASS;
			if (var4 instanceof PlayerEntity &&
				TrinketsApi.getTrinketComponent(player).map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)))
						.map((x) -> PlayerCollarsMod.filterStacksByOwner(x, var4.getUuid(), player.getUuid())).isPresent()) {
					// Collared players are allowed to attack bad owners, but have 75% damage returned to them
					player.sendMessage(Text.translatable("message.playercollars.no_attack_owner").formatted(Formatting.RED), true);
					double f = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
					f = (f - 1) * 0.75 + 1;
					player.damage(player.getDamageSources().playerAttack(player), (float) Math.ceil(f));
					return ActionResult.PASS;
			}

			Entity leashedEnt = ((LeashImpl) player).leashplayers$getProxyLeashHolder();
			if (leashedEnt instanceof LeashKnotEntity && leashedEnt.equals(var4)) {
				player.sendMessage(Text.translatable("message.playercollars.no_break_fence").formatted(Formatting.RED), true);
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});

        ServerLivingEntityEvents.ALLOW_DEATH.register((LivingEntity entity, DamageSource source, float amount) -> {
            // only run on *player* death and on the server
            if(!(entity instanceof ServerPlayerEntity player)) return true;
            // only do if gamerule is true
            if(!player.getWorld().getGameRules().getBoolean(PlayerCollarsMod.DROP_BINDING_CURSE_ON_DEATH_RULE)) return true;

            TrinketComponent trinkets = TrinketsApi.getTrinketComponent(player).orElse(null);
            if(trinkets == null) return true;
            TrinketComponent component = TrinketsApi.getTrinketComponent(player).orElse(null);
            if(component == null) return true;
            ArrayList<Pair<String, String>> slotsToCheck = new ArrayList<>();
            // add more slots if needed, these are just the group and slot in trinkets
            slotsToCheck.add(new Pair<>("hand", "glove"));
            slotsToCheck.add(new Pair<>("chest", "necklace"));
            slotsToCheck.add(new Pair<>("feet", "shoes"));
            for(Pair<String, String> slot : slotsToCheck) {
                // check SlotGroup and SlotType just to be sure they exist and all that (im not entirely sure whether this is needed)
                SlotGroup group = component.getGroups().getOrDefault(slot.getLeft(), null);
                if(group == null) continue;
                SlotType type = group.getSlots().getOrDefault(slot.getRight(), null);
                if(type == null) continue;
                ItemStack stack = component.getInventory().get(slot.getLeft()).get(slot.getRight()).getStack(0 /* offset = 0 */);
                if(!stack.hasEnchantments()) continue;
                // only do this for items that are actually enchantable with curse of binding and are provided by this mod (item tag is defined manually)

                if(!stack.isIn(PlayerCollarsMod.BINDING_CURSE_ENCHANTABLE_TAG)) continue;
                // this monster checks if the item has curse of binding enchantment
                if(EnchantmentHelper.getLevel(player.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.BINDING_CURSE), stack) == 0) continue;

                // drop item and make slot empty
                player.dropItem(stack, true, false);
                component.getInventory().get(slot.getLeft()).get(slot.getRight()).setStack(0, ItemStack.EMPTY);
            }
            return true;
        });

	}
}
