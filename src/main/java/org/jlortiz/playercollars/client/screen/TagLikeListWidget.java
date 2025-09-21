package org.jlortiz.playercollars.client.screen;

import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.tag.FabricTagKey;
import net.minecraft.client.MinecraftClient;
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
                    Text.empty(), (x) -> {}, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            if (this.isHovered())
                context.fill(getX(), getY(), getX() + getWidth(), getY() + height, 0x999999 + (142 << 24));
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
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.button.setPosition(x, y - 2);
            this.button.render(context, mouseX, mouseY, tickDelta);
            context.drawText(client.textRenderer, label, x, y - 1, 0xFFFFFF, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.button.mouseClicked(mouseX, mouseY, button)) {
                TagLikeListWidget.this.handleClick.accept(this.index);
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
