package org.jlortiz.playercollars.client.screen;

import com.mojang.datafixers.util.Either;
import io.wispforest.accessories.client.gui.ButtonEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.tag.FabricTagKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TagLikeListWidget<T extends ItemConvertible> extends EntryListWidget<TagLikeListWidget<T>.TagEntry> {
    private final RegistryKey<Registry<T>> registryKey;
    private final Consumer<Integer> handleClick;

    public TagLikeListWidget(int width, int height, int x, int y, int itemHeight, RegistryKey<Registry<T>> registryKey, Consumer<Integer> handleClick) {
        super(MinecraftClient.getInstance(), width, height, y, itemHeight);
        this.registryKey = registryKey;
        this.handleClick = handleClick;
        setX(x);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public int getRowWidth() {
        return width - 5;
    }

    @Override
    protected int getScrollbarX() {
        return getX() + getRowWidth();
    }

    public void setList(List<Either<TagKey<T>, RegistryKey<T>>> stream) {
        List<TagEntry> entries = new ArrayList<>(stream.size());
        for (int i = 0; i < stream.size(); i++)
            entries.add(i, new TagEntry(i, stream.get(i)));
        replaceEntries(entries);
        setScrollY(0);
    }

    @Override
    public int getRowLeft() {
        return getX() + 2;
    }

    private class TransparentButton extends ButtonWidget {
        protected TransparentButton() {
            super(0, 0, TagLikeListWidget.this.getRowWidth(), TagLikeListWidget.this.itemHeight,
                    net.minecraft.text.Text.empty(), (x) -> {}, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            if (this.isHovered())
                context.fill(getX(), getY(), getX() + getWidth(), getY() + height, 0x999999 + (142 << 24));
        }

        @Override
        public Event<ButtonEvents.AdjustRendering> getRenderingEvent() {
            return null;
        }
    }

    public class TagEntry extends EntryListWidget.Entry<TagEntry> {
        private final Text label;
        private final TransparentButton button;
        private final int index;

        private TagEntry(int index, Either<TagKey<T>, RegistryKey<T>> key) {
            this.label = key.map(FabricTagKey::getName, (x) -> MinecraftClient.getInstance().world
                    .getRegistryManager().getOrThrow(registryKey).getOptionalValue(x)
                    .map((y) -> y.asItem().getName()).orElse(Text.literal("error!")));
            this.button = new TransparentButton();
            this.index = index;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            this.button.setPosition(mouseX, mouseY - 2);
            this.button.render(context, mouseX, mouseY, deltaTicks);
            context.drawText(client.textRenderer, label, mouseX, mouseY - 1, -1, false);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            if (this.button.mouseClicked(click, doubled)) {
                TagLikeListWidget.this.handleClick.accept(this.index);
                return true;
            }
            return super.mouseClicked(click, doubled);
        }

        @Override
        public boolean mouseReleased(Click click) {
            return this.button.mouseReleased(click);
        }
    }
}
