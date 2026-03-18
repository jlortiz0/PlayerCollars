package org.jlortiz.playercollars;

import java.util.Optional;
import java.util.UUID;

public record OwnerComponent(UUID uuid, String name, Optional<UUID> owned, Optional<String> ownedName) {
    public OwnerComponent(UUID uuid, String name) {
        this(uuid, name, Optional.empty(), Optional.empty());
    }
}
