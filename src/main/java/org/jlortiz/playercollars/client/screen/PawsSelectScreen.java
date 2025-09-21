package org.jlortiz.playercollars.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.jlortiz.playercollars.network.PacketOpenPawsConfig;

import java.util.UUID;

public class PawsSelectScreen extends Screen {
    private final UUID plr;

    public PawsSelectScreen(Entity plr) {
        super(Text.translatable("gui.playercollars.paw_configurator.title", plr.getName()));
        this.plr = plr.getUuid();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(client.textRenderer, title, this.width / 2, this.height / 2 - 20, -1);
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.playercollars.paw_configurator.block.open"), (btn) -> {
            ClientPlayNetworking.send(new PacketOpenPawsConfig(plr, false));
            close();
        }).dimensions(x - 80, y, 160, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.playercollars.paw_configurator.item.open"), (btn) -> {
            ClientPlayNetworking.send(new PacketOpenPawsConfig(plr, true));
            close();
        }).dimensions(x - 80, y + 22, 160, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
