package com.khazhimetov.library.dao;

import com.khazhimetov.library.dao.impl.UserDaoImpl;
import com.khazhimetov.library.exception.DatabaseException;
import com.khazhimetov.library.model.User;
import com.khazhimetov.library.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDaoImplTest {

    @Mock
    private ConnectionPool connectionPool;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private UserDaoImpl userDao;

    @BeforeEach
    void setUp() {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(connectionPool);
            userDao = new UserDaoImpl();
        }
    }

    @Test
    void findById_ReturnsUser_WhenUserExists() throws SQLException {
        // Given
        Long userId = 1L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        // Mock ResultSet data
        when(resultSet.getLong("id")).thenReturn(userId);
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getString("password")).thenReturn("hashedPassword");
        when(resultSet.getString("first_name")).thenReturn("John");
        when(resultSet.getString("last_name")).thenReturn("Doe");
        when(resultSet.getString("user_role")).thenReturn("READER");
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        // When
        Optional<User> result = userDao.findById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
        assertEquals(UserRole.READER, result.get().getRole());
        assertTrue(result.get().isActive());

        verify(preparedStatement).setLong(1, userId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findById_ReturnsEmpty_WhenUserNotFound() throws SQLException {
        // Given
        Long userId = 999L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        Optional<User> result = userDao.findById(userId);

        // Then
        assertFalse(result.isPresent());
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findById_ThrowsDatabaseException_WhenSQLExceptionOccurs() throws SQLException {
        // Given
        Long userId = 1L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB Error"));

        // When & Then
        assertThrows(DatabaseException.class, () -> userDao.findById(userId));
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findByEmail_ReturnsUser_WhenUserExists() throws SQLException {
        // Given
        String email = "test@example.com";
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        // Mock ResultSet data
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("email")).thenReturn(email);
        when(resultSet.getString("password")).thenReturn("hashedPassword");
        when(resultSet.getString("first_name")).thenReturn("John");
        when(resultSet.getString("last_name")).thenReturn("Doe");
        when(resultSet.getString("user_role")).thenReturn("READER");
        when(resultSet.getBoolean("is_active")).thenReturn(true);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        // When
        Optional<User> result = userDao.findByEmail(email);

        // Then
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());

        verify(preparedStatement).setString(1, email);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findAll_ReturnsListOfUsers() throws SQLException {
        // Given
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Two users then stop

        // Mock ResultSet data for two users
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("email")).thenReturn("user1@example.com", "user2@example.com");
        when(resultSet.getString("password")).thenReturn("hash1", "hash2");
        when(resultSet.getString("first_name")).thenReturn("User", "User");
        when(resultSet.getString("last_name")).thenReturn("One", "Two");
        when(resultSet.getString("user_role")).thenReturn("READER", "LIBRARIAN");
        when(resultSet.getBoolean("is_active")).thenReturn(true, true);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        // When
        List<User> result = userDao.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("user1@example.com", result.get(0).getEmail());
        assertEquals("user2@example.com", result.get(1).getEmail());
        assertEquals(UserRole.READER, result.get(0).getRole());
        assertEquals(UserRole.LIBRARIAN, result.get(1).getRole());

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void save_CreatesNewUser_AndReturnsWithId() throws SQLException {
        // Given
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("hashedPassword");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setRole(UserRole.READER);
        newUser.setActive(true);

        ResultSet generatedKeys = mock(ResultSet.class);

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getLong(1)).thenReturn(10L);

        // When
        User savedUser = userDao.save(newUser);

        // Then
        assertEquals(10L, savedUser.getId());
        assertEquals("newuser@example.com", savedUser.getEmail());

        verify(preparedStatement).setString(1, newUser.getEmail());
        verify(preparedStatement).setString(2, newUser.getPassword());
        verify(preparedStatement).setString(3, newUser.getFirstName());
        verify(preparedStatement).setString(4, newUser.getLastName());
        verify(preparedStatement).setString(5, "READER");
        verify(preparedStatement).setBoolean(6, true);

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void save_ThrowsDatabaseException_WhenNoRowsAffected() throws SQLException {
        // Given
        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setPassword("password");
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setRole(UserRole.READER);

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0); // No rows affected

        // When & Then
        assertThrows(DatabaseException.class, () -> userDao.save(newUser));
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void update_UpdatesExistingUser() throws SQLException {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("updated@example.com");
        user.setFirstName("Updated");
        user.setLastName("User");
        user.setRole(UserRole.ADMIN);
        user.setActive(false);

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        User updatedUser = userDao.update(user);

        // Then
        assertEquals(user.getId(), updatedUser.getId());
        assertEquals(user.getEmail(), updatedUser.getEmail());

        verify(preparedStatement).setString(1, user.getEmail());
        verify(preparedStatement).setString(2, user.getFirstName());
        verify(preparedStatement).setString(3, user.getLastName());
        verify(preparedStatement).setString(4, "ADMIN");
        verify(preparedStatement).setBoolean(5, false);
        verify(preparedStatement).setLong(6, user.getId());

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void deleteById_ReturnsTrue_WhenUserDeleted() throws SQLException {
        // Given
        Long userId = 1L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = userDao.deleteById(userId);

        // Then
        assertTrue(result);
        verify(preparedStatement).setLong(1, userId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void deleteById_ReturnsFalse_WhenUserNotFound() throws SQLException {
        // Given
        Long userId = 999L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = userDao.deleteById(userId);

        // Then
        assertFalse(result);
        verify(connectionPool).releaseConnection(connection);
    }
}