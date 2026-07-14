package com.paw.adoptions.domain;

public enum AdoptionApplicationStatus {
    PENDING,
    INFO_REQUESTED,
    ACCEPTED,
    REJECTED;

    public boolean isActive() {
        return this == PENDING || this == INFO_REQUESTED;
    }
}