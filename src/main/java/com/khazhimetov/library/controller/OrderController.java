package com.khazhimetov.library.controller;

import com.khazhimetov.library.model.Order;
import com.khazhimetov.library.model.User;
import com.khazhimetov.library.service.OrderService;
import com.khazhimetov.library.util.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Controller responsible for managing user orders.
 */
@Controller
@RequestMapping("/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Displays the list of orders for the currently logged-in user.
     *
     * @param session the current HTTP session
     * @param model   the model to add attributes to for the view
     * @return the view for listing user's orders, or redirect to login if unauthorized
     */
    @GetMapping("/my")
    public String myOrders(HttpSession session, Model model) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            logger.warn("Unauthorized access attempt to my orders");
            return "redirect:/login";
        }

        List<Order> orders = orderService.findByUser(user.getId());
        logger.info("Found {} orders for user {}", orders.size(), user.getId());
        model.addAttribute("orders", orders);
        return "orders/list";
    }

    /**
     * Cancels a pending order if the logged-in user is the owner.
     *
     * @param id                 the ID of the order to cancel
     * @param session            the current HTTP session
     * @param redirectAttributes the attributes for flash messages
     * @return redirection to user's orders page
     */
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            logger.warn("Unauthorized cancellation attempt for order {}", id);
            return "redirect:/login";
        }

        logger.info("User {} attempting to cancel order {}", user.getId(), id);

        try {
            Order order = orderService.findById(id).get();
            if (order == null) {
                logger.error("Order {} not found", id);
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/orders/my";
            }

            if (!order.getUserId().equals(user.getId())) {
                logger.error("User {} attempted to cancel order {} belonging to user {}",
                        user.getId(), id, order.getUserId());
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/orders/my";
            }

            // Check if order can be cancelled
            if (!"PENDING".equals(order.getStatus().name())) {
                logger.error("Cannot cancel order {} with status {}", id, order.getStatus());
                redirectAttributes.addFlashAttribute("error", "Cannot cancel order with status: " + order.getStatus());
                return "redirect:/orders/my";
            }

            orderService.cancelOrder(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "msg.order_cancelled");
        } catch (IllegalStateException e) {
            logger.error("Cannot cancel order {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error cancelling order {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred");
        }

        return "redirect:/orders/my";
    }
}
