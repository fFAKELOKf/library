package com.khazhimetov.library.service;

import com.khazhimetov.library.dao.BookDao;
import com.khazhimetov.library.dao.OrderDao;
import com.khazhimetov.library.exception.ServiceException;
import com.khazhimetov.library.model.*;
import com.khazhimetov.library.model.Book;
import com.khazhimetov.library.model.Order;
import com.khazhimetov.library.model.OrderStatus;
import com.khazhimetov.library.model.OrderType;
import com.khazhimetov.library.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;
    @Mock
    private BookDao bookDao;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderDao, bookDao);
    }

    @Test
    void createOrder_SuccessfullyCreatesOrder() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        OrderType orderType = OrderType.HOME;

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Test Book");
        book.setAvailableCopies(3);

        when(bookDao.findById(bookId)).thenReturn(Optional.of(book));
        when(orderDao.findActiveOrderByUserAndBook(userId, bookId)).thenReturn(Optional.empty());
        when(orderDao.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(10L);
            return order;
        });

        // When
        Order result = orderService.createOrder(userId, bookId, orderType);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(bookId, result.getBookId());
        assertEquals(OrderType.HOME, result.getOrderType());
        assertEquals(OrderStatus.PENDING, result.getStatus());

        verify(orderDao).save(any(Order.class));
    }

    @Test
    void createOrder_ThrowsException_WhenBookNotFound() {
        // Given
        Long userId = 1L;
        Long bookId = 999L;
        OrderType orderType = OrderType.HOME;

        when(bookDao.findById(bookId)).thenReturn(Optional.empty());

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> orderService.createOrder(userId, bookId, orderType));

        assertTrue(exception.getMessage().contains("Book not found"));
        verify(orderDao, never()).save(any());
    }

    @Test
    void createOrder_ThrowsException_WhenNoCopiesAvailable() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        OrderType orderType = OrderType.HOME;

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Test Book");
        book.setAvailableCopies(0);

        when(bookDao.findById(bookId)).thenReturn(Optional.of(book));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> orderService.createOrder(userId, bookId, orderType));

        assertTrue(exception.getMessage().contains("No copies available"));
        verify(orderDao, never()).save(any());
    }

    @Test
    void createOrder_ThrowsException_WhenUserHasActiveOrder() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        OrderType orderType = OrderType.HOME;

        Book book = new Book();
        book.setId(bookId);
        book.setAvailableCopies(3);

        Order existingOrder = new Order();
        existingOrder.setId(5L);

        when(bookDao.findById(bookId)).thenReturn(Optional.of(book));
        when(orderDao.findActiveOrderByUserAndBook(userId, bookId))
                .thenReturn(Optional.of(existingOrder));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> orderService.createOrder(userId, bookId, orderType));

        assertTrue(exception.getMessage().contains("already has an active order"));
        verify(orderDao, never()).save(any());
    }

    @Test
    void issueOrder_SuccessfullyIssuesOrder() {
        // Given
        Long orderId = 1L;
        Long librarianId = 2L;
        int lendingDays = 14;

        Order order = new Order();
        order.setId(orderId);
        order.setBookId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderDao.findById(orderId)).thenReturn(Optional.of(order));
        when(orderDao.update(any(Order.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        // When
        Order result = orderService.issueOrder(orderId, librarianId, lendingDays);

        // Then
        assertEquals(OrderStatus.ISSUED, result.getStatus());
        assertEquals(librarianId, result.getLibrarianId());
        assertNotNull(result.getIssueDate());
        assertNotNull(result.getDueDate());
        assertTrue(result.getDueDate().isAfter(result.getIssueDate()));

        verify(bookDao).updateAvailableCopies(order.getBookId(), -1);
        verify(orderDao).update(any(Order.class));
    }

    @Test
    void issueOrder_ThrowsException_WhenOrderNotFound() {
        // Given
        Long orderId = 999L;
        Long librarianId = 2L;
        int lendingDays = 14;

        when(orderDao.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> orderService.issueOrder(orderId, librarianId, lendingDays));

        assertTrue(exception.getMessage().contains("Order not found"));
        verify(bookDao, never()).updateAvailableCopies(anyLong(), anyInt());
    }

    @Test
    void issueOrder_ThrowsException_WhenOrderNotPending() {
        // Given
        Long orderId = 1L;
        Long librarianId = 2L;
        int lendingDays = 14;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.ISSUED); // Already issued

        when(orderDao.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> orderService.issueOrder(orderId, librarianId, lendingDays));

        assertTrue(exception.getMessage().contains("not in PENDING status"));
        verify(bookDao, never()).updateAvailableCopies(anyLong(), anyInt());
    }

    @Test
    void returnOrder_SuccessfullyReturnsOrder() {
        // Given
        Long orderId = 1L;
        Long librarianId = 2L;

        Order order = new Order();
        order.setId(orderId);
        order.setBookId(1L);
        order.setStatus(OrderStatus.ISSUED);

        when(orderDao.findById(orderId)).thenReturn(Optional.of(order));
        when(orderDao.update(any(Order.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        // When
        Order result = orderService.returnOrder(orderId, librarianId);

        // Then
        assertEquals(OrderStatus.RETURNED, result.getStatus());
        assertEquals(librarianId, result.getLibrarianId());
        assertNotNull(result.getReturnDate());

        verify(bookDao).updateAvailableCopies(order.getBookId(), 1);
        verify(orderDao).update(any(Order.class));
    }

    @Test
    void returnOrder_ThrowsException_WhenOrderNotIssued() {
        // Given
        Long orderId = 1L;
        Long librarianId = 2L;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING); // Not issued yet

        when(orderDao.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> orderService.returnOrder(orderId, librarianId));

        assertTrue(exception.getMessage().contains("not in ISSUED status"));
        verify(bookDao, never()).updateAvailableCopies(anyLong(), anyInt());
    }

    @Test
    void cancelOrder_SuccessfullyCancelsOrder() {
        // Given
        Long orderId = 1L;
        Long userId = 1L;

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);

        when(orderDao.findById(orderId)).thenReturn(Optional.of(order));
        when(orderDao.update(any(Order.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        // When
        Order result = orderService.cancelOrder(orderId, userId);

        // Then
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderDao).update(any(Order.class));
    }

    @Test
    void cancelOrder_ThrowsException_WhenUserNotAuthorized() {
        // Given
        Long orderId = 1L;
        Long userId = 1L;
        Long differentUserId = 2L;

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(differentUserId); // Different user

        when(orderDao.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> orderService.cancelOrder(orderId, userId));

        assertTrue(exception.getMessage().contains("not authorized"));
        verify(orderDao, never()).update(any());
    }

    @Test
    void cancelOrder_ThrowsException_WhenOrderNotPending() {
        // Given
        Long orderId = 1L;
        Long userId = 1L;

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(OrderStatus.ISSUED); // Not pending

        when(orderDao.findById(orderId)).thenReturn(Optional.of(order));

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> orderService.cancelOrder(orderId, userId));

        assertTrue(exception.getMessage().contains("Only pending orders"));
        verify(orderDao, never()).update(any());
    }

    @Test
    void findById_ReturnsOrder_WhenExists() {
        // Given
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);

        when(orderDao.findById(orderId)).thenReturn(Optional.of(order));

        // When
        Optional<Order> result = orderService.findById(orderId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
        verify(orderDao).findById(orderId);
    }

    @Test
    void findByUser_ReturnsUserOrders() {
        // Given
        Long userId = 1L;
        List<Order> orders = Arrays.asList(new Order(), new Order());

        when(orderDao.findByUserId(userId)).thenReturn(orders);

        // When
        List<Order> result = orderService.findByUser(userId);

        // Then
        assertEquals(2, result.size());
        verify(orderDao).findByUserId(userId);
    }

    @Test
    void findByStatus_ReturnsOrdersWithStatus() {
        // Given
        OrderStatus status = OrderStatus.PENDING;
        List<Order> orders = Arrays.asList(new Order(), new Order(), new Order());

        when(orderDao.findByStatus(status)).thenReturn(orders);

        // When
        List<Order> result = orderService.findByStatus(status);

        // Then
        assertEquals(3, result.size());
        verify(orderDao).findByStatus(status);
    }

    @Test
    void findAllWithDetails_ReturnsOrdersWithDetails() {
        // Given
        List<Order> orders = Arrays.asList(new Order());
        when(orderDao.findAllWithDetails()).thenReturn(orders);

        // When
        List<Order> result = orderService.findAllWithDetails();

        // Then
        assertEquals(1, result.size());
        verify(orderDao).findAllWithDetails();
    }

    @Test
    void hasActiveOrder_ReturnsTrue_WhenActiveOrderExists() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        Order activeOrder = new Order();

        when(orderDao.findActiveOrderByUserAndBook(userId, bookId))
                .thenReturn(Optional.of(activeOrder));

        // When
        boolean result = orderService.hasActiveOrder(userId, bookId);

        // Then
        assertTrue(result);
        verify(orderDao).findActiveOrderByUserAndBook(userId, bookId);
    }

    @Test
    void hasActiveOrder_ReturnsFalse_WhenNoActiveOrder() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;

        when(orderDao.findActiveOrderByUserAndBook(userId, bookId))
                .thenReturn(Optional.empty());

        // When
        boolean result = orderService.hasActiveOrder(userId, bookId);

        // Then
        assertFalse(result);
        verify(orderDao).findActiveOrderByUserAndBook(userId, bookId);
    }
}
