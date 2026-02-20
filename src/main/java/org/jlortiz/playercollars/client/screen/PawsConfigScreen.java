package org.jlortiz.playercollars.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PawsConfigScreenHandler;

public class PawsConfigScreen<T extends ItemConvertible> extends HandledScreen<PawsConfigScreenHandler<T>> {
    private static final Identifier TEXTURE = Identifier.of(PlayerCollarsMod.MOD_ID, "textures/gui/paw_controller.png");
    private static final Identifier WIDGETS_TEXTURE = Identifier.of(PlayerCollarsMod.MOD_ID, "textures/gui/paw_controller_widgets.png");
    private static final int BUTTON_HEIGHT = 16;
    private TagLikeListWidget<T> listWidget;
    private boolean addingTags = false;

    public PawsConfigScreen(PawsConfigScreenHandler<T> screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Override
    protected void init() {
        backgroundWidth = 174;
        backgroundHeight = 222;
        playerInventoryTitleY = backgroundHeight - 94;
        super.init();
        listWidget = addDrawableChild(new TagLikeListWidget<>(160, 106 - BUTTON_HEIGHT, x + 7, y + 18,
                client.textRenderer.fontHeight, handler.getRegistryKey(), id -> handleButtonClick(id + PawsConfigScreenHandler.LIST_ID_OFFSET)));
        listWidget.setList(handler.listToDisplay);
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.playercollars.paw_configurator.filter.allow_all"), (btn) -> {
            handleButtonClick(PawsConfigScreenHandler.BTN_ALLOW_ALL_ID);
        }).dimensions(x + 7, y + 18 + 106 - BUTTON_HEIGHT, 80, BUTTON_HEIGHT).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.playercollars.paw_configurator.filter.deny_all"), (btn) -> {
            handleButtonClick(PawsConfigScreenHandler.BTN_DENY_ALL_ID);
        }).dimensions(x + 87, y + 18 + 106 - BUTTON_HEIGHT, 80, BUTTON_HEIGHT).build());
    }

    private void handleButtonClick(int id) {
        client.interactionManager.clickButton(handler.syncId, id);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (handler.checkAndClearDirty()) {
            addingTags = !handler.isDisplayingBackingList();
            listWidget.setList(handler.listToDisplay);
        }
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth - 50) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x, y, 0, 0, backgroundWidth + 50, backgroundHeight, 256, 256);
        context.drawTexture(RenderLayer::getGuiTextured, WIDGETS_TEXTURE, x + 7, y + 108, addingTags ? 0 : 16, 0, 16, 16, 32, 16);
    }
}
