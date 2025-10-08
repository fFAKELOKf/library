package com.khazhimetov.library.service.impl;

import com.khazhimetov.library.dao.BookDao;
import com.khazhimetov.library.exception.ServiceException;
import com.khazhimetov.library.model.Book;
import com.khazhimetov.library.model.Page;
import com.khazhimetov.library.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Book service implementation that handles business logic
 * for creating, updating, retrieving, searching, and deleting books.
 */
@Service
public class BookServiceImpl implements BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);
    private static final int DEFAULT_PAGE_SIZE = 12;

    private BookDao bookDao;

    @Autowired
    public BookServiceImpl(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Book> findById(Long id) {
        logger.debug("Finding book by ID: {}", id);
        return bookDao.findById(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<Book> findAll() {
        logger.debug("Retrieving all books");
        return bookDao.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public Page<Book> findAllPaginated(int pageNumber, int pageSize) {
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageNumber < 0) {
            pageNumber = 0;
        }
        logger.debug("Retrieving paginated books: page {}, size {}", pageNumber, pageSize);
        return bookDao.findAllPaginated(pageNumber, pageSize);
    }

    /** {@inheritDoc} */
    @Override
    public List<Book> search(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.debug("Empty search term provided, returning all books");
            return findAll();
        }
        logger.debug("Searching books with term: {}", searchTerm);
        return bookDao.search(searchTerm);
    }

    /** {@inheritDoc} */
    @Override
    public Page<Book> searchPaginated(String searchTerm, int pageNumber, int pageSize) {
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageNumber < 0) {
            pageNumber = 0;
        }
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            logger.debug("Empty search term provided, returning paginated books");
            return findAllPaginated(pageNumber, pageSize);
        }
        logger.debug("Empty search term provided, returning paginated books");
        return bookDao.searchPaginated(searchTerm, pageNumber, pageSize);
    }

    /** {@inheritDoc} */
    @Override
    public Page<Book> findByGenrePaginated(String genre, int pageNumber, int pageSize) {
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (pageNumber < 0) {
            pageNumber = 0;
        }
        logger.debug("Retrieving books by genre: {}, page: {}, size: {}", genre, pageNumber, pageSize);
        return bookDao.findByGenrePaginated(genre, pageNumber, pageSize);
    }

    /** {@inheritDoc} */
    @Override
    public Book save(Book book) {
        if (book.getTotalCopies() == null) {
            book.setTotalCopies(1);
        }
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }

        logger.info("Saving new book: {}", book.getTitle());
        return bookDao.save(book);
    }

    /** {@inheritDoc} */
    @Override
    public Book update(Book book) {
        Optional<Book> existingBook = bookDao.findById(book.getId());
        if (existingBook.isEmpty()) {
            logger.warn("Attempted to update non-existent book with ID: {}", book.getId());
            throw new ServiceException("Book not found with id: " + book.getId());
        }

        logger.info("Updating book: {}", book.getTitle());
        return bookDao.update(book);
    }

    /** {@inheritDoc} */
    @Override
    public boolean delete(Long id) {
        logger.info("Deleting book with id: {}", id);
        return bookDao.deleteById(id);
    }
}
