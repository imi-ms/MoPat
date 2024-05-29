package de.imi.mopat.helper.model;

import java.util.UUID;

/**
 *
 */
public class UUIDGenerator {

    public static String createUUID() {
        UUID uuid = java.util.UUID.randomUUID();
        return uuid.toString();
    }
}