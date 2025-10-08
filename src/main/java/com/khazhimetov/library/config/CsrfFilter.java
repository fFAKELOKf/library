package com.khazhimetov.library.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Filter for Cross-Site Request Forgery (CSRF) protection.
 * <p>
 * This filter intercepts all HTTP requests and ensures that any non-GET request
 * contains a valid CSRF token that matches the one stored in the user's session.
 * The token is made available to views for inclusion in forms.
 */
public class CsrfFilter implements Filter {

    private static final String CSRF_TOKEN_ATTR = "csrfToken";
    private static final String CSRF_TOKEN_PARAM = "_csrf";
    private final SecureRandom random = new SecureRandom();

    /**
     * Intercepts requests to handle CSRF token generation and validation.
     *
     * @param request  the ServletRequest
     * @param response the ServletResponse
     * @param chain    the FilterChain to pass control to the next filter
     * @throws IOException      if an I/O error occurs during filtering
     * @throws ServletException if the request cannot be handled
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession();

        // Generate token if not exists
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        if (token == null) {
            token = generateToken();
            session.setAttribute(CSRF_TOKEN_ATTR, token);
        }

        // Make token available to views
        httpRequest.setAttribute(CSRF_TOKEN_ATTR, token);

        // Skip CSRF check for GET requests
        if ("GET".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Validate token for POST requests
        String requestToken = httpRequest.getParameter(CSRF_TOKEN_PARAM);
        if (requestToken == null || !requestToken.equals(token)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Generates a secure random token and encodes it in Base64.
     *
     * @return a securely generated CSRF token
     */
    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}