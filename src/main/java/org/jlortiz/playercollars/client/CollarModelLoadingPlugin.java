package org.jlortiz.playercollars.client;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class CollarModelLoadingPlugin implements ModelLoadingPlugin {
    private final Identifier ID = new Identifier(PlayerCollarsMod.MOD_ID, "collar");

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        pluginContext.modifyModelAfterBake().register((model, context) -> {
            final Identifier id = context.id();
            if (id != null && id.equals(ID)) {
                // FIXME: either this isn't being run at all or the renderer is completely broken
                TrinketRendererRegistry.registerRenderer(PlayerCollarsMod.COLLAR_ITEM, new CollarRenderer(model));
            }
            return model;
        });
    }
}
