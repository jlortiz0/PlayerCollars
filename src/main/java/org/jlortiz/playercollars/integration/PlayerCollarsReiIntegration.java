package org.jlortiz.playercollars.integration;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;
import org.jlortiz.playercollars.client.screen.PositionedScreen;
import org.jlortiz.playercollars.network.GhostSlotContainer;
import org.jlortiz.playercollars.network.PacketSetGhostItem;

import java.util.stream.Stream;

public class PlayerCollarsReiIntegration implements REIClientPlugin {
    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new DraggableStackVisitor<>() {
            @Override
            public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<Screen> context, DraggableStack stack) {
                if (context.getScreen() instanceof HandledScreen<?> handledScreen
                        && handledScreen instanceof PositionedScreen pos
                        && handledScreen.getScreenHandler() instanceof GhostSlotContainer gsc) {
                    return gsc.getGhostSlots().map(s -> BoundsProvider.ofRectangle(getSlotBounds(pos, s)));
                }
                return Stream.empty();
            }

            @Override
            public DraggedAcceptorResult acceptDraggedStack(DraggingContext<Screen> context, DraggableStack stack) {
                if (context.getScreen() instanceof HandledScreen<?> handledScreen
                        && handledScreen instanceof PositionedScreen pos
                        && handledScreen.getScreenHandler() instanceof GhostSlotContainer gsc
                        && stack.getStack().getValue() instanceof ItemStack item) {
                    var currentPosition = context.getCurrentPosition();
                    if (currentPosition == null) return DraggedAcceptorResult.PASS;
                    return gsc.getGhostSlots()
                            .filter(s -> getSlotBounds(pos, s).contains(currentPosition))
                            .findAny()
                            .map(s -> {
                                ClientPlayNetworking.send(new PacketSetGhostItem(handledScreen.getScreenHandler().syncId, s.id, item));
                                return DraggedAcceptorResult.CONSUMED;
                            })
                            .orElse(DraggedAcceptorResult.PASS);
                }
                return DraggedAcceptorResult.PASS;
            }

            @Override
            public <R extends Screen> boolean isHandingScreen(R screen) {
                return screen instanceof HandledScreen<?> hs
                        && screen instanceof PositionedScreen
                        && hs.getScreenHandler() instanceof GhostSlotContainer;
            }
        });
    }

    private static @NotNull Rectangle getSlotBounds(PositionedScreen pos, Slot s) {
        return new Rectangle(pos.getX() + s.x - 1, pos.getY() + s.y - 1, 18, 18);
    }
}
