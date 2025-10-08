package com.khazhimetov.library.model;

import java.time.LocalDateTime;

/**
 * Represents a physical copy of a book in the library.
 * Each book copy is associated with a specific Book via bookId,
 * and has its own inventory number and availability status.
 */
public class BookCopy {
    /** Unique identifier of the book copy */
    private Long id;
    /** Identifier of the associated book (foreign key) */
    private Long bookId;
    /** Unique inventory number of the copy (e.g., barcode or asset tag) */
    private String inventoryNumber;
    /** Status of the book copy (e.g., AVAILABLE, CHECKED_OUT, LOST) */
    private BookCopyStatus status;
    /** Timestamp when the book copy record was created */
    private LocalDateTime createdAt;

    /**
     * Default constructor.
     */
    public BookCopy() {}

    /**
     * Full constructor for creating a book copy instance.
     *
     * @param id              the unique ID of the book copy
     * @param bookId          the ID of the associated book
     * @param inventoryNumber the unique inventory number for this copy
     * @param status          the current status of the copy
     * @param createdAt       the timestamp when the copy was added to the system
     */
    public BookCopy(Long id, Long bookId, String inventoryNumber,
                    BookCopyStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.bookId = bookId;
        this.inventoryNumber = inventoryNumber;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }

    public BookCopyStatus getStatus() { return status; }
    public void setStatus(BookCopyStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
