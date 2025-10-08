package com.khazhimetov.library.dao;

import com.khazhimetov.library.model.Book;
import com.khazhimetov.library.model.Page;

import java.util.List;

/**
 * Book DAO interface
 */
public interface BookDao extends BasicDao<Book, Long> {

    /**
     * Find all books with pagination
     * @param pageNumber page number (0-based)
     * @param pageSize number of items per page
     * @return page of books
     */
    Page<Book> findAllPaginated(int pageNumber, int pageSize);

    /**
     * Find books by genre
     * @param genre book genre
     * @return list of matching books
     */
    List<Book> findByGenre(String genre);

    /**
     * Find books by genre with pagination
     * @param genre book genre
     * @param pageNumber page number (0-based)
     * @param pageSize number of items per page
     * @return page of books in specified genre
     */
    Page<Book> findByGenrePaginated(String genre, int pageNumber, int pageSize);

    /**
     * Search books by multiple criteria
     * @param searchTerm search term
     * @return list of matching books
     */
    List<Book> search(String searchTerm);

    /**
     * Search books with pagination
     * @param searchTerm search term
     * @param pageNumber page number (0-based)
     * @param pageSize number of items per page
     * @return page of matching books
     */
    Page<Book> searchPaginated(String searchTerm, int pageNumber, int pageSize);

    /**
     * Count all books
     * @return total count
     */
    long countAll();

    /**
     * Count books matching search term
     * @param searchTerm search term
     * @return count of matching books
     */
    long countBySearchTerm(String searchTerm);

    /**
     * Count books by genre
     * @param genre book genre
     * @return count of books in genre
     */
    long countByGenre(String genre);

    /**
     * Update available copies count
     * @param bookId book ID
     * @param delta change in available copies (positive or negative)
     */
    void updateAvailableCopies(Long bookId, int delta);
}
