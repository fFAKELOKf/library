package com.khazhimetov.library.controller;

import com.khazhimetov.library.model.Order;
import com.khazhimetov.library.model.OrderStatus;
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
 * Controller for handling librarian-related actions such as viewing, issuing, and returning orders.
 */
@Controller
@RequestMapping("/librarian")
public class LibrarianController {
    private static final Logger logger = LoggerFactory.getLogger(LibrarianController.class);
    private OrderService orderService;

    @Autowired
    public LibrarianController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Displays a list of orders with optional filtering by status.
     *
     * @param status optional order status filter
     * @param model  Spring MVC model
     * @return the librarian orders view
     */
    @GetMapping("/orders")
    public String listOrders(@RequestParam(required = false) String status,
                             Model model) {
        List<Order> orders;

        if (status != null && !status.isEmpty()) {
            logger.debug("Filtering orders by status: {}", status);
            orders = orderService.findByStatus(OrderStatus.fromString(status));
        } else {
            logger.debug("Retrieving all orders with details");
            orders = orderService.findAllWithDetails();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        return "librarian/orders";
    }

    /**
     * Issues an order and updates its status and due date.
     *
     * @param id                 the ID of the order to issue
     * @param days               the number of lending days
     * @param session            HTTP session to retrieve current librarian
     * @param redirectAttributes used to pass flash messages
     * @return redirection to the orders list
     */
    @PostMapping("/orders/{id}/issue")
    public String issueOrder(@PathVariable("id") Long id,
                             @RequestParam(defaultValue = "14") int days,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User librarian = SessionUtil.getCurrentUser(session);

        try {
            logger.info("Librarian {} issuing order {} for {} days", librarian.getEmail(), id, days);
            orderService.issueOrder(id, librarian.getId(), days);
            redirectAttributes.addFlashAttribute("success", "msg.order_issued");
        } catch (Exception e) {
            logger.error("Failed to issue order {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/librarian/orders";
    }

    /**
     * Processes the return of a book order.
     *
     * @param id                 the ID of the order to return
     * @param session            HTTP session to retrieve current librarian
     * @param redirectAttributes used to pass flash messages
     * @return redirection to the orders list
     */
    @PostMapping("/orders/{id}/return")
    public String returnOrder(@PathVariable("id") Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User librarian = SessionUtil.getCurrentUser(session);

        try {
            logger.info("Librarian {} returning order {}", librarian.getEmail(), id);
            orderService.returnOrder(id, librarian.getId());
            redirectAttributes.addFlashAttribute("success", "msg.order_returned");
        } catch (Exception e) {
            logger.error("Failed to return order {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/librarian/orders";
    }
}
