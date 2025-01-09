package org.jlortiz.playercollars.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.MapColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PacketUpdateCollar;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;

import java.util.UUID;

public class CollarDyeScreen extends Screen {
    private final ItemStack is;
    private static final CollarItem item = PlayerCollarsMod.COLLAR_ITEM;
    private final int initColor, initPaw;
    private final UUID ownUUID;
    private OwnerComponent owner;

    public CollarDyeScreen(ItemStack is, UUID plr) {
        super(is.getName());
        this.is = is;
        this.ownUUID = plr;
        initColor = item.getColor(is);
        initPaw = item.getPawColor(is);
        owner = item.getOwner(is);
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
            PacketUpdateCollar.OwnerState os = owner == null ? PacketUpdateCollar.OwnerState.DEL : (owner.uuid().equals(ownUUID) ? PacketUpdateCollar.OwnerState.ADD : PacketUpdateCollar.OwnerState.NOP);
            ClientPlayNetworking.send(new PacketUpdateCollar(is, os));
            this.client.setScreen(null);
        }).dimensions(x + 5, y + 50, 75, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), (btn) -> {
            is.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(initColor, true));
            is.set(DataComponentTypes.MAP_COLOR, new MapColorComponent(initPaw));
            this.client.setScreen(null);
        }).dimensions(x - 80, y + 50, 75, 20).build());

        ButtonWidget ownerButton = ButtonWidget.builder(Text.empty(), this::updateOwner).dimensions(x - 80, y + 72, 160, 20).build();
        if (owner == null) {
            ownerButton.setMessage(Text.translatable("item.playercollars.collar.become_owner"));
        } else if (owner.uuid().equals(ownUUID)) {
            ownerButton.setMessage(Text.translatable("item.playercollars.collar.remove_owner"));
        } else {
            ownerButton.setMessage(Text.translatable("item.playercollars.collar.owner", owner.name()));
            ownerButton.active = false;
        }
        this.addDrawableChild(ownerButton);
    }

    private void updateOwner(ButtonWidget btn) {
        if (owner == null) {
            owner = new OwnerComponent(ownUUID, MinecraftClient.getInstance().getGameProfile().getName());
            btn.setMessage(Text.translatable("item.playercollars.collar.remove_owner"));
        } else {
            owner = null;
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
            is.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(col, true));
        } else {
            is.set(DataComponentTypes.MAP_COLOR, new MapColorComponent(col));
        }
    }

    @Override
    public void render(DrawContext p_281549_, int mouseX, int mouseY, float delta) {
        super.render(p_281549_, mouseX, mouseY, delta);
        p_281549_.drawText(textRenderer, Text.translatable("item.playercollars.collar"), this.width / 2 - 75, this.height / 2 - 25, -1, true);
        p_281549_.drawText(textRenderer, Text.translatable("item.playercollars.collar.paw"), this.width / 2 - 75, this.height / 2 + 1, -1, true);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
