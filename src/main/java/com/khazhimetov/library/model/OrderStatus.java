package com.khazhimetov.library.model;

/**
 * Represents the status of a book order in the library system.
 */
public enum OrderStatus {
    /** Order has been submitted but not yet processed */
    PENDING("PENDING"),
    /** Book copy has been issued to the user */
    ISSUED("ISSUED"),
    /** Book copy has been returned by the user */
    RETURNED("RETURNED"),
    /** Order was cancelled by the user or system */
    CANCELLED("CANCELLED");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Returns the OrderStatus from string input (case-insensitive).
     * @param text the input string (case-insensitive)
     * @return matching enum constant
     * @throws IllegalArgumentException if no match found
     */
    public static OrderStatus fromString(String text) {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.value.equalsIgnoreCase(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
