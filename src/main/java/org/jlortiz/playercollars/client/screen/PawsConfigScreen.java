package org.jlortiz.playercollars.client.screen;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PawsConfigScreenHandler;

public class PawsConfigScreen<T extends ItemConvertible> extends HandledScreen<PawsConfigScreenHandler<T>> {
    private static final Identifier TEXTURE = Identifier.of(PlayerCollarsMod.MOD_ID, "textures/gui/paw_controller.png");
    private static final Identifier WIDGETS_TEXTURE = Identifier.of(PlayerCollarsMod.MOD_ID, "textures/gui/paw_controller_widgets.png");
    private TagLikeListWidget<T> listWidget;
    private ItemStack stack;

    public PawsConfigScreen(PawsConfigScreenHandler<T> screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
        stack = ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        backgroundWidth = 174;
        backgroundHeight = 222;
        playerInventoryTitleY = backgroundHeight - 94;
        super.init();
        listWidget = addDrawableChild(new TagLikeListWidget<>(160, 106, x + 7, y + 18,
                client.textRenderer.fontHeight, handler.getRegistryKey(), this::handleButtonClick));
        listWidget.setList(handler.listToDisplay);
    }

    private void handleButtonClick(int id) {
        // Call the local function first to prevent a race where the list could be cleared before we update the payload.
        handler.onButtonClick(client.player, id);
        client.interactionManager.clickButton(handler.syncId, id);
        handler.getSlot(0).setStack(ItemStack.EMPTY);
        if (stack.isEmpty())
            listWidget.setList(handler.listToDisplay);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!ItemStack.areEqual(handler.getSlot(0).getStack(), stack)) {
            stack = handler.getSlot(0).getStack();
            listWidget.setList(handler.listToDisplay);
        }
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth - 50) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, backgroundWidth + 50, backgroundHeight, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, WIDGETS_TEXTURE, x + 7, y + 108, stack.isEmpty() ? 16 : 0, 0, 16, 16, 32, 16);
    }
}
