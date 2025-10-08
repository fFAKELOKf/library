package com.khazhimetov.library.controller;

import com.khazhimetov.library.exception.ServiceException;
import com.khazhimetov.library.exception.ValidationException;
import com.khazhimetov.library.model.Book;
import com.khazhimetov.library.model.Page;
import com.khazhimetov.library.model.User;
import com.khazhimetov.library.service.BookService;
import com.khazhimetov.library.service.UserService;
import com.khazhimetov.library.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for administrative operations including user management and book catalog operations.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final int DEFAULT_PAGE_SIZE = 20;

    private UserService userService;
    private BookService bookService;

    /**
     * Constructs the AdminController with user and book service dependencies.
     */
    @Autowired
    public AdminController(UserService userService, BookService bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }

    /**
     * Displays a list of all registered users.
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    /**
     * Toggles the active status of a user (enable/disable).
     */
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable("id") Long id,
                                   RedirectAttributes redirectAttributes) {
        try {
            logger.info("Toggling status for user with ID: {}", id);
            userService.toggleActiveStatus(id);
            redirectAttributes.addFlashAttribute("success", "User status updated");
        } catch (Exception e) {
            logger.error("Failed to toggle user status", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Displays a list of all books with search and pagination support.
     */
    @GetMapping("/books")
    public String listBooks(@RequestParam(required = false) String search,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            Model model) {
        Page<Book> bookPage;

        // Ensure page parameters are valid
        if (page < 0) page = 0;
        if (size <= 0) size = DEFAULT_PAGE_SIZE;

        if (search != null && !search.trim().isEmpty()) {
            bookPage = bookService.searchPaginated(search, page, size);
            model.addAttribute("search", search);
        } else {
            bookPage = bookService.findAllPaginated(page, size);
        }

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("newBook", new Book());

        // For pagination links
        if (search != null) {
            model.addAttribute("searchParam", "&search=" + search);
        } else {
            model.addAttribute("searchParam", "");
        }

        return "admin/books";
    }

    /**
     * Adds a new book to the system after validating input.
     */
    @PostMapping("/books")
    public String addBook(@ModelAttribute Book book,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String search,
                          RedirectAttributes redirectAttributes) {
        try {
            logger.info("Attempting to add book: {}", book);
            ValidationUtil.validateRequired(book.getTitle(), "Title");
            ValidationUtil.validateRequired(book.getAuthor(), "Author");
            ValidationUtil.validatePositive(book.getTotalCopies(), "Total copies");

            bookService.save(book);
            redirectAttributes.addFlashAttribute("success", "Book added successfully");
        } catch (ValidationException | ServiceException e) {
            logger.warn("Book creation failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("newBook", book);
        }

        // Maintain search and pagination state
        String redirect = "redirect:/admin/books?page=" + page;
        if (search != null && !search.isEmpty()) {
            redirect += "&search=" + search;
        }
        return redirect;
    }

    /**
     * Deletes a book by its ID.
     */
    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable("id") Long id,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) String search,
                             RedirectAttributes redirectAttributes) {
        try {
            logger.info("Attempting to delete book with ID: {}", id);
            bookService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Book deleted successfully");
        } catch (Exception e) {
            logger.error("Book deletion failed", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // Maintain search and pagination state
        String redirect = "redirect:/admin/books?page=" + page;
        if (search != null && !search.isEmpty()) {
            redirect += "&search=" + search;
        }
        return redirect;
    }

    /**
     * Shows the form for editing a book's details.
     */
    @GetMapping("/books/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        logger.info("Fetching book for editing with ID: {}", id);
        Book book = bookService.findById(id)
                .orElseThrow(() -> new ServiceException("Book with id " + id + " not found"));
        model.addAttribute("book", book);
        return "admin/book-edit";
    }

    /**
     * Updates an existing book's details.
     */
    @PostMapping("/books/{id}/edit")
    public String updateBook(@PathVariable("id") long id,
                             @ModelAttribute("book") Book book,
                             RedirectAttributes redirectAttributes) {
        logger.info("Updating book with ID: {}", id);
        book.setId(id);
        bookService.update(book);
        redirectAttributes.addFlashAttribute("success", "Book updated successfully");
        return "redirect:/admin/books";
    }
}