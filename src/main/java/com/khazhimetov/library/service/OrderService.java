package com.khazhimetov.library.service;

import com.khazhimetov.library.model.Order;
import com.khazhimetov.library.model.OrderStatus;
import com.khazhimetov.library.model.OrderType;

import java.util.List;
import java.util.Optional;

/**
 * Order service interface.
 */
public interface OrderService {
    /**
     * Create new order
     * @param userId user ID
     * @param bookId book ID
     * @param orderType order type
     * @return created order
     */
    Order createOrder(Long userId, Long bookId, OrderType orderType);

    /**
     * Issue order
     * @param orderId order ID
     * @param librarianId librarian ID
     * @param lendingDays number of days for lending
     * @return updated order
     */
    Order issueOrder(Long orderId, Long librarianId, int lendingDays);

    /**
     * Return order
     * @param orderId order ID
     * @param librarianId librarian ID
     * @return updated order
     */
    Order returnOrder(Long orderId, Long librarianId);

    /**
     * Cancel order
     * @param orderId order ID
     * @param userId user ID (for verification)
     * @return updated order
     */
    Order cancelOrder(Long orderId, Long userId);

    /**
     * Find order by ID
     * @param id order ID
     * @return order or empty if not found
     */
    Optional<Order> findById(Long id);

    /**
     * Find orders by user
     * @param userId user ID
     * @return list of user's orders
     */
    List<Order> findByUser(Long userId);

    /**
     * Find orders by status
     * @param status order status
     * @return list of orders with specified status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find all orders with details
     * @return list of orders with user and book details
     */
    List<Order> findAllWithDetails();

    /**
     * Check if user has active order for book
     * @param userId user ID
     * @param bookId book ID
     * @return true if user has active order
     */
    boolean hasActiveOrder(Long userId, Long bookId);
}
