package org.jlortiz.playercollars;

public enum OwnershipLevel {
    NOT_OWNED,
    OWNED,
    OWNED_SIGNED;

    public boolean isOwned() {
        return this != NOT_OWNED;
    }

    public boolean isOwnedByContract() {
        return this == OWNED_SIGNED;
    }
}
