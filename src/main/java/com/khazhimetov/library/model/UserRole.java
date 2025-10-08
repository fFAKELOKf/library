package com.khazhimetov.library.model;

/**
 * User roles enumeration
 */
public enum UserRole {
    READER("READER"),
    LIBRARIAN("LIBRARIAN"),
    ADMIN("ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromString(String text) {
        for (UserRole r : UserRole.values()) {
            if (r.value.equalsIgnoreCase(text)) {
                return r;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
