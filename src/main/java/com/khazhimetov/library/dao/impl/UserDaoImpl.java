package com.khazhimetov.library.dao.impl;

import com.khazhimetov.library.dao.ConnectionPool;
import com.khazhimetov.library.dao.UserDao;
import com.khazhimetov.library.exception.DatabaseException;
import com.khazhimetov.library.model.User;
import com.khazhimetov.library.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based implementation of the {@link UserDao} interface.
 *
 * Provides methods for CRUD operations and queries on {@link User} entities.
 * Relies on a custom {@link ConnectionPool} to manage database connections.
 */
public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);
    private final ConnectionPool connectionPool;

    public UserDaoImpl() {
        this.connectionPool = ConnectionPool.getInstance();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding user by id: {}", id, e);
            throw new DatabaseException("Error finding user by id", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new DatabaseException("Error finding user by email", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        Connection conn = null;
        List<User> users = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            logger.error("Error finding all users", e);
            throw new DatabaseException("Error finding all users", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (email, password, first_name, last_name, user_role, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getRole().getValue());
            stmt.setBoolean(6, user.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                } else {
                    throw new DatabaseException("Creating user failed, no ID obtained.");
                }
            }

            logger.info("User saved successfully: {}", user.getEmail());
            return user;
        } catch (SQLException e) {
            logger.error("Error saving user", e);
            throw new DatabaseException("Error saving user", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, first_name = ?, last_name = ?, " +
                "user_role = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getRole().getValue());
            stmt.setBoolean(5, user.isActive());
            stmt.setLong(6, user.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Updating user failed, no rows affected.");
            }

            logger.info("User updated successfully: {}", user.getEmail());
            return user;
        } catch (SQLException e) {
            logger.error("Error updating user", e);
            throw new DatabaseException("Error updating user", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = null;

        try {
            conn = connectionPool.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            logger.info("User deleted: {}", id);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting user", e);
            throw new DatabaseException("Error deleting user", e);
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    /**
     * Maps a {@link ResultSet} row to a {@link User} object.
     *
     * @param rs the result set containing user data
     * @return a {@link User} instance populated from the current row of the result set
     * @throws SQLException if any column access fails
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setRole(UserRole.fromString(rs.getString("user_role")));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    }
}