package org.jlortiz.playercollars.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import org.jlortiz.playercollars.item.CollarItem;
import org.jlortiz.playercollars.network.ModPackets;
import org.jlortiz.playercollars.network.PacketUpdateCollar;
import org.jlortiz.playercollars.util.NbtUtil;

import java.util.UUID;

public class CollarDyeScreen extends Screen {
    private final ItemStack is;
    private final boolean shouldPaw;
    private final int initColor, initPaw;
    private final UUID ownUUID;
    private final String ownerName;
    private UUID ownerUUID;

    public CollarDyeScreen(ItemStack is, UUID player) {
        super(is.getHoverName());
        this.is = is;
        this.ownUUID = player;
        this.initColor = ((DyeableLeatherItem) is.getItem()).getColor(is);
        this.initPaw = ((CollarItem) is.getItem()).getTagColor(is);
        this.shouldPaw = is.getItem() instanceof CollarItem ci && !ci.tagless;
        var owner = NbtUtil.getDeedOwner(is);
        this.ownerUUID = owner == null ? null : owner.uuid();
        this.ownerName = owner == null ? null : owner.name();
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2 - 30;

        EditBox dyeField = new EditBox(this.font, x - 30, this.shouldPaw ? y : y + 25, 100, 20, Component.empty());
        dyeField.setMaxLength(6);
        dyeField.setResponder(s -> updateTextField(false, s));
        dyeField.setFilter(s -> { try { Integer.parseInt(s, 16); } catch (NumberFormatException e) { return s.isEmpty(); } return true; });
        dyeField.setValue(Integer.toHexString(this.initColor));
        this.addRenderableWidget(dyeField);

        if (this.shouldPaw) {
            EditBox pawField = new EditBox(this.font, x - 30, y + 25, 100, 20, Component.empty());
            pawField.setMaxLength(6);
            pawField.setResponder(s -> updateTextField(true, s));
            pawField.setFilter(s -> { try { Integer.parseInt(s, 16); } catch (NumberFormatException e) { return s.isEmpty(); } return true; });
            pawField.setValue(Integer.toHexString(this.initPaw));
            this.addRenderableWidget(pawField);
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> {
            PacketUpdateCollar.OwnerState os = this.ownerUUID == null
                    ? PacketUpdateCollar.OwnerState.DEL
                    : (this.ownerUUID.equals(this.ownUUID) ? PacketUpdateCollar.OwnerState.ADD : PacketUpdateCollar.OwnerState.NOP);
            ModPackets.CHANNEL.sendToServer(new PacketUpdateCollar(this.is, os));
            onClose();
        }).bounds(x + 5, y + 50, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), btn -> {
            NbtUtil.setColor(this.is, this.initColor);
            NbtUtil.setTagColor(this.is, this.initPaw);
            onClose();
        }).bounds(x - 80, y + 50, 75, 20).build());

        Button ownerButton = Button.builder(Component.empty(), this::updateOwner)
                .bounds(x - 80, y + 72, 160, 20).build();
        if (this.ownerUUID == null) {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.become_owner"));
        } else if (this.ownerUUID.equals(this.ownUUID)) {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.remove_owner"));
        } else {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.owner", this.ownerName));
            ownerButton.active = false;
        }
        this.addRenderableWidget(ownerButton);
    }

    private void updateOwner(Button btn) {
        if (this.ownerUUID == null) {
            this.ownerUUID = this.ownUUID;
            btn.setMessage(Component.translatable("item.playercollars.collar.remove_owner"));
        } else {
            this.ownerUUID = null;
            btn.setMessage(Component.translatable("item.playercollars.collar.become_owner"));
        }
    }

    private void updateTextField(boolean paw, String s) {
        int col;
        try { col = Integer.parseInt(s, 16); } catch (NumberFormatException e) { return; }
        if (paw) NbtUtil.setTagColor(this.is, col);
        else NbtUtil.setColor(this.is, col);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        context.drawString(this.font, Component.translatable("item.playercollars.collar"),
                this.width / 2 - 75, this.height / 2 + (this.shouldPaw ? -25 : 1), -1, true);
        if (this.shouldPaw)
            context.drawString(this.font, Component.translatable("item.playercollars.collar.paw"),
                    this.width / 2 - 75, this.height / 2 + 1, -1, true);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
