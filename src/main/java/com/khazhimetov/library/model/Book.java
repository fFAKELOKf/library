package com.khazhimetov.library.model;

import java.time.LocalDateTime;

/**
 * Represents a book in the library system.
 * Contains metadata and inventory information such as title, author, and availability.
 */
public class Book {
    /** Unique identifier of the book */
    private Long id;
    /** Title of the book */
    private String title;
    /** Author of the book */
    private String author;
    /** ISBN for uniquely identifying the book */
    private String isbn;
    /** Genre or category of the book */
    private String genre;
    /** Short description or summary of the book */
    private String description;
    /** Year the book was published */
    private Integer publicationYear;
    /** Total number of copies the library owns */
    private Integer totalCopies;
    /** Number of copies currently available for borrowing */
    private Integer availableCopies;
    /** Timestamp when the book record was created */
    private LocalDateTime createdAt;
    /** Timestamp when the book record was last updated */
    private LocalDateTime updatedAt;

    /**
     * Default constructor for Book.
     */
    public Book() {}

    /**
     * Full constructor for Book.
     *
     * @param id              the unique identifier of the book
     * @param title           the title of the book
     * @param author          the author of the book
     * @param isbn            the ISBN of the book
     * @param genre           the genre of the book
     * @param description     the description of the book
     * @param publicationYear the year the book was published
     * @param totalCopies     the total number of copies in the library
     * @param availableCopies the number of currently available copies
     * @param createdAt       the timestamp of creation
     * @param updatedAt       the timestamp of last update
     */
    public Book(Long id, String title, String author, String isbn, String genre,
                String description, Integer publicationYear, Integer totalCopies,
                Integer availableCopies, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.description = description;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }

    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
