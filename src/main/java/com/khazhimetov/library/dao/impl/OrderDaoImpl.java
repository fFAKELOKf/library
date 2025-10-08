package com.khazhimetov.library.dao.impl;

import com.khazhimetov.library.dao.ConnectionPool;
import com.khazhimetov.library.dao.OrderDao;
import com.khazhimetov.library.exception.DatabaseException;
import com.khazhimetov.library.model.*;
import com.khazhimetov.library.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based implementation of the {@link OrderDao} interface.
 * <p>
 * Manages {@link Order} records in the system, including creation, retrieval,
 * updates, deletions, and searches with or without related entity details.
 */
public class OrderDaoImpl implements OrderDao {
    private static final Logger logger = LoggerFactory.getLogger(OrderDaoImpl.class);
    private final ConnectionPool connectionPool;

    /**
     * Constructs a new instance of {@code OrderDaoImpl} using the singleton connection pool.
     */
    public OrderDaoImpl() {
        this.connectionPool = ConnectionPool.getInstance();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Order> findById(Long id) {
        String sql = "SELECT * FROM book_orders WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToOrder(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding order by id: {}", id, e);
            throw new DatabaseException("Error finding order by id", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Order> findAll() {
        String sql = "SELECT * FROM book_orders ORDER BY order_date DESC";
        Connection conn = null;
        List<Order> orders = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding all orders", e);
            throw new DatabaseException("Error finding all orders", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Order> findByUserId(Long userId) {
        String sql = "SELECT * FROM book_orders WHERE user_id = ? ORDER BY order_date DESC";
        Connection conn = null;
        List<Order> orders = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding orders by user id: {}", userId, e);
            throw new DatabaseException("Error finding orders by user id", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        String sql = "SELECT * FROM book_orders WHERE status = ? ORDER BY order_date DESC";
        Connection conn = null;
        List<Order> orders = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.getValue());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding orders by status: {}", status, e);
            throw new DatabaseException("Error finding orders by status", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Order> findActiveOrderByUserAndBook(Long userId, Long bookId) {
        String sql = "SELECT * FROM book_orders WHERE user_id = ? AND book_id = ? " +
                "AND status IN ('PENDING', 'ISSUED') ORDER BY order_date DESC LIMIT 1";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            stmt.setLong(2, bookId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToOrder(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding active order for user {} and book {}", userId, bookId, e);
            throw new DatabaseException("Error finding active order", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO book_orders (user_id, book_id, book_copy_id, order_type, " +
                "status, order_date, issue_date, due_date, return_date, librarian_id, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, order.getUserId());
            stmt.setLong(2, order.getBookId());
            stmt.setObject(3, order.getBookCopyId());
            stmt.setString(4, order.getOrderType().getValue());
            stmt.setString(5, order.getStatus().getValue());
            stmt.setTimestamp(6, Timestamp.valueOf(order.getOrderDate()));
            stmt.setTimestamp(7, order.getIssueDate() != null ?
                    Timestamp.valueOf(order.getIssueDate()) : null);
            stmt.setTimestamp(8, order.getDueDate() != null ?
                    Timestamp.valueOf(order.getDueDate()) : null);
            stmt.setTimestamp(9, order.getReturnDate() != null ?
                    Timestamp.valueOf(order.getReturnDate()) : null);
            stmt.setObject(10, order.getLibrarianId());
            stmt.setString(11, order.getNotes());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("Creating order failed, no ID obtained.");
                }
            }

            conn.commit();
            logger.info("Order saved successfully: {}", order.getId());
            return order;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.error("Error rolling back transaction", ex);
            }
            logger.error("Error saving order", e);
            throw new DatabaseException("Error saving order", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Order update(Order order) {
        String sql = "UPDATE book_orders SET user_id = ?, book_id = ?, book_copy_id = ?, " +
                "order_type = ?, status = ?, issue_date = ?, due_date = ?, return_date = ?, " +
                "librarian_id = ?, notes = ? WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, order.getUserId());
            stmt.setLong(2, order.getBookId());
            stmt.setObject(3, order.getBookCopyId());
            stmt.setString(4, order.getOrderType().getValue());
            stmt.setString(5, order.getStatus().getValue());
            stmt.setTimestamp(6, order.getIssueDate() != null ?
                    Timestamp.valueOf(order.getIssueDate()) : null);
            stmt.setTimestamp(7, order.getDueDate() != null ?
                    Timestamp.valueOf(order.getDueDate()) : null);
            stmt.setTimestamp(8, order.getReturnDate() != null ?
                    Timestamp.valueOf(order.getReturnDate()) : null);
            stmt.setObject(9, order.getLibrarianId());
            stmt.setString(10, order.getNotes());
            stmt.setLong(11, order.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Updating order failed, no rows affected.");
            }

            conn.commit();
            logger.info("Order updated successfully: {}", order.getId());
            return order;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                logger.error("Error rolling back transaction", ex);
            }
            logger.error("Error updating order", e);
            throw new DatabaseException("Error updating order", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM book_orders WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            logger.info("Order deleted: {}", id);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting order", e);
            throw new DatabaseException("Error deleting order", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Order> findAllWithDetails() {
        String sql = "SELECT o.*, u.email, u.first_name, u.last_name, " +
                "b.title, b.author, b.genre, " +
                "l.email as librarian_email, l.first_name as librarian_fname, " +
                "l.last_name as librarian_lname " +
                "FROM book_orders o " +
                "JOIN users u ON o.user_id = u.id " +
                "JOIN books b ON o.book_id = b.id " +
                "LEFT JOIN users l ON o.librarian_id = l.id " +
                "ORDER BY o.order_date DESC";

        Connection conn = null;
        List<Order> orders = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrderWithDetails(rs));
            }
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding orders with details", e);
            throw new DatabaseException("Error finding orders with details", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Order> findByStatusWithDetails(OrderStatus status) {
        String sql = "SELECT o.*, u.email, u.first_name, u.last_name, " +
                "b.title, b.author, b.genre, " +
                "l.email as librarian_email, l.first_name as librarian_fname, " +
                "l.last_name as librarian_lname " +
                "FROM book_orders o " +
                "JOIN users u ON o.user_id = u.id " +
                "JOIN books b ON o.book_id = b.id " +
                "LEFT JOIN users l ON o.librarian_id = l.id " +
                "WHERE o.status = ? " +
                "ORDER BY o.order_date DESC";

        Connection conn = null;
        List<Order> orders = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.getValue());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrderWithDetails(rs));
            }
            return orders;
        } catch (SQLException e) {
            logger.error("Error finding orders by status with details", e);
            throw new DatabaseException("Error finding orders by status with details", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /**
     * Maps a result set row to a basic {@link Order} entity.
     *
     * @param rs result set containing order fields
     * @return populated {@link Order} instance
     * @throws SQLException if result set parsing fails
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setUserId(rs.getLong("user_id"));
        order.setBookId(rs.getLong("book_id"));
        order.setBookCopyId(rs.getObject("book_copy_id", Long.class));
        order.setOrderType(OrderType.fromString(rs.getString("order_type")));
        order.setStatus(OrderStatus.fromString(rs.getString("status")));
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());

        Timestamp issueDate = rs.getTimestamp("issue_date");
        if (issueDate != null) order.setIssueDate(issueDate.toLocalDateTime());

        Timestamp dueDate = rs.getTimestamp("due_date");
        if (dueDate != null) order.setDueDate(dueDate.toLocalDateTime());

        Timestamp returnDate = rs.getTimestamp("return_date");
        if (returnDate != null) order.setReturnDate(returnDate.toLocalDateTime());

        order.setLibrarianId(rs.getObject("librarian_id", Long.class));
        order.setNotes(rs.getString("notes"));

        return order;
    }

    /**
     * Maps a result set row to a full {@link Order} entity, including user, book, and librarian details.
     *
     * @param rs result set containing joined data
     * @return fully populated {@link Order} instance
     * @throws SQLException if result set parsing fails
     */
    private Order mapResultSetToOrderWithDetails(ResultSet rs) throws SQLException {
        Order order = mapResultSetToOrder(rs);

        // Map user details
        User user = new User();
        user.setId(order.getUserId());
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        order.setUser(user);

        // Map book details
        Book book = new Book();
        book.setId(order.getBookId());
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setGenre(rs.getString("genre"));
        order.setBook(book);

        // Map librarian details if exists
        if (order.getLibrarianId() != null) {
            User librarian = new User();
            librarian.setId(order.getLibrarianId());
            librarian.setEmail(rs.getString("librarian_email"));
            librarian.setFirstName(rs.getString("librarian_fname"));
            librarian.setLastName(rs.getString("librarian_lname"));
            order.setLibrarian(librarian);
        }

        return order;
    }
}
