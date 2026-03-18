package org.jlortiz.playercollars.client.screen;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import org.jlortiz.playercollars.util.TagKeyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TagLikeListWidget<T>
        extends ObjectSelectionList<TagLikeListWidget.TagEntry<T>> {

    private final ResourceKey<Registry<T>> registryKey;
    private final Consumer<Integer> handleClick;
    private final int leftPos;

    public TagLikeListWidget(int width, int height, int top, int bottom, int itemHeight,
                              int leftPos, ResourceKey<Registry<T>> registryKey,
                              Consumer<Integer> handleClick) {
        super(Minecraft.getInstance(), width, height, top, bottom, itemHeight);
        this.registryKey = registryKey;
        this.handleClick = handleClick;
        this.leftPos = leftPos;
        setRenderBackground(false);
        setRenderTopAndBottom(false);
    }

    @Override
    public int getRowWidth() { return this.width - 5; }

    @Override
    protected int getScrollbarPosition() { return this.leftPos + getRowWidth(); }

    @Override
    public int getRowLeft() { return this.leftPos + 2; }

    public void setList(List<Either<TagKey<T>, ResourceKey<T>>> list) {
        List<TagEntry<T>> entries = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++)
            entries.add(new TagEntry<>(this, i, list.get(i)));
        replaceEntries(entries);
        setScrollAmount(0);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.setColor(0.25F, 0.25F, 0.25F, 1.0F);
        context.blit(net.minecraft.client.gui.screens.Screen.BACKGROUND_LOCATION,
                this.leftPos, this.y0, 0.0F, 0.0F, this.width, this.height, 32, 32);
        context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.fillGradient(this.leftPos, this.y0, this.leftPos + this.width, this.y0 + 4, 0xFF000000, 0, 0);
        context.fillGradient(this.leftPos, this.y1 - 4, this.leftPos + this.width, this.y1, 0, 0xFF000000, 0);
        super.render(context, mouseX, mouseY, delta);
    }

    private static final class TransparentButton<T> extends Button {
        private TransparentButton(TagLikeListWidget<T> parent) {
            super(0, 0, parent.getRowWidth(), parent.itemHeight, Component.empty(), x -> {}, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            if (this.isHovered())
                context.fill(getX(), getY(), getX() + getWidth(), getY() + this.height, 0x80999999);
        }
    }

    public static final class TagEntry<T>
            extends ObjectSelectionList.Entry<TagEntry<T>> {
        private final TagLikeListWidget<T> parent;
        private final Component label;
        private final TransparentButton<T> button;
        private final int index;

        private TagEntry(TagLikeListWidget<T> parent, int index, Either<TagKey<T>, ResourceKey<T>> key) {
            this.parent = parent;
            this.index = index;
            assert Minecraft.getInstance().level != null;
            this.label = key.map(
                    TagKeyUtil::getName,
                    x -> {
                        assert Minecraft.getInstance().level != null;
                        return Minecraft.getInstance().level
                                .registryAccess()
                                .registry(parent.registryKey)
                                .flatMap(r -> r.getHolder(x))
                                .map(h -> {
                                        // Works for both Block and Item via their registry name
                                        var rKey = h.unwrapKey();
                                        return rKey.map(k -> Component.literal(k.location().getPath()))
                                                   .orElse(Component.literal("error!"));
                                    })
                                .orElse(Component.literal("error!"));
                    });
            this.button = new TransparentButton<>(parent);
        }

        @Override
        public void render(GuiGraphics context, int idx, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.button.setPosition(x, y - 2);
            this.button.render(context, mouseX, mouseY, tickDelta);
            context.drawString(this.parent.minecraft.font, this.label, x, y - 1, 0xFFFFFF, false);
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

        @Override
        public Component getNarration() { return this.label; }
    }
}
