package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jlortiz.playercollars.PacketUpdateCollar;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.item.CollarItem;

import java.util.UUID;

public class DyingStationScreen extends Screen {
    private final ItemStack is;
    private final CollarItem item;
    private final int initColor, initPaw;
    private final UUID ownUUID;
    private UUID ownerUUID;
    private final String ownerName;

    public DyingStationScreen(ItemStack is, UUID plr) {
        super(is.getDisplayName());
        this.is = is;
        this.item = PlayerCollarsMod.COLLAR_ITEM.get();
        this.ownUUID = plr;
        initColor = item.getColor(is);
        initPaw = item.getPawColor(is);
        Pair<UUID, String> owner = item.getOwner(is);
        ownerUUID = owner == null ? null : owner.getFirst();
        ownerName = owner == null ? null : owner.getSecond();
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2 - 30;

        EditBox dyeField = new EditBox(this.font, x- 30, y, 100, 20, Component.empty());
        dyeField.setMaxLength(6);
        dyeField.setResponder((s) -> updateTextField(0, s));
        dyeField.setFilter((s) -> {
            try {
                Integer.parseInt(s, 16);
            } catch (NumberFormatException e) {
                return s.isEmpty();
            }
            return true;
        });
        dyeField.setValue(Integer.toHexString(initColor));

        EditBox pawField = new EditBox(this.font, x - 30, y + 25, 100, 20, Component.empty());
        pawField.setMaxLength(6);
        pawField.setResponder((s) -> updateTextField(1, s));
        pawField.setFilter((s) -> {
            try {
                Integer.parseInt(s, 16);
            } catch (NumberFormatException e) {
                return s.isEmpty();
            }
            return true;
        });
        pawField.setValue(Integer.toHexString(initPaw));

        this.addRenderableWidget(dyeField);
        this.addRenderableWidget(pawField);
        this.addRenderableWidget(new Button(x + 5, y + 50, 75, 20, Component.literal("Done"), (btn) -> {
            PacketUpdateCollar.OwnerState os = ownerUUID == null ? PacketUpdateCollar.OwnerState.DEL : (ownerUUID.equals(ownUUID) ? PacketUpdateCollar.OwnerState.ADD : PacketUpdateCollar.OwnerState.NOP);
            PlayerCollarsMod.NETWORK.sendToServer(new PacketUpdateCollar(is, os));
            this.minecraft.setScreen(null);
        }));
        this.addRenderableWidget(new Button(x - 80, y + 50, 75, 20, Component.literal("Cancel"), (btn) -> {
            item.setColor(is, initColor);
            item.setPawColor(is, initPaw);
            this.minecraft.setScreen(null);
        }));

        Button ownerButton = new Button(x - 80, y + 72, 160, 20, Component.empty(), this::updateOwner);
        if (ownerUUID == null) {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.become_owner"));
        } else if (ownerUUID.equals(ownUUID)) {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.remove_owner"));
        } else {
            ownerButton.setMessage(Component.translatable("item.playercollars.collar.owner", ownerName));
            ownerButton.active = false;
        }
        this.addRenderableWidget(ownerButton);
    }

    private void updateOwner(Button btn) {
        if (ownerUUID == null) {
            ownerUUID = ownUUID;
            btn.setMessage(Component.translatable("item.playercollars.collar.remove_owner"));
        } else {
            ownerUUID = null;
            btn.setMessage(Component.translatable("item.playercollars.collar.become_owner"));
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
    public void render(@NotNull PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, mouseX, mouseY, delta);
        font.draw(pPoseStack, Component.translatable("item.playercollars.collar"), this.width / 2 - 75, this.height / 2 - 25, -1);
        font.draw(pPoseStack, Component.translatable("item.playercollars.collar.paw"), this.width / 2 - 75, this.height / 2 + 1, -1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
