package com.khazhimetov.library.dao;

import com.khazhimetov.library.dao.impl.OrderDaoImpl;
import com.khazhimetov.library.exception.DatabaseException;
import com.khazhimetov.library.model.*;
import com.khazhimetov.library.model.Order;
import com.khazhimetov.library.model.OrderStatus;
import com.khazhimetov.library.model.OrderType;
import org.junit.jupiter.api.AfterEach;
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
class OrderDaoImplTest {

    @Mock
    private ConnectionPool connectionPool;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private OrderDaoImpl orderDao;
    private MockedStatic<ConnectionPool> mockedConnectionPool;

    @BeforeEach
    void setUp() {
        // Keep the MockedStatic open for the entire test
        mockedConnectionPool = mockStatic(ConnectionPool.class);
        mockedConnectionPool.when(ConnectionPool::getInstance).thenReturn(connectionPool);
        orderDao = new OrderDaoImpl();
    }

    @AfterEach
    void tearDown() {
        // Close the MockedStatic after each test
        if (mockedConnectionPool != null) {
            mockedConnectionPool.close();
        }
    }

    @Test
    void findById_ReturnsOrder_WhenOrderExists() throws SQLException {
        // Given
        Long orderId = 1L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        mockOrderResultSet(orderId);

        // When
        Optional<Order> result = orderDao.findById(orderId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
        assertEquals(1L, result.get().getUserId());
        assertEquals(1L, result.get().getBookId());
        assertEquals(OrderType.HOME, result.get().getOrderType());
        assertEquals(OrderStatus.PENDING, result.get().getStatus());

        verify(preparedStatement).setLong(1, orderId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findById_ReturnsEmpty_WhenOrderNotFound() throws SQLException {
        // Given
        Long orderId = 999L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        Optional<Order> result = orderDao.findById(orderId);

        // Then
        assertFalse(result.isPresent());
        verify(preparedStatement).setLong(1, orderId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findByUserId_ReturnsUserOrders() throws SQLException {
        // Given
        Long userId = 1L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // 2 orders

        mockOrderResultSet(1L);

        // When
        List<Order> result = orderDao.findByUserId(userId);

        // Then
        assertEquals(2, result.size());
        verify(preparedStatement).setLong(1, userId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findByStatus_ReturnsOrdersWithStatus() throws SQLException {
        // Given
        OrderStatus status = OrderStatus.ISSUED;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        mockOrderResultSet(1L);

        // When
        List<Order> result = orderDao.findByStatus(status);

        // Then
        assertEquals(1, result.size());
        verify(preparedStatement).setString(1, status.getValue());
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findActiveOrderByUserAndBook_ReturnsOrder_WhenExists() throws SQLException {
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        mockOrderResultSet(1L);

        // When
        Optional<Order> result = orderDao.findActiveOrderByUserAndBook(userId, bookId);

        // Then
        assertTrue(result.isPresent());
        verify(preparedStatement).setLong(1, userId);
        verify(preparedStatement).setLong(2, bookId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void save_CreatesNewOrder_WithTransaction() throws SQLException {
        // Given
        Order newOrder = new Order.Builder()
                .userId(1L)
                .bookId(1L)
                .orderType(OrderType.HOME)
                .status(OrderStatus.PENDING)
                .build();

        ResultSet generatedKeys = mock(ResultSet.class);

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getLong(1)).thenReturn(10L);

        // When
        Order savedOrder = orderDao.save(newOrder);

        // Then
        assertEquals(10L, savedOrder.getId());

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(preparedStatement).setLong(1, newOrder.getUserId());
        verify(preparedStatement).setLong(2, newOrder.getBookId());
        verify(preparedStatement).setObject(3, newOrder.getBookCopyId());
        verify(preparedStatement).setString(4, newOrder.getOrderType().getValue());
        verify(preparedStatement).setString(5, newOrder.getStatus().getValue());

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void update_UpdatesExistingOrder() throws SQLException {
        // Given
        Order order = new Order.Builder()
                .id(1L)
                .userId(1L)
                .bookId(1L)
                .orderType(OrderType.HOME)
                .status(OrderStatus.ISSUED)
                .issueDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .librarianId(2L)
                .build();

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        Order updatedOrder = orderDao.update(order);

        // Then
        assertEquals(order.getId(), updatedOrder.getId());

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(preparedStatement).setLong(11, order.getId());

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findAllWithDetails_ReturnsOrdersWithUserAndBookInfo() throws SQLException {
        // Given
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        // Mock order fields first
        mockOrderResultSet(1L);
        // Then mock additional details
        mockOrderWithDetailsResultSet(false); // No librarian

        // When
        List<Order> result = orderDao.findAllWithDetails();

        // Then
        assertEquals(1, result.size());
        Order order = result.get(0);
        assertNotNull(order.getUser());
        assertNotNull(order.getBook());
        assertEquals("test@example.com", order.getUser().getEmail());
        assertEquals("Test Book", order.getBook().getTitle());

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findByStatusWithDetails_ReturnsOrdersWithDetails() throws SQLException {
        // Given
        OrderStatus status = OrderStatus.PENDING;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        // Mock order fields first
        mockOrderResultSet(1L);
        // Then mock additional details
        mockOrderWithDetailsResultSet(false); // No librarian

        // When
        List<Order> result = orderDao.findByStatusWithDetails(status);

        // Then
        assertEquals(1, result.size());
        Order order = result.get(0);
        assertNotNull(order.getUser());
        assertNotNull(order.getBook());

        verify(preparedStatement).setString(1, status.getValue());
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findAllWithDetails_ReturnsOrdersWithLibrarianInfo() throws SQLException {
        // Given
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        // Mock order with librarian
        mockOrderResultSet(1L);
        when(resultSet.getObject("librarian_id", Long.class)).thenReturn(2L);
        mockOrderWithDetailsResultSet(true); // With librarian

        // When
        List<Order> result = orderDao.findAllWithDetails();

        // Then
        assertEquals(1, result.size());
        Order order = result.get(0);
        assertNotNull(order.getLibrarian());
        assertEquals("lib@example.com", order.getLibrarian().getEmail());

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void deleteById_ReturnsTrue_WhenOrderDeleted() throws SQLException {
        // Given
        Long orderId = 1L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = orderDao.deleteById(orderId);

        // Then
        assertTrue(result);
        verify(preparedStatement).setLong(1, orderId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void deleteById_ReturnsFalse_WhenOrderNotFound() throws SQLException {
        // Given
        Long orderId = 999L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = orderDao.deleteById(orderId);

        // Then
        assertFalse(result);
        verify(preparedStatement).setLong(1, orderId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void save_ThrowsDatabaseException_WhenSQLExceptionOccurs() throws SQLException {
        // Given
        Order newOrder = new Order.Builder()
                .userId(1L)
                .bookId(1L)
                .orderType(OrderType.HOME)
                .build();

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("DB Error"));

        // When & Then
        assertThrows(DatabaseException.class, () -> orderDao.save(newOrder));
        verify(connection).rollback();
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void update_ThrowsDatabaseException_WhenNoRowsAffected() throws SQLException {
        // Given
        Order order = new Order.Builder()
                .id(999L)
                .userId(1L)
                .bookId(1L)
                .orderType(OrderType.HOME)
                .status(OrderStatus.PENDING)
                .build();

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When & Then
        DatabaseException exception = assertThrows(DatabaseException.class, () -> orderDao.update(order));
        assertTrue(exception.getMessage().contains("Updating order failed"));

        verify(connection).setAutoCommit(false);
        verify(connectionPool).releaseConnection(connection);
    }

    private void mockOrderResultSet(Long orderId) throws SQLException {
        when(resultSet.getLong("id")).thenReturn(orderId);
        when(resultSet.getLong("user_id")).thenReturn(1L);
        when(resultSet.getLong("book_id")).thenReturn(1L);
        when(resultSet.getObject("book_copy_id", Long.class)).thenReturn(null);
        when(resultSet.getString("order_type")).thenReturn("HOME");
        when(resultSet.getString("status")).thenReturn("PENDING");
        when(resultSet.getTimestamp("order_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("issue_date")).thenReturn(null);
        when(resultSet.getTimestamp("due_date")).thenReturn(null);
        when(resultSet.getTimestamp("return_date")).thenReturn(null);
        when(resultSet.getObject("librarian_id", Long.class)).thenReturn(null);
        when(resultSet.getString("notes")).thenReturn(null);
    }

    private void mockOrderWithDetailsResultSet(boolean withLibrarian) throws SQLException {
        // User details
        when(resultSet.getString("email")).thenReturn("test@example.com");
        when(resultSet.getString("first_name")).thenReturn("John");
        when(resultSet.getString("last_name")).thenReturn("Doe");

        // Book details
        when(resultSet.getString("title")).thenReturn("Test Book");
        when(resultSet.getString("author")).thenReturn("Test Author");
        when(resultSet.getString("genre")).thenReturn("Fiction");

        // Only mock librarian details if needed
        if (withLibrarian) {
            when(resultSet.getString("librarian_email")).thenReturn("lib@example.com");
            when(resultSet.getString("librarian_fname")).thenReturn("Jane");
            when(resultSet.getString("librarian_lname")).thenReturn("Smith");
        }
    }
}