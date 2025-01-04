package org.jlortiz.playercollars.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jlortiz.playercollars.PacketUpdateCollar;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;

import java.util.UUID;

public class CollarDyeScreen extends Screen {
    private final ItemStack is;
    private static final CollarItem item = PlayerCollarsMod.COLLAR_ITEM;
    private final int initColor, initPaw;
    private final UUID ownUUID;
    private UUID ownerUUID;
    private final String ownerName;

    public CollarDyeScreen(ItemStack is, UUID plr) {
        super(is.getName());
        this.is = is;
        this.ownUUID = plr;
        initColor = item.getColor(is);
        initPaw = item.getPawColor(is);
        Pair<UUID, String> owner = item.getOwner(is);
        ownerUUID = owner == null ? null : owner.getLeft();
        ownerName = owner == null ? null : owner.getRight();
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2 - 30;

        TextFieldWidget dyeField = new TextFieldWidget(this.textRenderer, x- 30, y, 100, 20, Text.empty());
        dyeField.setMaxLength(6);
        dyeField.setChangedListener((s) -> updateTextField(0, s));
        dyeField.setTextPredicate((s) -> {
            try {
                Integer.parseInt(s, 16);
            } catch (NumberFormatException e) {
                return s.isEmpty();
            }
            return true;
        });
        dyeField.setText(Integer.toHexString(initColor));

        TextFieldWidget pawField = new TextFieldWidget(this.textRenderer, x - 30, y + 25, 100, 20, Text.empty());
        pawField.setMaxLength(6);
        pawField.setChangedListener((s) -> updateTextField(1, s));
        pawField.setTextPredicate((s) -> {
            try {
                Integer.parseInt(s, 16);
            } catch (NumberFormatException e) {
                return s.isEmpty();
            }
            return true;
        });
        pawField.setText(Integer.toHexString(initPaw));

        this.addDrawableChild(dyeField);
        this.addDrawableChild(pawField);
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), (btn) -> {
            PacketUpdateCollar.OwnerState os = ownerUUID == null ? PacketUpdateCollar.OwnerState.DEL : (ownerUUID.equals(ownUUID) ? PacketUpdateCollar.OwnerState.ADD : PacketUpdateCollar.OwnerState.NOP);
            PacketByteBuf buffer = PacketByteBufs.create();
            new PacketUpdateCollar(is, os).encode(buffer);
            ClientPlayNetworking.send(new Identifier(PlayerCollarsMod.MOD_ID, "look_at"), buffer);
            this.client.setScreen(null);
        }).dimensions(x + 5, y + 50, 75, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), (btn) -> {
            item.setColor(is, initColor);
            item.setPawColor(is, initPaw);
            this.client.setScreen(null);
        }).dimensions(x - 80, y + 50, 75, 20).build());

        ButtonWidget ownerButton = ButtonWidget.builder(Text.empty(), this::updateOwner).dimensions(x - 80, y + 72, 160, 20).build();
        if (ownerUUID == null) {
            ownerButton.setMessage(Text.translatable("item.playercollars.collar.become_owner"));
        } else if (ownerUUID.equals(ownUUID)) {
            ownerButton.setMessage(Text.translatable("item.playercollars.collar.remove_owner"));
        } else {
            ownerButton.setMessage(Text.translatable("item.playercollars.collar.owner", ownerName));
            ownerButton.active = false;
        }
        this.addDrawableChild(ownerButton);
    }

    private void updateOwner(ButtonWidget btn) {
        if (ownerUUID == null) {
            ownerUUID = ownUUID;
            btn.setMessage(Text.translatable("item.playercollars.collar.remove_owner"));
        } else {
            ownerUUID = null;
            btn.setMessage(Text.translatable("item.playercollars.collar.become_owner"));
        }
    }

    private void updateTextField(int i, String s) {
        int col;
        try {
            col = Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            return;
        }
        if (i == 0) {
            item.setColor(is, col);
        } else {
            item.setPawColor(is, col);
        }
    }

    @Override
    public void render(DrawContext p_281549_, int mouseX, int mouseY, float delta) {
        renderBackground(p_281549_);
        super.render(p_281549_, mouseX, mouseY, delta);
        p_281549_.drawText(textRenderer, Text.translatable("item.playercollars.collar"), this.width / 2 - 75, this.height / 2 - 25, -1, true);
        p_281549_.drawText(textRenderer, Text.translatable("item.playercollars.collar.paw"), this.width / 2 - 75, this.height / 2 + 1, -1, true);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
