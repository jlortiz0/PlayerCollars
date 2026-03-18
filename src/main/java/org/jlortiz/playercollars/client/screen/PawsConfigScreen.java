package org.jlortiz.playercollars.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jlortiz.playercollars.PlayerCollarsMod;
import org.jlortiz.playercollars.network.PawsConfigScreenHandler;

public abstract class PawsConfigScreen<T>
        extends AbstractContainerScreen<PawsConfigScreenHandler<T>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(PlayerCollarsMod.MOD_ID, "textures/gui/paw_controller.png");
    private static final ResourceLocation WIDGETS_TEXTURE =
            new ResourceLocation(PlayerCollarsMod.MOD_ID, "textures/gui/paw_controller_widgets.png");

    private TagLikeListWidget<T> listWidget;
    private ItemStack stack;

    protected PawsConfigScreen(PawsConfigScreenHandler<T> handler, Inventory inv, Component title) {
        super(handler, inv, title);
        this.stack = ItemStack.EMPTY;
    }

    @Override
    protected void init() {
        this.imageWidth = 174;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
        super.init();
        this.listWidget = addRenderableWidget(new TagLikeListWidget<>(
                160, 106,
                this.topPos + 18, this.topPos + 124,
                this.font.lineHeight,
                this.leftPos + 7,
                this.menu.getRegistryKey(),
                this::handleButtonClick));
        this.listWidget.setList(this.menu.listToDisplay);
    }

    private void handleButtonClick(int id) {
        this.menu.clickMenuButton(this.minecraft.player, id);
        assert this.minecraft.gameMode != null;
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        this.menu.getSlot(0).set(ItemStack.EMPTY);
        if (this.stack.isEmpty()) this.listWidget.setList(this.menu.listToDisplay);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!ItemStack.matches(this.menu.getSlot(0).getItem(), this.stack)) {
            this.stack = this.menu.getSlot(0).getItem();
            this.listWidget.setList(this.menu.listToDisplay);
        }
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        int x = (this.width - this.imageWidth - 50) / 2;
        int y = (this.height - this.imageHeight) / 2;
        context.blit(TEXTURE, x, y, 0, 0, this.imageWidth + 50, this.imageHeight);
        context.blit(WIDGETS_TEXTURE, x + 7, y + 108, this.stack.isEmpty() ? 16 : 0, 0, 16, 16, 32, 16);
    }

    public static class BlockScreen extends PawsConfigScreen<Block> {
        public BlockScreen(PawsConfigScreenHandler<Block> handler, Inventory inv, Component title) {
            super(handler, inv, title);
        }
    }

    public static class ItemScreen extends PawsConfigScreen<Item> {
        public ItemScreen(PawsConfigScreenHandler<Item> handler, Inventory inv, Component title) {
            super(handler, inv, title);
        }
    }
}
