package com.khazhimetov.library.model;

/**
 * Represents the type of order made by a library user.
 */
public enum OrderType {
    /** Borrowed for home use */
    HOME("HOME"),
    /** Reserved or issued for use within the reading room only */
    READING_ROOM("READING_ROOM");

    private final String value;

    OrderType(String value) {
        this.value = value;
    }

    /**
     * Returns the string representation of the enum value.
     *
     * @return a string such as "HOME" or "READING_ROOM"
     */
    public String getValue() {
        return value;
    }

    /**
     * Parses a string into an OrderType enum constant.
     *
     * @param text the input string (case-insensitive)
     * @return the matching OrderType
     * @throws IllegalArgumentException if no match is found
     */
    public static OrderType fromString(String text) {
        for (OrderType t : OrderType.values()) {
            if (t.value.equalsIgnoreCase(text)) {
                return t;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
