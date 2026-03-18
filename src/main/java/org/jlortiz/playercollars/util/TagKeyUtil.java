package org.jlortiz.playercollars.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public final class TagKeyUtil {
    private TagKeyUtil() {}

    public static <T> String getTranslationKey(TagKey<T> tagKey) {
        StringBuilder sb = new StringBuilder("tag.");
        ResourceLocation registryId = tagKey.registry().location();
        ResourceLocation tagId = tagKey.location();

        if (!registryId.getNamespace().equals("minecraft")) {
            sb.append(registryId.getNamespace()).append(".");
        }
        sb.append(registryId.getPath().replace("/", "."))
          .append(".").append(tagId.getNamespace())
          .append(".").append(tagId.getPath().replace("/", ".").replace(":", "."));
        return sb.toString();
    }

    public static <T> Component getName(TagKey<T> tagKey) {
        return Component.translatableWithFallback(getTranslationKey(tagKey), "#" + tagKey.location());
    }
}
