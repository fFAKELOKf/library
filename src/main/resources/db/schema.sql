-- Database schema for Library Management System

-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS book_orders;
DROP TABLE IF EXISTS book_copies;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       user_role VARCHAR(50) NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_email (email),
                       INDEX idx_user_role (user_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create books table
CREATE TABLE books (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       isbn VARCHAR(20) UNIQUE,
                       genre VARCHAR(100),
                       description TEXT,
                       publication_year INT,
                       total_copies INT DEFAULT 1,
                       available_copies INT DEFAULT 1,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_title (title),
                       INDEX idx_author (author),
                       INDEX idx_genre (genre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create book_copies table
CREATE TABLE book_copies (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             book_id BIGINT NOT NULL,
                             inventory_number VARCHAR(50) NOT NULL UNIQUE,
                             status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                             INDEX idx_inventory (inventory_number),
                             INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create book_orders table
CREATE TABLE book_orders (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             book_id BIGINT NOT NULL,
                             book_copy_id BIGINT,
                             order_type VARCHAR(50) NOT NULL, -- 'HOME' or 'READING_ROOM'
                             status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                             order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             issue_date TIMESTAMP NULL,
                             due_date TIMESTAMP NULL,
                             return_date TIMESTAMP NULL,
                             librarian_id BIGINT,
                             notes TEXT,
                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                             FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                             FOREIGN KEY (book_copy_id) REFERENCES book_copies(id) ON DELETE SET NULL,
                             FOREIGN KEY (librarian_id) REFERENCES users(id) ON DELETE SET NULL,
                             INDEX idx_user_id (user_id),
                             INDEX idx_book_id (book_id),
                             INDEX idx_status (status),
                             INDEX idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;