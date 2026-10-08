package de.imi.mopat.model.user;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of user roles supported within MoPat 2.0
 */
public enum UserRole {

    ROLE_USER("ROLE_USER"), ROLE_ENCOUNTERMANAGER("ROLE_ENCOUNTERMANAGER"), ROLE_EDITOR(
        "ROLE_EDITOR"), ROLE_MODERATOR("ROLE_MODERATOR"), ROLE_ADMIN("ROLE_ADMIN");
    private final String textValue;
    private static final Map<String, UserRole> stringToEnum = new HashMap<String, UserRole>();

    static // Initialize map from constant name to enum constant
    {
        for (UserRole cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    UserRole(final String textValue) {
        this.textValue = textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public static UserRole fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }

    public boolean isAtLeast(UserRole required) {
        if (required == null) return false;

        int thisLevel = getLevel(this);
        int requiredLevel = getLevel(required);

        return thisLevel >= requiredLevel;
    }

    private int getLevel(UserRole role) {
        switch (role) {
            case ROLE_USER:
                return 0;
            case ROLE_ENCOUNTERMANAGER:
                return 1;
            case ROLE_EDITOR:
                return 2;
            case ROLE_MODERATOR:
                return 3;
            case ROLE_ADMIN:
                return 4;
            default:
                return -1; // Unknown role
        }
    }
}