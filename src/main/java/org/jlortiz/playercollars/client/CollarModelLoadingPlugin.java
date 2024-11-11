package org.jlortiz.playercollars.client;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class CollarModelLoadingPlugin implements ModelLoadingPlugin {
    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        pluginContext.modifyModelAfterBake().register((model, context) -> {
            Identifier id = context.id();
            if (id != null && id.getNamespace().equals(PlayerCollarsMod.MOD_ID) && id.getPath().startsWith("collar")) {
                TrinketRendererRegistry.registerRenderer(PlayerCollarsMod.COLLAR_ITEM, new CollarRenderer(model));
            }
            return model;
        });
    }
}
