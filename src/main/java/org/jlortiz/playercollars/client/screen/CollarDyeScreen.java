package org.jlortiz.playercollars.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jlortiz.playercollars.item.CollarItem;
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
        super(is.getName());
        this.is = is;
        this.ownUUID = player;
        this.initColor = NbtUtil.getColor(is);
        this.initPaw = NbtUtil.getPawColor(is);
        this.shouldPaw = is.getItem() instanceof CollarItem ci && !ci.tagless;
        var owner = NbtUtil.getOwner(is);
        this.ownerUUID = owner == null ? null : owner.getLeft();
        this.ownerName = owner == null ? null : owner.getRight();
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2 - 30;

        TextFieldWidget dyeField = new TextFieldWidget(this.textRenderer, x - 30, this.shouldPaw ? y : y + 25, 100, 20, Text.empty());
        dyeField.setMaxLength(6);
        dyeField.setChangedListener((s) -> updateTextField(false, s));
        dyeField.setTextPredicate((s) -> {
            try {
                Integer.parseInt(s, 16);
            } catch (NumberFormatException e) {
                return s.isEmpty();
            }
            return true;
        });
        dyeField.setText(Integer.toHexString(this.initColor));
        this.addDrawableChild(dyeField);

        if (this.shouldPaw) {
            TextFieldWidget pawField = new TextFieldWidget(this.textRenderer, x - 30, y + 25, 100, 20, Text.empty());
            pawField.setMaxLength(6);
            pawField.setChangedListener((s) -> updateTextField(true, s));
            pawField.setTextPredicate((s) -> {
                try {
                    Integer.parseInt(s, 16);
                } catch (NumberFormatException e) {
                    return s.isEmpty();
                }
                return true;
            });
            pawField.setText(Integer.toHexString(this.initPaw));
            this.addDrawableChild(pawField);
        }

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            PacketUpdateCollar.OwnerState os = this.ownerUUID == null
                    ? PacketUpdateCollar.OwnerState.DEL
                    : (this.ownerUUID.equals(this.ownUUID) ? PacketUpdateCollar.OwnerState.ADD : PacketUpdateCollar.OwnerState.NOP);
            // TODO 2026-03-02 (solonovamax): move color change into here
            ClientPlayNetworking.send(new PacketUpdateCollar(this.is, os));
            close();
        }).dimensions(x + 5, y + 50, 75, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), (btn) -> {
            NbtUtil.setColor(this.is, this.initColor);
            NbtUtil.setPawColor(this.is, this.initPaw);
            close();
        }).dimensions(x - 80, y + 50, 75, 20).build());

        ButtonWidget ownerButton = ButtonWidget.builder(Text.empty(), this::updateOwner).dimensions(x - 80, y + 72, 160, 20).build();
        if (this.ownerUUID == null) {
            ownerButton.setMessage(Text.translatable("item.playercollars.collar.become_owner"));
        } else if (this.ownerUUID.equals(this.ownUUID)) {
            ownerButton.setMessage(Text.translatable("item.playercollars.collar.remove_owner"));
        } else {
            ownerButton.setMessage(Text.translatable("item.playercollars.collar.owner", this.ownerName));
            ownerButton.active = false;
        }
        this.addDrawableChild(ownerButton);
    }

    private void updateOwner(ButtonWidget btn) {
        if (this.ownerUUID == null) {
            this.ownerUUID = this.ownUUID;
            btn.setMessage(Text.translatable("item.playercollars.collar.remove_owner"));
        } else {
            this.ownerUUID = null;
            btn.setMessage(Text.translatable("item.playercollars.collar.become_owner"));
        }
    }

    private void updateTextField(boolean paw, String s) {
        int col;
        try {
            col = Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            return;
        }

        if (paw) {
            NbtUtil.setPawColor(this.is, col);
        } else {
            NbtUtil.setColor(this.is, col);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        context.drawText(this.textRenderer, Text.translatable("item.playercollars.collar"),
                this.width / 2 - 75, this.height / 2 + (this.shouldPaw ? -25 : 1), -1, true);
        if (this.shouldPaw)
            context.drawText(this.textRenderer, Text.translatable("item.playercollars.collar.paw"), this.width / 2 - 75, this.height / 2 + 1, -1, true);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
