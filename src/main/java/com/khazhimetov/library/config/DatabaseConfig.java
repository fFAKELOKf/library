package com.khazhimetov.library.config;

import com.khazhimetov.library.dao.ConnectionPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Configuration class for database connection pool setup.
 * <p>
 * Initializes and exposes a singleton {@link ConnectionPool} instance
 * based on values from the application properties file.
 */
@Configuration
public class DatabaseConfig {

    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.username}")
    private String dbUsername;

    @Value("${db.password}")
    private String dbPassword;

    @Value("${db.pool.initial-size:5}")
    private int initialPoolSize;

    @Value("${db.pool.max-size:20}")
    private int maxPoolSize;

    private ConnectionPool connectionPool;

    /**
     * Provides the {@link ConnectionPool} bean to the Spring container.
     *
     * @return the configured singleton ConnectionPool instance
     */
    @Bean
    public ConnectionPool connectionPool() {
        return connectionPool;
    }

    /**
     * Initializes the {@link ConnectionPool} after dependency injection.
     * This method is invoked automatically after bean creation.
     */
    @PostConstruct
    public void init() {
        connectionPool = ConnectionPool.getInstance(
                dbUrl, dbUsername, dbPassword, initialPoolSize, maxPoolSize
        );
    }

    /**
     * Gracefully shuts down the connection pool during application shutdown.
     * Invoked automatically by Spring container before bean destruction.
     */
    @PreDestroy
    public void destroy() {
        if (connectionPool != null) {
            connectionPool.shutdown();
        }
    }
}
