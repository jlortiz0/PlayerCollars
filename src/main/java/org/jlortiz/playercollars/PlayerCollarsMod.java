package org.jlortiz.playercollars;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoryRegistry;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.jlortiz.playercollars.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class PlayerCollarsMod implements ModInitializer {
	public static final String MOD_ID = "playercollars";
    public static final CollarItem COLLAR_ITEM = Registry.register(Registries.ITEM, CollarItem.REGISTRY_KEY, new CollarItem(false));
    public static final CollarItem TAGLESS_COLLAR_ITEM = Registry.register(Registries.ITEM, CollarItem.TAGLESS_REGISTRY_KEY, new CollarItem(true));
    public static final ClickerItem CLICKER_ITEM = Registry.register(Registries.ITEM, ClickerItem.REGISTRY_KEY, new ClickerItem());
    public static final DeedItem DEED_OF_OWNERSHIP = Registry.register(Registries.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "deed_of_ownership"), new DeedItem());
    public static final Item DEED_OF_OWNERSHIP_STAMPED = Registry.register(Registries.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "stamped_deed_of_ownership"), new StampedDeedItem());
    public static final InvisibleFenceBlock INVISIBLE_FENCE_BLOCK = Registry.register(Registries.BLOCK, InvisibleFenceBlock.REGISTRY_KEY,
            new InvisibleFenceBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.GLASS).nonOpaque().dynamicBounds()));
    public static final BlockItem INVISIBLE_FENCE_BLOCK_ITEM = Registry.register(Registries.ITEM, InvisibleFenceBlock.ITEM_REGISTRY_KEY,
			new BlockItem(INVISIBLE_FENCE_BLOCK, new Item.Settings().registryKey(InvisibleFenceBlock.ITEM_REGISTRY_KEY)));
    public static final PawSetupItem PAW_CONFIGURATION_ITEM = Registry.register(Registries.ITEM, PawSetupItem.REGISTRY_KEY, new PawSetupItem());
    public static final CollarLockerItem COLLAR_LOCKER_ITEM = Registry.register(Registries.ITEM, CollarLockerItem.REGISTRY_KEY, new CollarLockerItem());
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

	private static final Codec<List<Either<TagKey<Block>, RegistryKey<Block>>>> CAN_INTERACT_COMPONENT_CODEC = Codec.withAlternative(
			new ListCodec<>(new EitherCodec<>(TagKey.codec(RegistryKeys.BLOCK), RegistryKey.createCodec(RegistryKeys.BLOCK)), 0, 1024),
			Codec.of(Encoder.error("deprecated"), new ListCodec<>(Identifier.CODEC, 0, 65535).map((x) -> {
				List<Either<TagKey<Block>, RegistryKey<Block>>> ls = new ArrayList<>(x.size());
				for (Identifier id : x) {
					ls.add(Either.right(RegistryKey.of(RegistryKeys.BLOCK, id)));
				}
				return ls;
			}))
	);
	public static final ComponentType<List<Either<TagKey<Block>, RegistryKey<Block>>>> CAN_INTERACT_COMPONENT_TYPE = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			Identifier.of(MOD_ID, "can_interact_component"),
			ComponentType.<List<Either<TagKey<Block>, RegistryKey<Block>>>>builder().codec(CAN_INTERACT_COMPONENT_CODEC).build());

	private static final Codec<List<Either<TagKey<Item>, RegistryKey<Item>>>> HELD_ITEMS_COMPONENT_CODEC = new ListCodec<>(
			new EitherCodec<>(TagKey.codec(RegistryKeys.ITEM), RegistryKey.createCodec(RegistryKeys.ITEM)), 0, 65535);
	public static final ComponentType<List<Either<TagKey<Item>, RegistryKey<Item>>>> HELD_ITEMS_COMPONENT_TYPE = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			Identifier.of(MOD_ID, "held_items_component"),
			ComponentType.<List<Either<TagKey<Item>, RegistryKey<Item>>>>builder().codec(HELD_ITEMS_COMPONENT_CODEC).build());

	public static final RegistryEntry<EntityAttribute> ATTR_CLICKER_DISTANCE = Registry.registerReference(
			Registries.ATTRIBUTE, Identifier.of(MOD_ID, "clicker_distance"),
			new ClampedEntityAttribute("attribute.playercollars.clicker_distance", 4, 0, 32));
	public static final RegistryEntry<EntityAttribute> ATTR_LEASH_DISTANCE = Registry.registerReference(
			Registries.ATTRIBUTE, Identifier.of(MOD_ID, "leash_distance"),
			new ClampedEntityAttribute("attribute.playercollars.leash_distance", 4, 2, 16));

	public static final GameRules.Key<GameRules.BooleanRule> PLAYER_LEASHES_BREAK_RULE = GameRuleRegistry.register(
			"playerLeashesBreak", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.BooleanRule> LEASHED_PLAYERS_RIDE_ENTITIES = GameRuleRegistry.register(
            "leashedPlayersRideEntities", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
	public static final GameRules.Key<GameRules.BooleanRule> ALLOW_ATTACK_OWNER = GameRuleRegistry.register(
			"playerAllowAttackOwner", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
	public static final GameRules.Key<GameRules.BooleanRule> ALLOW_UNLEASH_OTHER = GameRuleRegistry.register(
			"allowUnleashUnownedPlayer", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

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
	public static final ExtendedScreenHandlerType<PawsConfigScreenHandler<Block>, List<Either<TagKey<Block>, RegistryKey<Block>>>> PAWS_BLOCK_CONFIG_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(
			PawsConfigScreenHandler.PawsBlockConfigScreenHandler::new, PacketCodecs.codec(CAN_INTERACT_COMPONENT_CODEC)
	);
	public static final ExtendedScreenHandlerType<PawsConfigScreenHandler<Item>, List<Either<TagKey<Item>, RegistryKey<Item>>>> PAWS_ITEM_CONFIG_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(
			PawsConfigScreenHandler.PawsItemConfigScreenHandler::new, PacketCodecs.codec(HELD_ITEMS_COMPONENT_CODEC)
	);

    static {
        for (DyeColor c : DyeColor.values()) {
			RegistryKey<Block> blockKey = DogBowlBlock.getRegistryKey(c);
            DOG_BOWLS[c.ordinal()] = Registry.register(Registries.BLOCK, blockKey.getValue(),
                    new DogBowlBlock(c, AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE).strength(0.6F).nonOpaque().pistonBehavior(PistonBehavior.DESTROY).registryKey(blockKey)));
			RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockKey.getValue());
            DOG_BOWL_ITEMS[c.ordinal()] = Registry.register(Registries.ITEM, itemKey.getValue(),
                    new BlockItem(DOG_BOWLS[c.ordinal()], new Item.Settings().registryKey(itemKey)));
        }
        DOG_BOWL_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "dog_bowl"),
				FabricBlockEntityTypeBuilder.create(DogBowlBlock.DogBowlBlockEntity::new, DOG_BOWLS).build()
        );

        GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "group"),
                FabricItemGroup.builder().displayName(Text.translatable("itemGroup.playercollars"))
                        .icon(COLLAR_ITEM::getDefaultStack)
                        .entries(((displayContext, entries) -> {
                            entries.add(COLLAR_ITEM);
                            entries.add(TAGLESS_COLLAR_ITEM);
                            entries.add(CLICKER_ITEM);
                            entries.add(COLLAR_LOCKER_ITEM);
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

		Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "paws_block_config"), PAWS_BLOCK_CONFIG_SCREEN_HANDLER);
		Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "paws_item_config"), PAWS_ITEM_CONFIG_SCREEN_HANDLER);
	}

	public static ItemStack filterStacksByOwner(Iterable<SlotEntryReference> stacks, UUID plr, UUID entity) {
		for (SlotEntryReference p : stacks) {
			ItemStack is = p.stack();
			OwnerComponent owner = is.get(OWNER_COMPONENT_TYPE);
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

	public static boolean blockLeashKnotBreak(ServerWorld world, PlayerEntity player, LeashKnotEntity entity) {
		if (entity.equals(((LeashImpl) player).leashplayers$getProxyLeashHolder())) {
			player.sendMessage(Text.translatable("message.playercollars.no_break_fence").formatted(Formatting.RED), true);
			return true;
		}
		if (!world.getGameRules().getBoolean(ALLOW_UNLEASH_OTHER)) {
			List<Leashable> list = LeadItem.collectLeashablesAround(world, entity.getBlockPos(), (e) -> entity.equals(e.getLeashHolder()));
			for (Leashable l : list) {
				if (!(l instanceof LeashProxyEntity le)) continue;
				LivingEntity leashTarget = le.getLeashTarget();
				AccessoriesCapability cap = AccessoriesCapability.get(leashTarget);
				if (cap == null) continue;
				for (SlotEntryReference sr : cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.COLLAR_TAG))) {
					OwnerComponent oc = sr.stack().get(OWNER_COMPONENT_TYPE);
					if (oc == null || !oc.owned().orElseGet(leashTarget::getUuid).equals(leashTarget.getUuid())) continue;
					if (!player.getUuid().equals(oc.uuid())) {
						player.sendMessage(Text.translatable("message.playercollars.no_break_fence_other", le.getLeashTarget().getName()).formatted(Formatting.RED), true);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onInitialize() {
		Registry.register(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, Identifier.of(MOD_ID, "regeneration_effect"), RegenerationEnchantmentEffect.CODEC);
		Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "owner_transfer"), OwnershipCraftingRecipe.Serializer.INSTANCE);
		PayloadTypeRegistry.playC2S().register(PacketUpdateCollar.ID, PacketUpdateCollar.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PacketUpdateCollar.ID, PacketUpdateCollar::handle);
		PayloadTypeRegistry.playC2S().register(PacketStampDeed.ID, PacketStampDeed.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PacketStampDeed.ID, PacketStampDeed::handle);
		PayloadTypeRegistry.playC2S().register(PacketOpenPawsConfig.ID, PacketOpenPawsConfig.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PacketOpenPawsConfig.ID, PacketOpenPawsConfig::handle);

		PayloadTypeRegistry.playS2C().register(PacketLookAtLerped.ID, PacketLookAtLerped.CODEC);
		AccessoryRegistry.register(COLLAR_ITEM, COLLAR_ITEM);
        AccessoryRegistry.register(TAGLESS_COLLAR_ITEM, COLLAR_ITEM);

		for (int i = 0; i < PAWS_DYE_COLORS.length; i++) {
			DyeColor c = PAWS_DYE_COLORS[i];
			RegistryKey<Item> itemKey = PawsItem.getRegistryKey(c);
			PAWS_ITEMS[i] = Registry.register(Registries.ITEM, itemKey,
					new PawsItem(itemKey, c.getFireworkColor(), 0xF196CF));
            itemKey = FootPawsItem.getRegistryKey(c);
			FOOT_PAWS_ITEMS[i] = Registry.register(Registries.ITEM, itemKey,
					new FootPawsItem(itemKey, c.getFireworkColor(), 0xF196CF));
		}

		for (DyeColor c : DyeColor.values()) {
			RegistryKey<Block> blockKey = DogBedBlock.getRegistryKey(c);
			DOG_BEDS[c.ordinal()] = Registry.register(Registries.BLOCK, blockKey, new DogBedBlock(c, blockKey));
			RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockKey.getValue());
			DOG_BED_ITEMS[c.ordinal()] = Registry.register(Registries.ITEM, itemKey,
					new BedItem(DOG_BEDS[c.ordinal()], (new Item.Settings()).maxCount(1).registryKey(itemKey)));
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

		AttackEntityCallback.EVENT.register((PlayerEntity player, World world, Hand var3, Entity entity, @Nullable EntityHitResult var5) -> {
			if (world.isClient) return ActionResult.PASS;
			if (player.isSpectator()) return ActionResult.PASS;

			ServerWorld sworld = (ServerWorld) world;
			AccessoriesCapability cap = AccessoriesCapability.get(player);
			if (cap != null && sworld.getGameRules().getBoolean(ALLOW_ATTACK_OWNER)) {
				for (SlotEntryReference sr : cap.getEquipped((x) -> x.isIn(PlayerCollarsMod.COLLAR_TAG))) {
					OwnerComponent owner = sr.stack().get(OWNER_COMPONENT_TYPE);
					if (owner != null && owner.uuid().equals(entity.getUuid())) {
						// Collared players are allowed to attack owners, but have 75% damage returned to them
						player.sendMessage(Text.translatable("message.playercollars.no_attack_owner").formatted(Formatting.RED), true);
						double f = player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
						f = (f - 1) * 0.75 + 1;
						player.damage(sworld, player.getDamageSources().playerAttack(player), (float) Math.ceil(f));
						return ActionResult.PASS;
					}
				}
			}

			if (entity instanceof LeashKnotEntity ke && blockLeashKnotBreak(sworld, player, ke)) return ActionResult.FAIL;
			return ActionResult.PASS;
		});
	}
}
