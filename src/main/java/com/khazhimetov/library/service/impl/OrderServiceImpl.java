package com.khazhimetov.library.service.impl;

import com.khazhimetov.library.dao.BookDao;
import com.khazhimetov.library.dao.OrderDao;
import com.khazhimetov.library.exception.ServiceException;
import com.khazhimetov.library.model.Book;
import com.khazhimetov.library.model.Order;
import com.khazhimetov.library.model.OrderStatus;
import com.khazhimetov.library.model.OrderType;
import com.khazhimetov.library.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order service implementation for managing order operations.
 */
@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private OrderDao orderDao;
    private BookDao bookDao;

    @Autowired
    public OrderServiceImpl(OrderDao orderDao, BookDao bookDao) {
        this.orderDao = orderDao;
        this.bookDao = bookDao;
    }

    /** {@inheritDoc} */
    @Override
    public Order createOrder(Long userId, Long bookId, OrderType orderType) {
        logger.info("Attempting to create order for user {} and book {}", userId, bookId);
        Optional<Book> book = bookDao.findById(bookId);
        if (book.isEmpty()) {
            logger.warn("Book not found with id: {}", bookId);
            throw new ServiceException("Book not found with id: " + bookId);
        }

        if (book.get().getAvailableCopies() <= 0) {
            logger.warn("No available copies for book: {}", book.get().getTitle());
            throw new ServiceException("No copies available for book: " + book.get().getTitle());
        }

        if (hasActiveOrder(userId, bookId)) {
            logger.warn("User {} already has an active order for book {}", userId, bookId);
            throw new ServiceException("User already has an active order for this book");
        }

        Order order = new Order.Builder()
                .userId(userId)
                .bookId(bookId)
                .orderType(orderType)
                .status(OrderStatus.PENDING)
                .build();

        logger.info("Creating order for user {} and book {}", userId, bookId);
        return orderDao.save(order);
    }

    /** {@inheritDoc} */
    @Override
    public Order issueOrder(Long orderId, Long librarianId, int lendingDays) {
        logger.info("Issuing order {} by librarian {} for {} days", orderId, librarianId, lendingDays);
        Optional<Order> orderOpt = orderDao.findById(orderId);
        if (orderOpt.isEmpty()) {
            logger.warn("Order not found with id: {}", orderId);
            throw new ServiceException("Order not found with id: " + orderId);
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.PENDING) {
            logger.warn("Cannot issue order {} because it is not in PENDING status", orderId);
            throw new ServiceException("Order is not in PENDING status");
        }

        // Update order
        order.setStatus(OrderStatus.ISSUED);
        order.setLibrarianId(librarianId);
        order.setIssueDate(LocalDateTime.now());
        order.setDueDate(LocalDateTime.now().plusDays(lendingDays));

        // Update available copies
        bookDao.updateAvailableCopies(order.getBookId(), -1);

        logger.info("Issuing order {} by librarian {}", orderId, librarianId);
        return orderDao.update(order);
    }

    /** {@inheritDoc} */
    @Override
    public Order returnOrder(Long orderId, Long librarianId) {
        logger.info("Returning order {} by librarian {}", orderId, librarianId);
        Optional<Order> orderOpt = orderDao.findById(orderId);
        if (orderOpt.isEmpty()) {
            logger.warn("Order not found with id: {}", orderId);
            throw new ServiceException("Order not found with id: " + orderId);
        }

        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.ISSUED) {
            logger.warn("Cannot return order {} because it is not in ISSUED status", orderId);
            throw new ServiceException("Order is not in ISSUED status");
        }

        // Update order
        order.setStatus(OrderStatus.RETURNED);
        order.setReturnDate(LocalDateTime.now());
        order.setLibrarianId(librarianId);

        // Update available copies
        bookDao.updateAvailableCopies(order.getBookId(), 1);

        logger.info("Returning order {} by librarian {}", orderId, librarianId);
        return orderDao.update(order);
    }

    /** {@inheritDoc} */
    @Override
    public Order cancelOrder(Long orderId, Long userId) {
        logger.info("Cancelling order {} by user {}", orderId, userId);
        Optional<Order> orderOpt = orderDao.findById(orderId);
        if (orderOpt.isEmpty()) {
            logger.warn("Order not found with id: {}", orderId);
            throw new ServiceException("Order not found with id: " + orderId);
        }

        Order order = orderOpt.get();

        // Verify user owns the order
        if (!order.getUserId().equals(userId)) {
            logger.warn("User {} is not authorized to cancel order {}", userId, orderId);
            throw new ServiceException("User is not authorized to cancel this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            logger.warn("Order {} cannot be cancelled because it is not in PENDING status", orderId);
            throw new ServiceException("Only pending orders can be cancelled");
        }

        // Update order
        order.setStatus(OrderStatus.CANCELLED);

        logger.info("Cancelling order {} by user {}", orderId, userId);
        return orderDao.update(order);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Order> findById(Long id) {
        logger.debug("Finding order by ID: {}", id);
        return orderDao.findById(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<Order> findByUser(Long userId) {
        logger.debug("Finding orders for user ID: {}", userId);
        return orderDao.findByUserId(userId);
    }

    /** {@inheritDoc} */
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        logger.debug("Finding orders with status: {}", status);
        return orderDao.findByStatus(status);
    }

    /** {@inheritDoc} */
    @Override
    public List<Order> findAllWithDetails() {
        logger.debug("Retrieving all orders with details");
        return orderDao.findAllWithDetails();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasActiveOrder(Long userId, Long bookId) {
        boolean hasOrder = orderDao.findActiveOrderByUserAndBook(userId, bookId).isPresent();
        logger.debug("User {} has active order for book {}: {}", userId, bookId, hasOrder);
        return hasOrder;
    }
}
