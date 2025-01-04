package org.jlortiz.playercollars.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class CollarRenderer implements ICurioRenderer {

    private final BakedModel model;
    private final Ingredient chestplateIngr = Ingredient.of(Tags.Items.ARMORS_CHESTPLATES);
    public CollarRenderer(BakedModel m) {
        this.model = m;
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack itemStack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int i, float v, float v1, float v2, float v3, float v4, float v5) {
        poseStack.pushPose();
        try {
            ModelPart body = ((HumanoidModel<T>) renderLayerParent.getModel()).body;
            boolean hasChestplate = false;
            for (ItemStack is : slotContext.entity().getArmorSlots()) {
                if (chestplateIngr.test(is)) {
                    hasChestplate = true;
                    break;
                }
            }
            poseStack.translate(body.x * 0.0625f, body.y * 0.0625f, body.z * 0.0625f);
            poseStack.mulPose(new Quaternionf().rotateXYZ(body.xRot, body.yRot, body.zRot + (float) Math.PI));
            poseStack.scale((hasChestplate ? 0.7f : 0.85f) * body.xScale, 0.85f * body.yScale, (hasChestplate ? 1.1f : 0.85f) * body.zScale);
            poseStack.translate(0, hasChestplate ? 0.475 : 0.4125, -0.005);
            Minecraft.getInstance().getItemRenderer().render(itemStack, ItemDisplayContext.HEAD, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, model);
        } catch (ClassCastException ignored) {}
        poseStack.popPose();
    }
}
