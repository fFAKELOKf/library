package com.khazhimetov.library.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Initializes the Spring web application.
 * <p>
 * This class is automatically detected and executed during application startup.
 * It sets up the application context, registers the Spring DispatcherServlet,
 * and configures global settings such as encoding and security filters.
 */
public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // Create Spring application context
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class, DatabaseConfig.class, WebConfig.class);

        // Create and register the DispatcherServlet
        DispatcherServlet servlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic registration = servletContext.addServlet("dispatcher", servlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/");

        // Set encoding
        servletContext.setRequestCharacterEncoding("UTF-8");
        servletContext.setResponseCharacterEncoding("UTF-8");

        // Add filter for CSRF protection
        servletContext.addFilter("csrfFilter", new CsrfFilter())
                .addMappingForUrlPatterns(null, false, "/*");
    }
}