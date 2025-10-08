package com.khazhimetov.library.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateInputException;

/**
 * Global exception handler for the application.
 * <p>
 * This class handles exceptions that are thrown by any controller across the application.
 * It ensures user-friendly error views and logs unexpected behaviors.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles exceptions related to missing or malformed Thymeleaf templates.
     *
     * @param e the TemplateInputException thrown
     * @return a ModelAndView pointing to the error page with a descriptive message
     */
    @ExceptionHandler(TemplateInputException.class)
    public ModelAndView handleTemplateInputException(TemplateInputException e) {
        logger.error("Template error: ", e);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("error", "Template not found: " + e.getMessage());
        return mav;
    }

    /**
     * Handles all other uncaught exceptions in the application.
     *
     * @param e the general Exception thrown
     * @return a ModelAndView pointing to the error page with a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception e) {
        logger.error("Unexpected error: ", e);
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("error", "An unexpected error occurred: " + e.getMessage());
        return mav;
    }
}
