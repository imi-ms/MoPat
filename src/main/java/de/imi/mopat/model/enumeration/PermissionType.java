package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of permission types supported within Spring
 */
public enum PermissionType {

    FORBIDDEN(0), READ(1), WRITE(2), CREATE(3), DELETE(4), ADMIN(5);
    private final int value;
    private static final Map<Integer, PermissionType> intToEnum = new HashMap<Integer, PermissionType>();

    static // Initialize map from constant value to enum constant
    {
        for (PermissionType cValue : values()) {
            intToEnum.put(cValue.getValue(), cValue);
        }
    }

    PermissionType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}