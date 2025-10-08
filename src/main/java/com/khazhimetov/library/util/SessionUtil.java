package com.khazhimetov.library.util;

import com.khazhimetov.library.model.User;
import jakarta.servlet.http.HttpSession;

/**
 * Session utility class
 */
public class SessionUtil {

    public static final String USER_ATTRIBUTE = "user";

    /**
     * Get current user from session
     * @param session HTTP session
     * @return current user or null
     */
    public static User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute(USER_ATTRIBUTE);
    }

    /**
     * Set current user in session
     * @param session HTTP session
     * @param user user to set
     */
    public static void setCurrentUser(HttpSession session, User user) {
        session.setAttribute(USER_ATTRIBUTE, user);
    }

    /**
     * Remove current user from session
     * @param session HTTP session
     */
    public static void removeCurrentUser(HttpSession session) {
        session.removeAttribute(USER_ATTRIBUTE);
    }
}
