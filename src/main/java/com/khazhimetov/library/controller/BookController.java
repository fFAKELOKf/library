package com.khazhimetov.library.controller;

import com.khazhimetov.library.model.*;
import com.khazhimetov.library.model.*;
import com.khazhimetov.library.service.BookService;
import com.khazhimetov.library.service.OrderService;
import com.khazhimetov.library.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller responsible for handling book listing, viewing, and requesting operations.
 */
@Controller
@RequestMapping("/books")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);
    private static final int DEFAULT_PAGE_SIZE = 12;

    private BookService bookService;
    private OrderService orderService;

    @Autowired
    public BookController(BookService bookService, OrderService orderService) {
        this.bookService = bookService;
        this.orderService = orderService;
    }

    /**
     * Displays a paginated list of books with optional search and genre filtering.
     *
     * @param search the search query string
     * @param genre the genre to filter by
     * @param page the current page number
     * @param size the number of items per page
     * @param model the model to store view attributes
     * @return the book list view
     */
    @GetMapping
    public String listBooks(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String genre,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "12") int size,
                            Model model) {
        Page<Book> bookPage;

        // Ensure page parameters are valid
        if (page < 0) page = 0;
        if (size <= 0) size = DEFAULT_PAGE_SIZE;

        if (search != null && !search.trim().isEmpty()) {
            logger.debug("Searching books with query: {}", search);
            bookPage = bookService.searchPaginated(search, page, size);
            model.addAttribute("search", search);
        } else if (genre != null && !genre.trim().isEmpty()) {
            logger.debug("Filtering books by genre: {}", genre);
            bookPage = bookService.findByGenrePaginated(genre, page, size);
            model.addAttribute("genre", genre);
        } else {
            bookPage = bookService.findAllPaginated(page, size);
        }

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        // For pagination links
        if (search != null) {
            model.addAttribute("searchParam", "&search=" + search);
        } else if (genre != null) {
            model.addAttribute("searchParam", "&genre=" + genre);
        } else {
            model.addAttribute("searchParam", "");
        }

        return "books/list";
    }

    /**
     * Displays the detail view of a specific book.
     *
     * @param id the ID of the book
     * @param model the model to store book data
     * @param session the HTTP session
     * @return the book detail view
     */
    @GetMapping("/{id}")
    public String viewBook(@PathVariable("id") Long id, Model model, HttpSession session) {
        Optional<Book> book = bookService.findById(id);
        if (book.isEmpty()) {
            logger.warn("Book with ID {} not found", id);
            return "redirect:/books";
        }

        model.addAttribute("book", book.get());

        // Check if user has active order for this book
        User user = SessionUtil.getCurrentUser(session);
        if (user != null) {
            boolean hasActiveOrder = orderService.hasActiveOrder(user.getId(), id);
            model.addAttribute("hasActiveOrder", hasActiveOrder);
        }

        return "books/detail";
    }

    /**
     * Processes a book request by a user.
     *
     * @param id the ID of the book
     * @param orderType the type of order (e.g., HOME or READING ROOM)
     * @param session the HTTP session
     * @param redirectAttributes attributes for passing flash messages
     * @return the redirect URL
     */
    @PostMapping("/{id}/request")
    public String requestBook(@PathVariable("id") Long id,
                              @RequestParam OrderType orderType,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            logger.warn("Unauthenticated user tried to request a book");
            return "redirect:/login";
        }

        try {
            logger.info("User {} requested book {} as {}", user.getEmail(), id, orderType);
            Order order = orderService.createOrder(user.getId(), id, orderType);
            redirectAttributes.addFlashAttribute("success", "msg.book_requested");
        } catch (Exception e) {
            logger.error("Failed to create book request for user {}: {}", user.getEmail(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/books/" + id;
    }
}