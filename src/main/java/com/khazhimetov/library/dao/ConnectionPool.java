package com.khazhimetov.library.dao;

import com.khazhimetov.library.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe singleton connection pool for managing JDBC connections.
 * <p>
 * Connections are created lazily and reused to avoid performance overhead.
 * Properly handles shutdown and reinitialization logic.
 */
public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static ConnectionPool instance;
    private static final Object LOCK = new Object();

    private final BlockingQueue<Connection> availableConnections;
    private final BlockingQueue<Connection> usedConnections;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    private final String url;
    private final String user;
    private final String password;
    private final int maxPoolSize;
    private final int initialPoolSize;

    /**
     * Private constructor to initialize the pool.
     *
     * @param url              database URL
     * @param user             database username
     * @param password         database password
     * @param initialPoolSize  number of connections to start with
     * @param maxPoolSize      maximum number of total connections
     */
    private ConnectionPool(String url, String user, String password,
                           int initialPoolSize, int maxPoolSize) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.initialPoolSize = initialPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.availableConnections = new LinkedBlockingQueue<>(maxPoolSize);
        this.usedConnections = new LinkedBlockingQueue<>(maxPoolSize);

        initializePool();
    }

    /**
     * Get singleton instance of ConnectionPool
     * @param url database URL
     * @param user database user
     * @param password database password
     * @param initialPoolSize initial pool size
     * @param maxPoolSize maximum pool size
     * @return ConnectionPool instance
     */
    public static ConnectionPool getInstance(String url, String user, String password,
                                             int initialPoolSize, int maxPoolSize) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    logger.info("Initializing ConnectionPool instance");
                    instance = new ConnectionPool(url, user, password, initialPoolSize, maxPoolSize);
                }
            }
        }
        return instance;
    }

    /**
     * Retrieves the already initialized pool instance.
     *
     * @return ConnectionPool instance
     */
    public static ConnectionPool getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionPool not initialized. Call getInstance with parameters first.");
        }
        return instance;
    }

    /**
     * Retrieves the already initialized pool instance.
     */
    private void initializePool() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            for (int i = 0; i < initialPoolSize; i++) {
                Connection connection = createConnection();
                availableConnections.offer(connection);
            }
            logger.info("Connection pool initialized with {} connections", initialPoolSize);
        } catch (ClassNotFoundException e) {
            logger.error("Database driver not found", e);
            throw new DatabaseException("MySQL driver not found", e);
        }
    }

    /**
     * Creates a new database connection.
     *
     * @return new {@link Connection}
     */
    private Connection createConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            logger.error("Failed to create database connection", e);
            throw new DatabaseException("Failed to create connection", e);
        }
    }

    /**
     * Provides a connection from the pool.
     *
     * @return an available {@link Connection}
     * @throws DatabaseException if no connections are available or an error occurs
     */
    public Connection getConnection() {
        if (isShutdown.get()) {
            throw new IllegalStateException("Connection pool is shutdown");
        }

        try {
            Connection connection = availableConnections.poll(2, TimeUnit.SECONDS);

            if (connection == null) {
                if (usedConnections.size() < maxPoolSize) {
                    logger.warn("No idle connections available; creating new connection");
                    connection = createConnection();
                } else {
                    throw new DatabaseException("Connection pool exhausted");
                }
            }

            if (!connection.isValid(1)) {
                logger.warn("Invalid connection encountered; recreating");
                connection = createConnection();
            }

            usedConnections.offer(connection);
            return connection;
        } catch (InterruptedException | SQLException e) {
            logger.error("Failed to obtain connection", e);
            throw new DatabaseException("Failed to get connection from pool", e);
        }
    }

    /**
     * Returns a connection to the pool or closes it if invalid.
     *
     * @param connection connection to release
     */
    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }

        try {
            if (connection.isValid(1) && !connection.getAutoCommit()) {
                connection.rollback();
                connection.setAutoCommit(true);
            }

            usedConnections.remove(connection);

            if (!isShutdown.get() && connection.isValid(1)) {
                availableConnections.offer(connection);
            } else {
                connection.close();
                logger.info("Connection closed instead of returning to pool");
            }
        } catch (SQLException e) {
            logger.error("Error releasing connection", e);
            try {
                connection.close();
            } catch (SQLException ex) {
                logger.error("Error closing connection", ex);
            }
        }
    }

    /**
     * Gracefully shuts down the connection pool and closes all connections.
     */
    public void shutdown() {
        isShutdown.set(true);

        closeConnections(availableConnections);
        closeConnections(usedConnections);

        logger.info("Connection pool shutdown complete");
    }

    /**
     * Closes all connections in the specified queue.
     *
     * @param connections queue of connections to close
     */
    private void closeConnections(BlockingQueue<Connection> connections) {
        Connection connection;
        while ((connection = connections.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }
}
