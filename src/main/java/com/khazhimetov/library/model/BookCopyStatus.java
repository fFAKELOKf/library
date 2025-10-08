package com.khazhimetov.library.model;

/**
 * Represents the current status of a book copy in the library.
 * Enum values correspond to typical states used in library systems.
 */
public enum BookCopyStatus {
    /** Book is available for borrowing */
    AVAILABLE("AVAILABLE"),
    /** Book has been issued to a reader */
    ISSUED("ISSUED"),
    /** Book has been reserved by a reader */
    RESERVED("RESERVED");

    private final String value;

    /**
     * Constructs an enum constant with the given string representation.
     *
     * @param value the string value associated with the status
     */
    BookCopyStatus(String value) {
        this.value = value;
    }

    /**
     * Gets the string representation of the enum value.
     *
     * @return the string representation (e.g., "AVAILABLE")
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the BookCopyStatus enum corresponding to a string.
     *
     * @param text the input string (case-insensitive)
     * @return the matching BookCopyStatus constant
     * @throws IllegalArgumentException if the input does not match any known status
     */
    public static BookCopyStatus fromString(String text) {
        for (BookCopyStatus s : BookCopyStatus.values()) {
            if (s.value.equalsIgnoreCase(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
