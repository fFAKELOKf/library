package com.khazhimetov.library.service;

import com.khazhimetov.library.model.Book;
import com.khazhimetov.library.model.Page;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for book-related operations.
 *
 * Provides methods to manage books, including retrieval, search,
 * pagination, creation, update, and deletion.
 */
public interface BookService {
    /**
     * Find book by ID
     * @param id book ID
     * @return book or empty if not found
     */
    Optional<Book> findById(Long id);

    /**
     * Find all books
     * @return list of all books
     */
    List<Book> findAll();

    /**
     * Retrieves all books with pagination.
     *
     * @param pageNumber the page number (0-based)
     * @param pageSize   the number of items per page
     * @return a {@link Page} containing the books
     */
    Page<Book> findAllPaginated(int pageNumber, int pageSize);

    /**
     * Search books
     * @param searchTerm search term
     * @return list of matching books
     */
    List<Book> search(String searchTerm);

    /**
     * Searches for books with pagination.
     *
     * @param searchTerm the term to search for
     * @param pageNumber the page number (0-based)
     * @param pageSize   the number of items per page
     * @return a {@link Page} containing the search results
     */
    Page<Book> searchPaginated(String searchTerm, int pageNumber, int pageSize);

    /**
     * Finds books by genre with pagination.
     *
     * @param genre      the genre to filter by
     * @param pageNumber the page number (0-based)
     * @param pageSize   the number of items per page
     * @return a {@link Page} containing books of the specified genre
     */
    Page<Book> findByGenrePaginated(String genre, int pageNumber, int pageSize);

    /**
     * Save book
     * @param book book to save
     * @return saved book
     */
    Book save(Book book);

    /**
     * Update book
     * @param book book to update
     * @return updated book
     */
    Book update(Book book);

    /**
     * Delete book
     * @param id book ID
     * @return true if deleted, false otherwise
     */
    boolean delete(Long id);
}
