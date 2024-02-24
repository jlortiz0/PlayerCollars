package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jlortiz.playercollars.PacketUpdateCollar;
import org.jlortiz.playercollars.PlayerCollarItem;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class DyingStationScreen extends Screen {
    private final ItemStack is;
    private final PlayerCollarItem item;
    private int initColor, initPaw;

    public DyingStationScreen(ItemStack is) {
        super(is.getDisplayName());
        this.is = is;
        this.item = PlayerCollarsMod.COLLAR_ITEM.get();
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
        initColor = item.getColor(is);
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
        initPaw = item.getPawColor(is);
        pawField.setValue(Integer.toHexString(initPaw));

        this.addRenderableWidget(dyeField);
        this.addRenderableWidget(pawField);
        this.addRenderableWidget(new Button(x + 5, y + 50, 75, 20, Component.literal("Done"), (btn) -> {
            PlayerCollarsMod.NETWORK.sendToServer(new PacketUpdateCollar(is));
            this.minecraft.setScreen(null);
        }));
        this.addRenderableWidget(new Button(x - 80, y + 50, 75, 20, Component.literal("Cancel"), (btn) -> {
            item.setColor(is, initColor);
            item.setPawColor(is, initPaw);
            this.minecraft.setScreen(null);
        }));
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
        font.draw(pPoseStack, Component.translatable("item.playercollars.collar_paw2"), this.width / 2 - 75, this.height / 2 + 1, -1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
