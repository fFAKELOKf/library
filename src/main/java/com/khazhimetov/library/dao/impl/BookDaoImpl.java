package com.khazhimetov.library.dao.impl;

import com.khazhimetov.library.dao.BookDao;
import com.khazhimetov.library.dao.ConnectionPool;
import com.khazhimetov.library.exception.DatabaseException;
import com.khazhimetov.library.model.Book;
import com.khazhimetov.library.model.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based implementation of the {@link BookDao} interface.
 *
 * Provides CRUD operations and advanced search functionality for managing {@link Book} entities
 * in the library database. Utilizes a custom connection pool and includes support for pagination,
 * search, and genre-based filtering.
 */
public class BookDaoImpl implements BookDao {
    private static final Logger logger = LoggerFactory.getLogger(BookDaoImpl.class);
    private final ConnectionPool connectionPool;

    /**
     * Constructs a new instance of {@code BookDaoImpl} using a singleton {@link ConnectionPool}.
     */
    public BookDaoImpl() {
        this.connectionPool = ConnectionPool.getInstance();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Book> findById(Long id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToBook(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding book by id: {}", id, e);
            throw new DatabaseException("Error finding book by id", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Book> findAll() {
        String sql = "SELECT * FROM books ORDER BY title";
        Connection conn = null;
        List<Book> books = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
            return books;
        } catch (SQLException e) {
            logger.error("Error finding all books", e);
            throw new DatabaseException("Error finding all books", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Page<Book> findAllPaginated(int pageNumber, int pageSize) {
        String sql = "SELECT * FROM books ORDER BY title LIMIT ? OFFSET ?";
        Connection conn = null;
        List<Book> books = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();

            // Get total count
            long totalElements = countAll();

            // Get page data
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, pageSize);
            stmt.setInt(2, pageNumber * pageSize);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

            return new Page<>(books, pageNumber, pageSize, totalElements);
        } catch (SQLException e) {
            logger.error("Error finding books with pagination", e);
            throw new DatabaseException("Error finding books with pagination", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Book> findByGenre(String genre) {
        String sql = "SELECT * FROM books WHERE LOWER(genre) = LOWER(?) ORDER BY title";
        Connection conn = null;
        List<Book> books = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, genre);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
            return books;
        } catch (SQLException e) {
            logger.error("Error finding books by genre: {}", genre, e);
            throw new DatabaseException("Error finding books by genre", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Page<Book> findByGenrePaginated(String genre, int pageNumber, int pageSize) {
        String sql = "SELECT * FROM books WHERE LOWER(genre) = LOWER(?) ORDER BY title LIMIT ? OFFSET ?";
        Connection conn = null;
        List<Book> books = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();

            // Get total count
            long totalElements = countByGenre(genre);

            // Get page data
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, genre);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, pageNumber * pageSize);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

            return new Page<>(books, pageNumber, pageSize, totalElements);
        } catch (SQLException e) {
            logger.error("Error finding books by genre with pagination: {}", genre, e);
            throw new DatabaseException("Error finding books by genre with pagination", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Book> search(String searchTerm) {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(?) " +
                "OR LOWER(author) LIKE LOWER(?) OR LOWER(genre) LIKE LOWER(?) " +
                "OR LOWER(description) LIKE LOWER(?) ORDER BY title";
        Connection conn = null;
        List<Book> books = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
            return books;
        } catch (SQLException e) {
            logger.error("Error searching books: {}", searchTerm, e);
            throw new DatabaseException("Error searching books", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Page<Book> searchPaginated(String searchTerm, int pageNumber, int pageSize) {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(?) " +
                "OR LOWER(author) LIKE LOWER(?) OR LOWER(genre) LIKE LOWER(?) " +
                "OR LOWER(description) LIKE LOWER(?) ORDER BY title LIMIT ? OFFSET ?";
        Connection conn = null;
        List<Book> books = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();

            // Get total count
            long totalElements = countBySearchTerm(searchTerm);

            // Get page data
            PreparedStatement stmt = conn.prepareStatement(sql);
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            stmt.setInt(5, pageSize);
            stmt.setInt(6, pageNumber * pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

            return new Page<>(books, pageNumber, pageSize, totalElements);
        } catch (SQLException e) {
            logger.error("Error searching books with pagination: {}", searchTerm, e);
            throw new DatabaseException("Error searching books with pagination", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM books";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting all books", e);
            throw new DatabaseException("Error counting all books", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long countBySearchTerm(String searchTerm) {
        String sql = "SELECT COUNT(*) FROM books WHERE LOWER(title) LIKE LOWER(?) " +
                "OR LOWER(author) LIKE LOWER(?) OR LOWER(genre) LIKE LOWER(?) " +
                "OR LOWER(description) LIKE LOWER(?)";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting books by search term: {}", searchTerm, e);
            throw new DatabaseException("Error counting books by search term", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long countByGenre(String genre) {
        String sql = "SELECT COUNT(*) FROM books WHERE LOWER(genre) = LOWER(?)";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, genre);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting books by genre: {}", genre, e);
            throw new DatabaseException("Error counting books by genre", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Book save(Book book) {
        String sql = "INSERT INTO books (title, author, isbn, genre, description, " +
                "publication_year, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getGenre());
            stmt.setString(5, book.getDescription());
            stmt.setObject(6, book.getPublicationYear());
            stmt.setInt(7, book.getTotalCopies());
            stmt.setInt(8, book.getAvailableCopies());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating book failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    book.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("Creating book failed, no ID obtained.");
                }
            }

            conn.commit();
            logger.info("Book saved successfully: {}", book.getTitle());
            return book;
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.error("Error rolling back 'book save' transaction", ex);
            }
            logger.error("Error saving book", e);
            throw new DatabaseException("Error saving book", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Book update(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, genre = ?, " +
                "description = ?, publication_year = ?, total_copies = ?, available_copies = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getGenre());
            stmt.setString(5, book.getDescription());
            stmt.setObject(6, book.getPublicationYear());
            stmt.setInt(7, book.getTotalCopies());
            stmt.setInt(8, book.getAvailableCopies());
            stmt.setLong(9, book.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Updating book failed, no rows affected.");
            }

            conn.commit();
            logger.info("Book updated successfully: {}", book.getTitle());
            return book;
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.error("Error rolling back 'book update' transaction", ex);
            }
            logger.error("Error updating book", e);
            throw new DatabaseException("Error updating book", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM books WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            logger.info("Book deleted: {}", id);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting book", e);
            throw new DatabaseException("Error deleting book", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateAvailableCopies(Long bookId, int delta) {
        String sql = "UPDATE books SET available_copies = available_copies + ? WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, delta);
            stmt.setLong(2, bookId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Updating available copies failed, no rows affected.");
            }

            logger.info("Available copies updated for book {}: {}", bookId, delta);
        } catch (SQLException e) {
            logger.error("Error updating available copies", e);
            throw new DatabaseException("Error updating available copies", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /**
     * Maps a {@link ResultSet} row to a {@link Book} object.
     *
     * @param rs the result set containing book data
     * @return a {@link Book} instance populated from the current row of the result set
     * @throws SQLException if any column access fails
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setGenre(rs.getString("genre"));
        book.setDescription(rs.getString("description"));
        book.setPublicationYear(rs.getObject("publication_year", Integer.class));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        book.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        book.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return book;
    }
}