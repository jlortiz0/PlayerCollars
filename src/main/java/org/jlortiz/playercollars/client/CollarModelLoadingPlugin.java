package org.jlortiz.playercollars.client;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.jlortiz.playercollars.PlayerCollarsMod;

public class CollarModelLoadingPlugin implements ModelLoadingPlugin {
    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        pluginContext.modifyModelAfterBake().register((model, context) -> {
            ModelIdentifier id = context.topLevelId();
            if (id == null) return model;
            Identifier id2 = id.id();
            if (id2 != null && id2.getNamespace().equals(PlayerCollarsMod.MOD_ID) && id2.getPath().startsWith("collar")) {
                TrinketRendererRegistry.registerRenderer(PlayerCollarsMod.COLLAR_ITEM, new CollarRenderer(model));
            }
            return model;
        });
    }
}
