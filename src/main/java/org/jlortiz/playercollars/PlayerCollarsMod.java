package org.jlortiz.playercollars;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Uuids;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.jlortiz.playercollars.block.DogBedBlock;
import org.jlortiz.playercollars.block.DogBowlBlock;
import org.jlortiz.playercollars.block.InvisibleFenceBlock;
import org.jlortiz.playercollars.item.ClickerItem;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.item.CollarLockerItem;
import org.jlortiz.playercollars.item.DeedItem;
import org.jlortiz.playercollars.item.FootPawsItem;
import org.jlortiz.playercollars.item.OwnershipCraftingRecipe;
import org.jlortiz.playercollars.item.PawSetupItem;
import org.jlortiz.playercollars.item.PawsItem;
import org.jlortiz.playercollars.item.SpatulaItem;
import org.jlortiz.playercollars.item.StampedDeedItem;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.jlortiz.playercollars.network.PacketOpenPawsConfig;
import org.jlortiz.playercollars.network.PacketStampDeed;
import org.jlortiz.playercollars.network.PacketUpdateCollar;
import org.jlortiz.playercollars.network.PawsConfigScreenHandler;
import org.jlortiz.playercollars.util.LeashedUtil;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class PlayerCollarsMod implements ModInitializer {
    public static final String MOD_ID = "playercollars";
    public static final CollarItem COLLAR_ITEM = Registry.register(
            Registries.ITEM, Identifier.of(MOD_ID, "collar"),
            new CollarItem(false)
    );
    public static final CollarItem TAGLESS_COLLAR_ITEM = Registry.register(
            Registries.ITEM, Identifier.of(MOD_ID, "tagless_collar"),
            new CollarItem(true)
    );
    public static final ClickerItem CLICKER_ITEM = Registry.register(
            Registries.ITEM, Identifier.of(MOD_ID, "clicker"),
            new ClickerItem()
    );
    public static final DeedItem DEED_OF_OWNERSHIP = Registry.register(
            Registries.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "deed_of_ownership"),
            new DeedItem()
    );
    public static final Item DEED_OF_OWNERSHIP_STAMPED = Registry.register(
            Registries.ITEM, Identifier.of(PlayerCollarsMod.MOD_ID, "stamped_deed_of_ownership"),
            new StampedDeedItem()
    );
    public static final InvisibleFenceBlock INVISIBLE_FENCE_BLOCK = Registry.register(
            Registries.BLOCK, InvisibleFenceBlock.REGISTRY_KEY,
            new InvisibleFenceBlock(AbstractBlock.Settings.create().breakInstantly().sounds(BlockSoundGroup.GLASS).nonOpaque().dynamicBounds())
    );
    public static final BlockItem INVISIBLE_FENCE_BLOCK_ITEM = Registry.register(
            Registries.ITEM, InvisibleFenceBlock.ITEM_REGISTRY_KEY,
            new BlockItem(INVISIBLE_FENCE_BLOCK, new Item.Settings())
    );
    public static final PawSetupItem PAW_CONFIGURATION_ITEM = Registry.register(
            Registries.ITEM, Identifier.of(MOD_ID, "paw_configurator"),
            new PawSetupItem()
    );
    public static final CollarLockerItem COLLAR_LOCKER_ITEM = Registry.register(
            Registries.ITEM, Identifier.of(MOD_ID, "collar_locker"),
            new CollarLockerItem()
    );
    public static final SpatulaItem SPATULA_ITEM = Registry.register(
            Registries.ITEM, Identifier.of(MOD_ID, "golden_spatula"),
            new SpatulaItem()
    );

    public static final SoundEvent CLICKER_ON = Registry.register(
            Registries.SOUND_EVENT, Identifier.of(MOD_ID, "clicker_on"),
            SoundEvent.of(Identifier.of(MOD_ID, "clicker_on"))
    );
    public static final SoundEvent CLICKER_OFF = Registry.register(
            Registries.SOUND_EVENT, Identifier.of(MOD_ID, "clicker_off"),
            SoundEvent.of(Identifier.of(MOD_ID, "clicker_off"))
    );

    public static final Codec<OwnerComponent> OWNER_COMPONENT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(OwnerComponent::uuid),
            Codec.STRING.fieldOf("name").forGetter(OwnerComponent::name),
            Uuids.CODEC.optionalFieldOf("owned").forGetter(OwnerComponent::owned),
            Codec.STRING.optionalFieldOf("owned_name").forGetter(OwnerComponent::ownedName)
    ).apply(builder, OwnerComponent::new));

    public static final Codec<List<Either<TagKey<Block>, RegistryKey<Block>>>> CAN_INTERACT_COMPONENT_CODEC = Codec.either(
            TagKey.codec(RegistryKeys.BLOCK),
            RegistryKey.createCodec(RegistryKeys.BLOCK)
    ).listOf();

    public static final Codec<List<Either<TagKey<Item>, RegistryKey<Item>>>> HELD_ITEMS_COMPONENT_CODEC = Codec.either(
            TagKey.codec(RegistryKeys.ITEM),
            RegistryKey.createCodec(RegistryKeys.ITEM)
    ).listOf();

    public static final EntityAttribute ATTR_CLICKER_DISTANCE = Registry.register(
            Registries.ATTRIBUTE, Identifier.of(MOD_ID, "clicker_distance"),
            new ClampedEntityAttribute("attribute.playercollars.clicker_distance", 4, 0, 32)
    );
    public static final EntityAttribute ATTR_LEASH_DISTANCE = Registry.register(
            Registries.ATTRIBUTE, Identifier.of(PlayerCollarsMod.MOD_ID, "leash_distance"),
            new ClampedEntityAttribute("attribute.playercollars.leash_distance", 4, 2, 16)
    );

    public static final GameRules.Key<GameRules.BooleanRule> PLAYER_LEASHES_BREAK_RULE = GameRuleRegistry.register(
            "playerLeashesBreak", GameRules.Category.PLAYER,
            GameRuleFactory.createBooleanRule(true)
    );
    public static final GameRules.Key<GameRules.BooleanRule> LEASHED_PLAYERS_RIDE_ENTITIES = GameRuleRegistry.register(
            "leashedPlayersRideEntities", GameRules.Category.PLAYER,
            GameRuleFactory.createBooleanRule(false)
    );
    public static final GameRules.Key<GameRules.BooleanRule> ALLOW_ATTACK_OWNER = GameRuleRegistry.register(
            "playerAllowAttackOwner", GameRules.Category.PLAYER,
            GameRuleFactory.createBooleanRule(false)
    );
    public static final GameRules.Key<GameRules.BooleanRule> ALLOW_UNLEASH_OTHER = GameRuleRegistry.register(
            "allowUnleashUnownedPlayer", GameRules.Category.PLAYER,
            GameRuleFactory.createBooleanRule(true)
    );

    public static final DogBedBlock[] DOG_BEDS = new DogBedBlock[DyeColor.values().length];
    public static final BedItem[] DOG_BED_ITEMS = new BedItem[DyeColor.values().length];
    public static final TagKey<Item> COLLAR_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "collars"));

    public static final DyeColor[] PAWS_DYE_COLORS = {
            DyeColor.WHITE, DyeColor.LIGHT_GRAY,
            DyeColor.GRAY, DyeColor.BLACK,
            DyeColor.BLUE, DyeColor.RED,
            DyeColor.PURPLE
    };
    public static final PawsItem[] PAWS_ITEMS = new PawsItem[PAWS_DYE_COLORS.length];
    public static final TagKey<Block> PAWS_ALLOW_INTERACT = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "paws_allow_interact"));
    public static final TagKey<Item> PAWS_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "paws"));
    public static final FootPawsItem[] FOOT_PAWS_ITEMS = new FootPawsItem[PAWS_DYE_COLORS.length];
    public static final TagKey<Item> FOOT_PAWS_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "foot_paws"));

    public static final DogBowlBlock[] DOG_BOWLS = new DogBowlBlock[DyeColor.values().length];
    public static final Item[] DOG_BOWL_ITEMS = new Item[DyeColor.values().length];
    public static final BlockEntityType<DogBowlBlock.DogBowlBlockEntity> DOG_BOWL_BLOCK_ENTITY;
    public static final ItemGroup GROUP;

    public static final ExtendedScreenHandlerType<PawsConfigScreenHandler<Block>> PAWS_BLOCK_CONFIG_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(
            PawsConfigScreenHandler.PawsBlockConfigScreenHandler::new
    );
    public static final ExtendedScreenHandlerType<PawsConfigScreenHandler<Item>> PAWS_ITEM_CONFIG_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(
            PawsConfigScreenHandler.PawsItemConfigScreenHandler::new
    );

    static {
        for (DyeColor c : DyeColor.values()) {
            DOG_BOWLS[c.ordinal()] = Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, c.getName() + "_dog_bowl"),
                    new DogBowlBlock(c, AbstractBlock.Settings.create()
                            .sounds(BlockSoundGroup.STONE)
                            .strength(0.6F)
                            .nonOpaque()
                            .pistonBehavior(PistonBehavior.DESTROY)));
            DOG_BOWL_ITEMS[c.ordinal()] = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, c.getName() + "_dog_bowl"),
                    new BlockItem(DOG_BOWLS[c.ordinal()], new Item.Settings()));
        }

        DOG_BOWL_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "dog_bowl"),
                BlockEntityType.Builder.create(DogBowlBlock.DogBowlBlockEntity::new, DOG_BOWLS).build(null)
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

    public static ItemStack filterStacksByOwner(List<Pair<SlotReference, ItemStack>> stacks, UUID ownerUuid, UUID entity) {
        for (Pair<SlotReference, ItemStack> p : stacks) {
            ItemStack is = p.getRight();
            if (is.getItem() instanceof CollarItem) {
                Pair<UUID, String> owner = NbtUtil.getOwner(is);
                if (owner != null && owner.getLeft().equals(ownerUuid)) {
                    return is;
                }
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

    public static boolean blockLeashKnotBreak(World world, PlayerEntity player, LeashKnotEntity entity) {
        if (entity.equals(((LeashImpl) player).leashplayers$getProxyLeashHolder())) {
            player.sendMessage(Text.translatable("message.playercollars.no_break_fence").formatted(Formatting.RED), true);
            return true;
        }
        if (!world.getGameRules().getBoolean(ALLOW_UNLEASH_OTHER)) {
            List<MobEntity> list = LeashedUtil.collectLeashablesAround(world, entity.getBlockPos(), (e) -> entity.equals(e.getHoldingEntity()));
            for (MobEntity l : list) {
                if (!(l instanceof LeashProxyEntity le)) continue;
                LivingEntity leashTarget = le.getLeashTarget();
                Stream<Pair<UUID, String>> collars = TrinketsApi.getTrinketComponent(leashTarget)
                        .map((x) -> x.getEquipped((y) -> y.isIn(PlayerCollarsMod.COLLAR_TAG)))
                        .stream()
                        .flatMap(x ->
                                x.stream()
                                        .map((p) -> NbtUtil.getOwner(p.getRight()))
                                        .filter(Objects::nonNull)
                                        .filter((c) -> c.getLeft().equals(leashTarget.getUuid()))
                        );
                if (!collars.allMatch((c) -> player.getUuid().equals(c.getLeft()))) {
                    player.sendMessage(Text.translatable("message.playercollars.no_break_fence_other", le.getLeashTarget().getName())
                            .formatted(Formatting.RED), true);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onInitialize() {
        Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "owner_transfer"), OwnershipCraftingRecipe.Serializer.INSTANCE);
        ServerPlayNetworking.registerGlobalReceiver(PacketUpdateCollar.TYPE, PacketUpdateCollar::handle);
        ServerPlayNetworking.registerGlobalReceiver(PacketStampDeed.TYPE, PacketStampDeed::handle);
        ServerPlayNetworking.registerGlobalReceiver(PacketOpenPawsConfig.TYPE, PacketOpenPawsConfig::handle);

        TrinketsApi.registerTrinket(PlayerCollarsMod.COLLAR_ITEM, PlayerCollarsMod.COLLAR_ITEM);
        TrinketsApi.registerTrinket(PlayerCollarsMod.TAGLESS_COLLAR_ITEM, PlayerCollarsMod.COLLAR_ITEM);

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
                    new DogBedBlock(c, AbstractBlock.Settings.create()
                            .sounds(BlockSoundGroup.WOOL)
                            .strength(0.2F)
                            .nonOpaque()
                            .burnable()
                            .pistonBehavior(PistonBehavior.DESTROY)));
            DOG_BED_ITEMS[c.ordinal()] = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, c.getName() + "_dog_bed"),
                    new BedItem(DOG_BEDS[c.ordinal()], (new Item.Settings()).maxCount(1)));
        }

        PlayerBlockBreakEvents.BEFORE.register((World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) -> {
            if (world.isClient) return true;
            if (player.isSpectator()) return true;
            Entity leashHolderEntity = ((LeashImpl) player).leashplayers$getProxyLeashHolder();
            if (leashHolderEntity instanceof LeashKnotEntity knot && pos.equals(knot.getBlockPos())) {
                player.sendMessage(Text.translatable("message.playercollars.no_break_fence").formatted(Formatting.RED), true);
                return false;
            }
            return true;
        });

        // TODO 2026-03-02 (solonovamax): I'm pretty sure this code currently does nothing? as of right now I'm hijacking the thorns code elsewhere.
        AttackEntityCallback.EVENT.register((PlayerEntity player, World world, Hand var3, Entity entity, @Nullable EntityHitResult var5) -> {
            if (world.isClient) return ActionResult.PASS;
            if (player.isSpectator()) return ActionResult.PASS;
            if (world.getGameRules().getBoolean(ALLOW_ATTACK_OWNER) && entity instanceof PlayerEntity &&
                // TODO 2026-02-11 (solonovamax): this feels slow
                TrinketsApi.getTrinketComponent(player)
                        .map((component) -> component.getEquipped((stack) -> stack.isIn(PlayerCollarsMod.COLLAR_TAG)))
                        .map((stacks) -> PlayerCollarsMod.filterStacksByOwner(stacks, entity.getUuid(), player.getUuid()))
                        .isPresent()) {
                // Collared players are allowed to attack bad owners, but have 75% damage returned to them
                player.sendMessage(Text.translatable("message.playercollars.no_attack_owner").formatted(Formatting.RED), true);
                double baseDamage = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                baseDamage = (baseDamage - 1) * 0.75 + 1;
                player.damage(player.getDamageSources().playerAttack(player), (float) Math.ceil(baseDamage));
                return ActionResult.PASS;
            }

            if (entity instanceof LeashKnotEntity ke && blockLeashKnotBreak(world, player, ke)) return ActionResult.FAIL;
            return ActionResult.PASS;
        });
    }
}
