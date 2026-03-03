package org.jlortiz.playercollars.client.screen;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import org.jlortiz.playercollars.util.TagKeyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TagLikeListWidget<T extends ItemConvertible> extends EntryListWidget<TagLikeListWidget.TagEntry<T>> {
    private final RegistryKey<Registry<T>> registryKey;
    private final Consumer<Integer> handleClick;

    public TagLikeListWidget(int width, int height, int left, int top, int itemHeight, RegistryKey<Registry<T>> registryKey, Consumer<Integer> handleClick) {
        super(MinecraftClient.getInstance(), width, height, top, top + height, itemHeight);
        this.registryKey = registryKey;
        this.handleClick = handleClick;
        this.setLeftPos(left);
        setRenderBackground(false);
        setRenderHorizontalShadows(false);
        setRenderHeader(false, 0);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(0.25F, 0.25F, 0.25F, 1.0F);
        context.drawTexture(Screen.OPTIONS_BACKGROUND_TEXTURE, this.left, this.top, 0.0F, 0.0F, this.width, this.height, 32, 32);
        // context.drawTexture(Screen.OPTIONS_BACKGROUND_TEXTURE, this.left, this.bottom, 0.0F, this.bottom, this.width, this.height - this.bottom, 32, 32);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.fillGradient(RenderLayer.getGuiOverlay(), this.left, this.top, this.right, this.top + 4, -16777216, 0, 0);
        context.fillGradient(RenderLayer.getGuiOverlay(), this.left, this.bottom - 4, this.right, this.bottom, 0, -16777216, 0);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public int getRowWidth() {
        return this.width - 5;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.left + getRowWidth();
    }

    public void setList(List<Either<TagKey<T>, RegistryKey<T>>> stream) {
        List<TagEntry<T>> entries = new ArrayList<>(stream.size());
        for (int i = 0; i < stream.size(); i++)
            entries.add(i, new TagEntry<>(this, i, stream.get(i)));
        replaceEntries(entries);
        setScrollAmount(0);
    }

    @Override
    public int getRowLeft() {
        return this.left + 2;
    }

    private static final class TransparentButton<T extends ItemConvertible> extends ButtonWidget {
        private TransparentButton(TagLikeListWidget<T> parent) {
            super(0, 0, parent.getRowWidth(), parent.itemHeight, Text.empty(), (x) -> {
            }, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            if (this.isHovered())
                context.fill(getX(), getY(), getX() + getWidth(), getY() + this.height, 0x999999 + (142 << 24));
        }
    }

    public static final class TagEntry<T extends ItemConvertible> extends EntryListWidget.Entry<TagEntry<T>> {
        private final TagLikeListWidget<T> parent;
        private final Text label;
        private final TransparentButton<T> button;
        private final int index;

        private TagEntry(TagLikeListWidget<T> parent, int index, Either<TagKey<T>, RegistryKey<T>> key) {
            this.parent = parent;
            assert MinecraftClient.getInstance().world != null;
            this.label = key.map(
                    TagKeyUtil::getName,
                    (x) -> {
                        assert MinecraftClient.getInstance().world != null;
                        return MinecraftClient.getInstance().world
                                .getRegistryManager()
                                .get(parent.registryKey)
                                .getEntry(x)
                                .map((y) -> y.value().asItem().getName())
                                .orElse(Text.literal("error!"));
                    }
            );

            this.button = new TransparentButton<>(parent);
            this.index = index;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered,
                           float tickDelta) {
            this.button.setPosition(x, y - 2);
            this.button.render(context, mouseX, mouseY, tickDelta);
            context.drawText(this.parent.client.textRenderer, this.label, x, y - 1, 0xFFFFFF, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.button.mouseClicked(mouseX, mouseY, button)) {
                this.parent.handleClick.accept(this.index);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.button.mouseReleased(mouseX, mouseY, button);
        }
    }
}
