package com.khazhimetov.library.controller;

import com.khazhimetov.library.exception.InactiveAccountException;
import com.khazhimetov.library.exception.ServiceException;
import com.khazhimetov.library.exception.ValidationException;
import com.khazhimetov.library.model.User;
import com.khazhimetov.library.service.UserService;
import com.khazhimetov.library.util.SessionUtil;
import com.khazhimetov.library.util.ValidationUtil;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller for handling authentication operations such as login, registration, and logout.
 */
@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private UserService userService;

    /**
     * Constructor for injecting user service dependency.
     */
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the login form.
     *
     * @param model Spring MVC model object
     * @return the login view
     */
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }

    /**
     * Processes login requests with validation and role-based redirection.
     *
     * @param email the user's email
     * @param password the user's password
     * @param session the current HTTP session
     * @param redirectAttributes used to pass flash messages
     * @return the redirect URL
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            ValidationUtil.validateEmail(email);
            ValidationUtil.validatePassword(password);

            Optional<User> user = userService.authenticate(email, password);
            if (user.isPresent()) {
                SessionUtil.setCurrentUser(session, user.get());
                logger.info("User logged in: {}", email);

                // Redirect based on role
                switch (user.get().getRole()) {
                    case ADMIN:
                        return "redirect:/admin/users";
                    case LIBRARIAN:
                        return "redirect:/librarian/orders";
                    default:
                        return "redirect:/books";
                }
            } else {
                logger.warn("Login failed for user: {}", email);
                redirectAttributes.addFlashAttribute("error", "msg.login_failed");
                return "redirect:/login";
            }
        } catch (ValidationException e) {
            logger.warn("Validation error during login for user: {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/login";
        } catch (InactiveAccountException e) {
            logger.warn("Inactive account attempted login: {}", email);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/login";
        }
    }

    /**
     * Displays the registration form.
     *
     * @param model Spring MVC model object
     * @return the registration view
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Processes user registration with input validation.
     *
     * @param user the user data
     * @param confirmPassword confirmation password input
     * @param redirectAttributes used to pass flash messages
     * @return the redirect URL
     */
    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           @RequestParam String confirmPassword,
                           RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            ValidationUtil.validateEmail(user.getEmail());
            ValidationUtil.validatePassword(user.getPassword());
            ValidationUtil.validateRequired(user.getFirstName(), "First name");
            ValidationUtil.validateRequired(user.getLastName(), "Last name");

            if (!user.getPassword().equals(confirmPassword)) {
                throw new ValidationException("confirmPassword", "validation.password.mismatch");
            }

            userService.register(user);
            logger.info("User registered: {}", user.getEmail());
            redirectAttributes.addFlashAttribute("success", "msg.register_success");
            return "redirect:/login";

        } catch (ValidationException | ServiceException e) {
            logger.warn("Registration failed for user {}: {}", user.getEmail(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/register";
        }
    }

    /**
     * Logs the user out by invalidating the session.
     *
     * @param session the current HTTP session
     * @return the login view redirect
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SessionUtil.removeCurrentUser(session);
        session.invalidate();
        return "redirect:/login";
    }
}
