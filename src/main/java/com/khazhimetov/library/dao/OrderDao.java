package com.khazhimetov.library.dao;

import com.khazhimetov.library.model.Order;
import com.khazhimetov.library.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Order DAO interface
 */
public interface OrderDao extends BasicDao<Order, Long> {
    /**
     * Find orders by user ID
     * @param userId user ID
     * @return list of user's orders
     */
    List<Order> findByUserId(Long userId);

    /**
     * Find orders by status
     * @param status order status
     * @return list of orders with specified status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find active order for book by user
     * @param userId user ID
     * @param bookId book ID
     * @return Optional containing active order if found
     */
    Optional<Order> findActiveOrderByUserAndBook(Long userId, Long bookId);

    /**
     * Find orders with full details (including user and book info)
     * @return list of orders with full details
     */
    List<Order> findAllWithDetails();

    /**
     * Find orders by status with full details
     * @param status order status
     * @return list of orders with full details
     */
    List<Order> findByStatusWithDetails(OrderStatus status);
}
