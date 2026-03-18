package org.jlortiz.playercollars.network;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class PawsConfigScreenHandler<T> extends AbstractContainerMenu {
    private final Container inventory;
    public final List<Either<TagKey<T>, ResourceKey<T>>> data;
    protected final Level world;
    public List<Either<TagKey<T>, ResourceKey<T>>> listToDisplay;
    protected ItemStack[] pawsStacks;

    protected PawsConfigScreenHandler(MenuType<? extends PawsConfigScreenHandler<T>> type, int syncId,
                                       Inventory playerInventory, List<Either<TagKey<T>, ResourceKey<T>>> data) {
        super(type, syncId);
        this.inventory = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(this);
            }
        };
        this.data = (data == null) ? new ArrayList<>() : new ArrayList<>(data);
        this.listToDisplay = this.data;
        this.world = playerInventory.player.level();
        this.inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(this.inventory, 0, 175, 108) {
            @Override
            public boolean mayPickup(Player player) { return false; }

            @Override
            public ItemStack remove(int amount) {
                getSlot(0).set(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }

            @Override
            public void set(ItemStack stack) {
                inventory.setItem(0, stack.copyWithCount(1));
                setChanged();
            }
        });

        for (int j = 0; j < 3; j++)
            for (int k = 0; k < 9; k++)
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 7 + k * 18, 140 + j * 18));

        for (int j = 0; j < 9; j++)
            this.addSlot(new Slot(playerInventory, j, 7 + j * 18, 198));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        this.inventory.setItem(0, getSlot(slot).getItem().copyWithCount(1));
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
        if (slotIndex == 0) {
            ItemStack is = getCarried();
            if (is == null) is = ItemStack.EMPTY;
            this.inventory.setItem(0, is.copyWithCount(1));
            return;
        }
        super.clicked(slotIndex, button, actionType, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0) return false;
        if (this.inventory.getItem(0).isEmpty()) {
            if (id >= this.data.size()) return false;
            this.data.remove(id);
        } else {
            if (id >= this.listToDisplay.size()) return false;
            this.data.add(this.listToDisplay.get(id));
        }
        return true;
    }

    public void setPawsStack(ItemStack[] is) {
        if (this.pawsStacks == null) this.pawsStacks = is;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        ItemStack is = container.getItem(0);
        this.listToDisplay = is.isEmpty() ? this.data : genForItem(is.getItem());
    }

    protected abstract List<Either<TagKey<T>, ResourceKey<T>>> genForItem(Item item);

    public abstract net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<T>> getRegistryKey();

    public static class PawsBlockConfigScreenHandler extends PawsConfigScreenHandler<Block> {
        public PawsBlockConfigScreenHandler(int syncId, Inventory inv, List<Either<TagKey<Block>, ResourceKey<Block>>> data) {
            super(PlayerCollarsMod.PAWS_BLOCK_CONFIG_MENU.get(), syncId, inv, data);
        }

        public PawsBlockConfigScreenHandler(int syncId, Inventory inv, FriendlyByteBuf buf) {
            this(syncId, inv, NbtUtil.readCanInteract(buf));
        }

        @Override
        protected List<Either<TagKey<Block>, ResourceKey<Block>>> genForItem(Item item) {
            if (!(item instanceof BlockItem bi)) return List.of();
            var entry = this.world.registryAccess().registryOrThrow(Registries.BLOCK).getHolderOrThrow(
                    ResourceKey.create(Registries.BLOCK, ForgeRegistries.BLOCKS.getKey(bi.getBlock())));
            Stream<Either<TagKey<Block>, ResourceKey<Block>>> tags = entry.tags().map(Either::left);
            if (entry.unwrapKey().isPresent())
                tags = Stream.concat(Stream.of(Either.right(entry.unwrapKey().get())), tags);
            return tags.toList();
        }

        @Override
        public net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<Block>> getRegistryKey() {
            return Registries.BLOCK;
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            if (this.pawsStacks != null)
                for (ItemStack ps : this.pawsStacks) NbtUtil.setCanInteract(ps, this.data);
        }
    }

    public static class PawsItemConfigScreenHandler extends PawsConfigScreenHandler<Item> {
        public PawsItemConfigScreenHandler(int syncId, Inventory inv, List<Either<TagKey<Item>, ResourceKey<Item>>> data) {
            super(PlayerCollarsMod.PAWS_ITEM_CONFIG_MENU.get(), syncId, inv, data);
        }

        public PawsItemConfigScreenHandler(int syncId, Inventory inv, FriendlyByteBuf buf) {
            this(syncId, inv, NbtUtil.readHeldItems(buf));
        }

        @Override
        protected List<Either<TagKey<Item>, ResourceKey<Item>>> genForItem(Item item) {
            var entry = this.world.registryAccess().registryOrThrow(Registries.ITEM).getHolderOrThrow(
                    ResourceKey.create(Registries.ITEM, ForgeRegistries.ITEMS.getKey(item)));
            Stream<Either<TagKey<Item>, ResourceKey<Item>>> tags = entry.tags().map(Either::left);
            if (entry.unwrapKey().isPresent())
                tags = Stream.concat(Stream.of(Either.right(entry.unwrapKey().get())), tags);
            return tags.toList();
        }

        @Override
        public net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<Item>> getRegistryKey() {
            return Registries.ITEM;
        }

        @Override
        public void removed(Player player) {
            super.removed(player);
            if (this.pawsStacks != null)
                for (ItemStack ps : this.pawsStacks) NbtUtil.setHeldItems(ps, this.data);
        }
    }
}
