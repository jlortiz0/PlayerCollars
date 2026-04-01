package org.jlortiz.playercollars;

import java.util.Optional;
import java.util.UUID;

public record OwnerComponent(UUID uuid, String name, Optional<UUID> owned, Optional<String> ownedName) {
    public OwnerComponent(UUID uuid, String name) {
        this(uuid, name, Optional.empty(), Optional.empty());
    }

    public boolean isOwnedByContract() {
        return owned.isPresent();
    }

    public boolean isValidForPet(UUID pet) {
        return owned.map(pet::equals).orElse(true);
    }

    public boolean isOwnedBy(UUID owner) {
        return uuid.equals(owner);
    }
}
