package org.jlortiz.playercollars;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jlortiz.playercollars.enchantment.AudibleEnchantment;
import org.jlortiz.playercollars.enchantment.HealingEnchantment;
import org.jlortiz.playercollars.enchantment.SpikedEnchantment;
import org.jlortiz.playercollars.enchantment.TightLeashEnchantment;
import org.jlortiz.playercollars.block.DogBedBlock;
import org.jlortiz.playercollars.block.DogBowlBlock;
import org.jlortiz.playercollars.block.InvisibleFenceBlock;
import org.jlortiz.playercollars.item.*;
import org.jlortiz.playercollars.leash.LeashImpl;
import org.jlortiz.playercollars.leash.LeashProxyEntity;
import org.jlortiz.playercollars.network.ModPackets;
import org.jlortiz.playercollars.network.PawsConfigScreenHandler;
import org.jlortiz.playercollars.util.LeashedUtil;
import org.jlortiz.playercollars.util.NbtUtil;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@Mod(PlayerCollarsMod.MOD_ID)
public class PlayerCollarsMod {
    public static final String MOD_ID = "playercollars";

    // ---- Deferred Registers ----
    public static final EnchantmentCategory COLLAR_ENCHANTABLE = EnchantmentCategory.create(
            "PLAYERCOLLARS_COLLAR", item -> item instanceof org.jlortiz.playercollars.item.CollarItem);
        public static final EnchantmentCategory CLICKER_ENCHANTABLE = EnchantmentCategory.create(
            "PLAYERCOLLARS_CLICKER", item -> item instanceof org.jlortiz.playercollars.item.ClickerItem);
        public static final DeferredRegister<net.minecraft.world.item.enchantment.Enchantment> ENCHANTMENTS =
            DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS, MOD_ID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
    public static final DeferredRegister<net.minecraft.world.item.CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // ---- Sounds ----
    public static final RegistryObject<SoundEvent> CLICKER_ON = SOUNDS.register("clicker_on",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "clicker_on")));
    public static final RegistryObject<SoundEvent> CLICKER_OFF = SOUNDS.register("clicker_off",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "clicker_off")));

    // ---- Attributes ----
    public static final RegistryObject<Attribute> ATTR_CLICKER_DISTANCE = ATTRIBUTES.register("clicker_distance",
            () -> new RangedAttribute("attribute.playercollars.clicker_distance", 4, 0, 32).setSyncable(true));
    public static final RegistryObject<Attribute> ATTR_LEASH_DISTANCE = ATTRIBUTES.register("leash_distance",
            () -> new RangedAttribute("attribute.playercollars.leash_distance", 4, 2, 16).setSyncable(true));

    // ---- Items ----
    public static final RegistryObject<HealingEnchantment> HEALING_ENCHANTMENT =
            ENCHANTMENTS.register("regeneration", HealingEnchantment::new);
    public static final RegistryObject<TightLeashEnchantment> TIGHT_LEASH_ENCHANTMENT =
            ENCHANTMENTS.register("short_leash", TightLeashEnchantment::new);
    public static final RegistryObject<SpikedEnchantment> SPIKED_ENCHANTMENT =
            ENCHANTMENTS.register("thorns", SpikedEnchantment::new);
    public static final RegistryObject<AudibleEnchantment> AUDIBLE_ENCHANTMENT =
            ENCHANTMENTS.register("clicker", AudibleEnchantment::new);

    public static final RegistryObject<CollarItem> COLLAR_ITEM = ITEMS.register("collar", () -> new CollarItem(false));
    public static final RegistryObject<CollarItem> TAGLESS_COLLAR_ITEM = ITEMS.register("tagless_collar", () -> new CollarItem(true));
    public static final RegistryObject<ClickerItem> CLICKER_ITEM = ITEMS.register("clicker", ClickerItem::new);
    public static final RegistryObject<DeedItem> DEED_OF_OWNERSHIP = ITEMS.register("deed_of_ownership", DeedItem::new);
    public static final RegistryObject<Item> DEED_OF_OWNERSHIP_STAMPED = ITEMS.register("stamped_deed_of_ownership", StampedDeedItem::new);
    public static final RegistryObject<PawSetupItem> PAW_CONFIGURATION_ITEM = ITEMS.register("paw_configurator", PawSetupItem::new);
    public static final RegistryObject<CollarLockerItem> COLLAR_LOCKER_ITEM = ITEMS.register("collar_locker", CollarLockerItem::new);
    public static final RegistryObject<SpatulaItem> SPATULA_ITEM = ITEMS.register("golden_spatula", SpatulaItem::new);

    // ---- Invisible Fence ----
    public static final RegistryObject<InvisibleFenceBlock> INVISIBLE_FENCE_BLOCK = BLOCKS.register("invisible_fence",
            () -> new InvisibleFenceBlock(BlockBehaviour.Properties.of()
                    .instabreak().sound(SoundType.GLASS).noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<BlockItem> INVISIBLE_FENCE_BLOCK_ITEM = ITEMS.register("invisible_fence",
            () -> new BlockItem(INVISIBLE_FENCE_BLOCK.get(), new Item.Properties()));

    // ---- Dog Beds / Bowls (16 colors) ----
    @SuppressWarnings("unchecked")
    public static final RegistryObject<DogBedBlock>[] DOG_BED_BLOCKS = new RegistryObject[16];
    @SuppressWarnings("unchecked")
    public static final RegistryObject<BedItem>[] DOG_BED_ITEMS = new RegistryObject[16];
    @SuppressWarnings("unchecked")
    public static final RegistryObject<DogBowlBlock>[] DOG_BOWL_BLOCKS = new RegistryObject[16];
    @SuppressWarnings("unchecked")
    public static final RegistryObject<Item>[] DOG_BOWL_ITEMS = new RegistryObject[16];
    public static final RegistryObject<BlockEntityType<DogBowlBlock.DogBowlBlockEntity>> DOG_BOWL_BLOCK_ENTITY;

    // ---- Paws ----
    public static final net.minecraft.world.item.DyeColor[] PAWS_DYE_COLORS = net.minecraft.world.item.DyeColor.values();
    @SuppressWarnings("unchecked")
    public static final RegistryObject<PawsItem>[] PAWS_ITEMS = new RegistryObject[PAWS_DYE_COLORS.length];
    @SuppressWarnings("unchecked")
    public static final RegistryObject<FootPawsItem>[] FOOT_PAWS_ITEMS = new RegistryObject[PAWS_DYE_COLORS.length];

    // ---- Tags ----
    public static final TagKey<Item> COLLAR_TAG = TagKey.create(Registries.ITEM, new ResourceLocation("c", "collars"));
    public static final TagKey<Block> PAWS_ALLOW_INTERACT = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "paws_allow_interact"));
    public static final TagKey<Item> PAWS_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "paws"));
    public static final TagKey<Item> FOOT_PAWS_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "foot_paws"));

    // ---- Menus ----
    @SuppressWarnings({"unchecked","rawtypes"})
    public static final RegistryObject<MenuType<PawsConfigScreenHandler<Block>>> PAWS_BLOCK_CONFIG_MENU =
            (RegistryObject) MENUS.register("paws_block_config",
                    () -> net.minecraftforge.common.extensions.IForgeMenuType.create(
                            PawsConfigScreenHandler.PawsBlockConfigScreenHandler::new));
    @SuppressWarnings({"unchecked","rawtypes"})
    public static final RegistryObject<MenuType<PawsConfigScreenHandler<Item>>> PAWS_ITEM_CONFIG_MENU =
            (RegistryObject) MENUS.register("paws_item_config",
                    () -> net.minecraftforge.common.extensions.IForgeMenuType.create(
                            PawsConfigScreenHandler.PawsItemConfigScreenHandler::new));

    // ---- Recipe Serializer ----
    public static final RegistryObject<RecipeSerializer<?>> OWNERSHIP_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("owner_transfer", () -> OwnershipCraftingRecipe.Serializer.INSTANCE);

    public static final RegistryObject<net.minecraft.world.item.CreativeModeTab> CREATIVE_TAB =
            CREATIVE_TABS.register("group", () -> net.minecraft.world.item.CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.playercollars"))
                    .icon(() -> COLLAR_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(COLLAR_ITEM.get());
                        output.accept(TAGLESS_COLLAR_ITEM.get());
                        output.accept(CLICKER_ITEM.get());
                        output.accept(COLLAR_LOCKER_ITEM.get());
                        output.accept(PAW_CONFIGURATION_ITEM.get());
                        for (var p : PAWS_ITEMS) output.accept(p.get());
                        for (var p : FOOT_PAWS_ITEMS) output.accept(p.get());
                        output.accept(DEED_OF_OWNERSHIP.get());
                        output.accept(SPATULA_ITEM.get());
                        for (var b : DOG_BED_ITEMS) output.accept(b.get());
                        for (var b : DOG_BOWL_ITEMS) output.accept(b.get());
                        output.accept(INVISIBLE_FENCE_BLOCK_ITEM.get());
                    })
                    .build());

    // ---- Codecs ----
    public static final Codec<OwnerComponent> OWNER_COMPONENT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("uuid").forGetter(c -> c.uuid().toString()),
            Codec.STRING.fieldOf("name").forGetter(OwnerComponent::name),
            Codec.STRING.optionalFieldOf("owned").forGetter(c -> c.owned().map(UUID::toString)),
            Codec.STRING.optionalFieldOf("owned_name").forGetter(OwnerComponent::ownedName)
    ).apply(builder, (uuidStr, name, ownedStr, ownedName) ->
            new OwnerComponent(UUID.fromString(uuidStr), name, ownedStr.map(UUID::fromString), ownedName)));

    public static final Codec<List<Either<TagKey<Block>, ResourceKey<Block>>>> CAN_INTERACT_COMPONENT_CODEC = Codec.either(
            TagKey.codec(Registries.BLOCK), ResourceKey.codec(Registries.BLOCK)).listOf();

    public static final Codec<List<Either<TagKey<Item>, ResourceKey<Item>>>> HELD_ITEMS_COMPONENT_CODEC = Codec.either(
            TagKey.codec(Registries.ITEM), ResourceKey.codec(Registries.ITEM)).listOf();

    // ---- Game Rules (populated during RegisterGameRulesEvent) ----
    public static GameRules.Key<GameRules.BooleanValue> PLAYER_LEASHES_BREAK_RULE;
    public static GameRules.Key<GameRules.BooleanValue> LEASHED_PLAYERS_RIDE_ENTITIES;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_ATTACK_OWNER;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_UNLEASH_OTHER;

    static {
        net.minecraft.world.item.DyeColor[] colors = net.minecraft.world.item.DyeColor.values();
        for (net.minecraft.world.item.DyeColor c : colors) {
            int idx = c.ordinal();
            String name = c.getName();
            DOG_BOWL_BLOCKS[idx] = BLOCKS.register(name + "_dog_bowl",
                    () -> new DogBowlBlock(c, BlockBehaviour.Properties.of()
                            .sound(SoundType.STONE).strength(0.6F).noOcclusion().pushReaction(PushReaction.DESTROY)));
            DOG_BOWL_ITEMS[idx] = ITEMS.register(name + "_dog_bowl",
                    () -> new BlockItem(DOG_BOWL_BLOCKS[c.ordinal()].get(), new Item.Properties()));
            DOG_BED_BLOCKS[idx] = BLOCKS.register(name + "_dog_bed",
                    () -> new DogBedBlock(c, BlockBehaviour.Properties.of()
                            .sound(SoundType.WOOL).strength(0.2F).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY)));
            DOG_BED_ITEMS[idx] = ITEMS.register(name + "_dog_bed",
                    () -> new BedItem(DOG_BED_BLOCKS[c.ordinal()].get(), new Item.Properties().stacksTo(1)));
        }
        DOG_BOWL_BLOCK_ENTITY = BLOCK_ENTITIES.register("dog_bowl", () -> {
            DogBowlBlock[] bowls = new DogBowlBlock[DOG_BOWL_BLOCKS.length];
            for (int i = 0; i < DOG_BOWL_BLOCKS.length; i++) bowls[i] = DOG_BOWL_BLOCKS[i].get();
            return BlockEntityType.Builder.of(DogBowlBlock.DogBowlBlockEntity::new, bowls).build(null);
        });
        for (int i = 0; i < PAWS_DYE_COLORS.length; i++) {
            net.minecraft.world.item.DyeColor c = PAWS_DYE_COLORS[i];
            PAWS_ITEMS[i] = ITEMS.register(c.getName() + "_paws", () -> new PawsItem(c.getFireworkColor(), 0xF196CF));
            FOOT_PAWS_ITEMS[i] = ITEMS.register(c.getName() + "_foot_paws", () -> new FootPawsItem(c.getFireworkColor(), 0xF196CF));
        }
    }

    public PlayerCollarsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        ENCHANTMENTS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        SOUNDS.register(modBus);
        ATTRIBUTES.register(modBus);
        MENUS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        CREATIVE_TABS.register(modBus);
        modBus.addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        ModPackets.register();


        // Register game rules directly - Forge 47.x has no RegisterGameRulesEvent
        PLAYER_LEASHES_BREAK_RULE = GameRules.register("playerLeashesBreak",
                GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
        LEASHED_PLAYERS_RIDE_ENTITIES = GameRules.register("leashedPlayersRideEntities",
                GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
        ALLOW_ATTACK_OWNER = GameRules.register("playerAllowAttackOwner",
                GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
        ALLOW_UNLEASH_OTHER = GameRules.register("allowUnleashUnownedPlayer",
                GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
    }




    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Level world = (Level) event.getLevel();
        if (world.isClientSide) return;
        Player player = event.getPlayer();
        if (player.isSpectator()) return;
        Entity leashHolder = ((LeashImpl) player).leashplayers$getProxyLeashHolder();
        if (leashHolder instanceof LeashFenceKnotEntity knot && event.getPos().equals(knot.blockPosition())) {
            player.displayClientMessage(Component.translatable("message.playercollars.no_break_fence")
                    .withStyle(ChatFormatting.RED), true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        Level world = player.level();
        if (world.isClientSide) return;
        if (player.isSpectator()) return;
        Entity target = event.getTarget();
        if (world.getGameRules().getBoolean(ALLOW_ATTACK_OWNER) && target instanceof Player) {
            final OwnerComponent[] collarMatch = {null};
            CuriosApi.getCuriosInventory(player).ifPresent(h ->
                h.findCurios(s -> s.is(COLLAR_TAG)).stream()
                    .map(sr -> NbtUtil.getDeedOwner(sr.stack()))
                    .filter(Objects::nonNull)
                    .filter(c -> c.uuid().equals(target.getUUID()) &&
                            (c.owned().isEmpty() || c.owned().get().equals(player.getUUID())))
                    .findFirst().ifPresent(c -> collarMatch[0] = c));
            if (collarMatch[0] != null) {
                player.displayClientMessage(Component.translatable("message.playercollars.no_attack_owner")
                        .withStyle(ChatFormatting.RED), true);
                double baseDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                baseDamage = (baseDamage - 1) * 0.75 + 1;
                player.hurt(player.damageSources().playerAttack(player), (float) Math.ceil(baseDamage));
            }
        }
        if (target instanceof LeashFenceKnotEntity ke && blockLeashKnotBreak(world, player, ke)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        Entity entity = event.getTarget();
        if (!(entity instanceof LeashImpl impl)) return;
        InteractionResult result = impl.leashplayers$interact(serverPlayer, event.getHand());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    // ---- Static helpers ----

    public static boolean isAcceptableEnchant(Item item, net.minecraft.world.item.enchantment.Enchantment enchantment) {
        return (item instanceof CollarItem && CollarItem.isAcceptableEnchantment(enchantment)) ||
               (item instanceof ClickerItem && ClickerItem.isAcceptableEnchantment(enchantment));
    }

    public static @Nullable ItemStack filterStacksByOwner(
            List<SlotResult> stacks,
            UUID ownerUuid, UUID entity) {
        for (var sr : stacks) {
            ItemStack is = sr.stack();
            if (is.getItem() instanceof CollarItem) {
                var owner = NbtUtil.getDeedOwner(is);
                if (owner != null && owner.uuid().equals(ownerUuid) &&
                        (owner.owned().isEmpty() || owner.owned().get().equals(entity))) {
                    return is;
                }
            }
        }
        return null;
    }

    public static InteractionResult pullPlayerTowards(ServerPlayer plr, Vec3 towards, double minDist,
            double maxDist, UnaryOperator<Double> getFactor) {
        Vec3 vecTo = towards.subtract(plr.position());
        double distance = vecTo.length();
        if (distance < minDist) return InteractionResult.PASS;
        if (distance > maxDist) return InteractionResult.FAIL;
        plr.addDeltaMovement(vecTo.scale(Math.abs(getFactor.apply(distance))));
        plr.connection.send(new ClientboundSetEntityMotionPacket(plr));
        plr.hurtMarked = false;
        return InteractionResult.SUCCESS;
    }

    public static boolean blockLeashKnotBreak(Level world, Player player, LeashFenceKnotEntity entity) {
        if (entity.equals(((LeashImpl) player).leashplayers$getProxyLeashHolder())) {
            player.displayClientMessage(Component.translatable("message.playercollars.no_break_fence")
                    .withStyle(ChatFormatting.RED), true);
            return true;
        }
        if (!world.getGameRules().getBoolean(ALLOW_UNLEASH_OTHER)) {
            List<Mob> list = LeashedUtil.collectLeashablesAround(world, entity.blockPosition(),
                    e -> entity.equals(e.getLeashHolder()));
            for (Mob l : list) {
                if (!(l instanceof LeashProxyEntity le)) continue;
                LivingEntity leashTarget = le.getLeashTarget();
                var collars = CuriosApi.getCuriosInventory(leashTarget)
                        .map(h -> h.findCurios(s -> s.is(COLLAR_TAG)))
                        .stream().flatMap(java.util.Collection::stream)
                        .map(sr -> NbtUtil.getDeedOwner(sr.stack()))
                        .filter(Objects::nonNull)
                        .filter(c -> c.owned().orElseGet(leashTarget::getUUID).equals(leashTarget.getUUID()));
                if (!collars.allMatch(c -> player.getUUID().equals(c.uuid()))) {
                    player.displayClientMessage(
                            Component.translatable("message.playercollars.no_break_fence_other",
                                    le.getLeashTarget().getName()).withStyle(ChatFormatting.RED), true);
                    return true;
                }
            }
        }
        return false;
    }
}
