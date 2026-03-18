package org.jlortiz.playercollars.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.network.ModPackets;
import org.jlortiz.playercollars.network.PacketStampDeed;
import org.jlortiz.playercollars.util.NbtUtil;

public class DeedItemScreen extends Screen {
    private final OwnerComponent owner;
    private final Component name;

    public DeedItemScreen(ItemStack is, Entity plr) {
        super(is.getHoverName());
        this.owner = NbtUtil.getDeedOwner(is);
        this.name = plr.getName();
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(
                Component.translatable("item.playercollars.deed_of_ownership.stamp"), this::stampDeed)
                .bounds(this.width / 2 - 80, this.height / 2 + 72, 160, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"), x -> onClose())
                .bounds(this.width / 2 - 80, this.height / 2 + 95, 160, 20).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        int cx = this.width / 2;
        context.drawCenteredString(this.font, Component.translatable("item.playercollars.deed_of_ownership"), cx, this.height / 2 - 88, -1);
        context.drawCenteredString(this.font, Component.translatable("item.playercollars.deed_of_ownership.line1", this.name, this.owner.name()), cx, this.height / 2 - 55, -1);
        context.drawCenteredString(this.font, Component.translatable("item.playercollars.deed_of_ownership.line2"), cx, this.height / 2 - 38, -1);
        context.drawCenteredString(this.font, Component.translatable("item.playercollars.deed_of_ownership.line3"), cx, this.height / 2 - 26, -1);
        context.drawCenteredString(this.font, Component.translatable("item.playercollars.deed_of_ownership.line4"), cx, this.height / 2 - 14, -1);
        context.drawCenteredString(this.font, Component.translatable("item.playercollars.deed_of_ownership.line5"), cx, this.height / 2 - 2, -1);
        context.drawCenteredString(this.font, Component.translatable("item.playercollars.deed_of_ownership.line6"), cx, this.height / 2 + 10, -1);
        context.drawCenteredString(this.font, Component.translatable("item.playercollars.deed_of_ownership.line7"), cx, this.height / 2 + 23, -1);
        context.drawCenteredString(this.font, Component.translatable("item.playercollars.deed_of_ownership.line8"), cx, this.height / 2 + 40, -1);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private void stampDeed(Button btn) {
        ModPackets.CHANNEL.sendToServer(PacketStampDeed.INSTANCE);
        onClose();
    }
}
