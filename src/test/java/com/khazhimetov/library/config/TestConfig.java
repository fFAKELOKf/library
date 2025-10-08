package com.khazhimetov.library.config;

import com.khazhimetov.library.dao.ConnectionPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Test configuration for unit tests.
 * This configuration is only active when the "test" profile is used.
 */
@Configuration
@Profile("test")
public class TestConfig {

    /**
     * Override ConnectionPool bean for testing to prevent actual database connections.
     * In unit tests, we mock the ConnectionPool, so this bean won't be used,
     * but it prevents Spring from trying to create the real one.
     */
    @Bean
    public ConnectionPool connectionPool() {
        // Return null as we'll be mocking this in tests
        return null;
    }
}