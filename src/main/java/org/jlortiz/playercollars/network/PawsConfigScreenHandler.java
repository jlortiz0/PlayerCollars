package org.jlortiz.playercollars.network;

import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jlortiz.playercollars.PlayerCollarsMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class PawsConfigScreenHandler<T extends ItemConvertible> extends ScreenHandler {
    private final Inventory inventory;
    public final List<Either<TagKey<T>, RegistryKey<T>>> data;
    public List<Either<TagKey<T>, RegistryKey<T>>> listToDisplay;
    protected ItemStack[] pawsStacks;

    public PawsConfigScreenHandler(ScreenHandlerType<? extends PawsConfigScreenHandler<T>> id, int syncId,
                                   PlayerInventory playerInventory, List<Either<TagKey<T>, RegistryKey<T>>> data) {
        super(id, syncId);
        this.inventory = new SimpleInventory(1) {
            @Override
            public void markDirty() {
                super.markDirty();
                PawsConfigScreenHandler.this.onContentChanged(this);
            }
        };
        this.data = (data == null) ? new ArrayList<>() : new ArrayList<>(data);
        this.listToDisplay = data;
        inventory.onOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 175, 108) {
            @Override
            public boolean canTakePartial(PlayerEntity playerEntity) {
                return false;
            }

            @Override
            public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
                setStack(ItemStack.EMPTY);
                return Optional.of(ItemStack.EMPTY);
            }

            @Override
            public ItemStack takeStack(int amount) {
                setStack(ItemStack.EMPTY);
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack insertStack(ItemStack stack) {
                this.inventory.setStack(0, stack.copyWithCount(1));
                return stack;
            }
        });

        for(int j = 0; j < 3; ++j) {
            for(int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 7 + k * 18, 140 + j * 18));
            }
        }

        for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 7 + j * 18, 198));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        this.inventory.setStack(0, getSlot(slot).getStack().copyWithCount(1));
        return ItemStack.EMPTY;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex == 0) {
            ItemStack is = this.getCursorStack();
            if (is == null) is = ItemStack.EMPTY;
            this.inventory.setStack(0, is.copyWithCount(1));
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id < 0) return false;
        if (inventory.getStack(0).isEmpty()) {
            if (id >= data.size()) return false;
            data.remove(id);
        } else {
            if (id >= listToDisplay.size()) return false;
            data.add(listToDisplay.get(id));
        }
        return true;
    }

    public void setPawsStack(ItemStack[] is) {
        if (pawsStacks == null)
            pawsStacks = is;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        ItemStack is = inventory.getStack(0);
        this.listToDisplay = is.isEmpty() ? data : genForItem(is.getItem());
    }

    protected abstract List<Either<TagKey<T>, RegistryKey<T>>> genForItem(Item item);

    public abstract RegistryKey<Registry<T>> getRegistryKey();

    public static class PawsBlockConfigScreenHandler extends PawsConfigScreenHandler<Block> {
        public PawsBlockConfigScreenHandler(int syncId, PlayerInventory playerInventory, List<Either<TagKey<Block>, RegistryKey<Block>>> data) {
            super(PlayerCollarsMod.PAWS_BLOCK_CONFIG_SCREEN_HANDLER, syncId, playerInventory, data);
        }

        protected List<Either<TagKey<Block>, RegistryKey<Block>>> genForItem(Item item) {
            if (!(item instanceof BlockItem bi)) return List.of();
            RegistryEntry<Block> entry = MinecraftClient.getInstance().world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK).getEntry(bi.getBlock());
            Stream<Either<TagKey<Block>, RegistryKey<Block>>> tags = entry.streamTags().map(Either::left);
            if (entry.getKey().isPresent()) {
                tags = Stream.concat(Stream.of(Either.right(entry.getKey().get())), tags);
            }
            return tags.toList();
        }

        @Override
        public RegistryKey<Registry<Block>> getRegistryKey() {
            return RegistryKeys.BLOCK;
        }

        @Override
        public void onClosed(PlayerEntity player) {
            super.onClosed(player);
            if (pawsStacks != null)
                for (ItemStack ps : pawsStacks)
                    ps.set(PlayerCollarsMod.CAN_INTERACT_COMPONENT_TYPE, data.isEmpty() ? null : data);
        }
    }

    public static class PawsItemConfigScreenHandler extends PawsConfigScreenHandler<Item> {
        public PawsItemConfigScreenHandler(int syncId, PlayerInventory playerInventory, List<Either<TagKey<Item>, RegistryKey<Item>>> data) {
            super(PlayerCollarsMod.PAWS_ITEM_CONFIG_SCREEN_HANDLER, syncId, playerInventory, data);
        }

        protected List<Either<TagKey<Item>, RegistryKey<Item>>> genForItem(Item item) {
            RegistryEntry<Item> entry = MinecraftClient.getInstance().world.getRegistryManager().getOrThrow(RegistryKeys.ITEM).getEntry(item);
            Stream<Either<TagKey<Item>, RegistryKey<Item>>> tags = entry.streamTags().map(Either::left);
            if (entry.getKey().isPresent()) {
                tags = Stream.concat(Stream.of(Either.right(entry.getKey().get())), tags);
            }
            return tags.toList();
        }

        @Override
        public RegistryKey<Registry<Item>> getRegistryKey() {
            return RegistryKeys.ITEM;
        }

        @Override
        public void onClosed(PlayerEntity player) {
            super.onClosed(player);
            if (pawsStacks != null)
                for (ItemStack ps : pawsStacks)
                    ps.set(PlayerCollarsMod.HELD_ITEMS_COMPONENT_TYPE, data.isEmpty() ? null : data);
        }
    }
}
