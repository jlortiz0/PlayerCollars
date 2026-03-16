package org.jlortiz.playercollars.util;

import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TagKeyUtil {
    private TagKeyUtil() {
    }

    // backport of FabricTagKey
    public static <T> String getTranslationKey(TagKey<T> tagKey) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("tag.");

        Identifier registryIdentifier = tagKey.registry().getValue();
        Identifier tagIdentifier = tagKey.id();

        if (!registryIdentifier.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            stringBuilder.append(registryIdentifier.getNamespace())
                    .append(".");
        }

        stringBuilder.append(registryIdentifier.getPath().replace("/", "."))
                .append(".")
                .append(tagIdentifier.getNamespace())
                .append(".")
                .append(tagIdentifier.getPath().replace("/", ".").replace(":", "."));

        return stringBuilder.toString();
    }

    public static <T> Text getName(TagKey<T> tagKey) {
        return Text.translatableWithFallback(getTranslationKey(tagKey), "#" + tagKey.id().toString());
    }
}
