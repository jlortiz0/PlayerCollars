package org.jlortiz.playercollars.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jlortiz.playercollars.OwnerComponent;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PacketStampDeed;

public class DeedItemScreen extends Screen {
    private final OwnerComponent owner;
    private final Text name;

    public DeedItemScreen(ItemStack is, Entity plr) {
        super(is.getName());
        this.owner = is.get(PlayerCollarsMod.OWNER_COMPONENT_TYPE);
        this.name = plr.getName();
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("item.playercollars.deed_of_ownership.stamp"), this::stampDeed).dimensions(this.width / 2 - 80, this.height / 2 + 72, 160, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), (x) -> close()).dimensions(this.width / 2 - 80, this.height / 2 + 95, 160, 20).build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        applyBlur();
        renderDarkening(context);
        // TODO draw background
//        renderBackgroundTexture();
    }

    @Override
    public void render(DrawContext p_281549_, int mouseX, int mouseY, float delta) {
        renderBackground(p_281549_, mouseX, mouseY, delta);
        super.render(p_281549_, mouseX, mouseY, delta);
        p_281549_.drawCenteredTextWithShadow(textRenderer, Text.translatable("item.playercollars.deed_of_ownership"), this.width / 2, this.height / 2 - 88, -1);
        p_281549_.drawCenteredTextWithShadow(textRenderer, Text.translatable("item.playercollars.deed_of_ownership.line1", name, owner.name()), this.width / 2, this.height / 2 - 55, -1);
        p_281549_.drawCenteredTextWithShadow(textRenderer, Text.translatable("item.playercollars.deed_of_ownership.line2"), this.width / 2, this.height / 2 - 38, -1);
        p_281549_.drawCenteredTextWithShadow(textRenderer, Text.translatable("item.playercollars.deed_of_ownership.line3"), this.width / 2, this.height / 2 - 26, -1);
        p_281549_.drawCenteredTextWithShadow(textRenderer, Text.translatable("item.playercollars.deed_of_ownership.line4"), this.width / 2, this.height / 2 - 14, -1);
        p_281549_.drawCenteredTextWithShadow(textRenderer, Text.translatable("item.playercollars.deed_of_ownership.line5"), this.width / 2, this.height / 2 - 2, -1);
        p_281549_.drawCenteredTextWithShadow(textRenderer, Text.translatable("item.playercollars.deed_of_ownership.line6"), this.width / 2, this.height / 2 + 10, -1);
        p_281549_.drawCenteredTextWithShadow(textRenderer, Text.translatable("item.playercollars.deed_of_ownership.line7"), this.width / 2, this.height / 2 + 23, -1);
        p_281549_.drawCenteredTextWithShadow(textRenderer, Text.translatable("item.playercollars.deed_of_ownership.line8"), this.width / 2, this.height / 2 + 40, -1);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void stampDeed(ButtonWidget btn) {
        ClientPlayNetworking.send(PacketStampDeed.INSTANCE);
        close();
    }
}

