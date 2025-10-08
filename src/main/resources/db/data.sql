-- Initial data for Library Management System

-- Insert admin user (password: admin123)
INSERT INTO users (email, password, first_name, last_name, user_role)
VALUES ('admin@library.com', '$2a$10$N.fKJPV.ZPGzYKqWYYZDz.Tg7gHvK/cOZKVlDN7eVQIQPV.WUXRbm', 'Admin', 'User', 'ADMIN');

-- Insert librarian user (password: lib123)
INSERT INTO users (email, password, first_name, last_name, user_role)
VALUES ('librarian@library.com', '$2a$10$HhTZpBHPCVcvHRqXTlbzx.Kv4eYQOjLVZm9ywb5WRRpImRCzRgn.a', 'John', 'Smith',
        'LIBRARIAN');

-- Insert reader users (password: reader123)
INSERT INTO users (email, password, first_name, last_name, user_role)
VALUES ('reader1@example.com', '$2a$10$q2Q4/MvP.yUzYzVGIBpXYORwEqQhFOmIwITrSBGOFVP1EHCnZDKwO', 'Alice', 'Johnson',
        'READER'),
       ('reader2@example.com', '$2a$10$q2Q4/MvP.yUzYzVGIBpXYORwEqQhFOmIwITrSBGOFVP1EHCnZDKwO', 'Bob', 'Williams',
        'READER');

-- Insert books
INSERT INTO books (title, author, isbn, genre, description, publication_year, total_copies, available_copies)
VALUES ('1984', 'George Orwell', '978-0452284234', 'Dystopian Fiction',
        'A dystopian novel set in a totalitarian society.', 1949, 3, 3),
       ('To Kill a Mockingbird', 'Harper Lee', '978-0061120084', 'Classic Fiction',
        'A classic of modern American literature.', 1960, 2, 2),
       ('The Great Gatsby', 'F. Scott Fitzgerald', '978-0743273565', 'Classic Fiction',
        'A novel about the American Dream.', 1925, 2, 2),
       ('Pride and Prejudice', 'Jane Austen', '978-0141439518', 'Romance', 'A romantic novel of manners.', 1813, 2, 2),
       ('The Catcher in the Rye', 'J.D. Salinger', '978-0316769174', 'Coming-of-age',
        'A story about teenage rebellion and angst.', 1951, 1, 1),
       ('Brave New World', 'Aldous Huxley', '978-0060850524', 'Science Fiction',
        'A dystopian novel about a technologically advanced future.', 1932, 2, 2),
       ('Animal Farm', 'George Orwell', '978-0452284241', 'Political Satire',
        'An allegorical novella about a group of farm animals.', 1945, 2, 2),
       ('The Lord of the Rings', 'J.R.R. Tolkien', '978-0544003415', 'Fantasy', 'An epic fantasy trilogy.', 1954, 3, 3),
       ('Harry Potter and the Philosopher''s Stone', 'J.K. Rowling', '978-0747532699', 'Fantasy',
        'The first book in the Harry Potter series.', 1997, 4, 4),
       ('The Hobbit', 'J.R.R. Tolkien', '978-0547928227', 'Fantasy', 'A fantasy novel and children''s book.', 1937, 2,
        2),
       ('Fahrenheit 451', 'Ray Bradbury', '978-1451673319', 'Dystopian Fiction',
        'A novel about a future society where books are outlawed and burned.', 1953, 3, 3),
       ('The Alchemist', 'Paulo Coelho', '978-0061122415', 'Philosophical Fiction',
        'A spiritual journey of a shepherd in search of his destiny.', 1988, 2, 2),
       ('The Book Thief', 'Markus Zusak', '978-0375842207', 'Historical Fiction',
        'A young girl finds solace in books during WWII in Nazi Germany.', 2005, 2, 2),
       ('Moby-Dick', 'Herman Melville', '978-1503280786', 'Adventure Fiction',
        'A sailor recounts the obsessive quest of Ahab for revenge on the white whale.', 1851, 2, 2),
       ('Jane Eyre', 'Charlotte Brontë', '978-0141441146', 'Gothic Romance',
        'An orphaned girl’s growth to adulthood and love for Mr. Rochester.', 1847, 2, 2),
       ('The Road', 'Cormac McCarthy', '978-0307387899', 'Post-Apocalyptic Fiction',
        'A father and son journey through a devastated American landscape.', 2006, 2, 2),
       ('Life of Pi', 'Yann Martel', '978-0156027328', 'Adventure Fiction',
        'A boy survives a shipwreck and shares a lifeboat with a Bengal tiger.', 2001, 2, 2),
       ('Crime and Punishment', 'Fyodor Dostoevsky', '978-0486415871', 'Psychological Fiction',
        'A young man commits a crime and grapples with guilt and redemption.', 1866, 2, 2),
       ('Wuthering Heights', 'Emily Brontë', '978-0141439556', 'Gothic Fiction',
        'A tragic tale of love and revenge set on the Yorkshire moors.', 1847, 2, 2),
       ('The Picture of Dorian Gray', 'Oscar Wilde', '978-0141439570', 'Philosophical Fiction',
        'A man remains young while his portrait ages, reflecting his moral decay.', 1890, 2, 2),
       ('Dracula', 'Bram Stoker', '978-0486411095', 'Gothic Horror',
        'The legendary vampire Count Dracula attempts to move from Transylvania to England.', 1897, 2, 2),
       ('The Brothers Karamazov', 'Fyodor Dostoevsky', '978-0374528379', 'Philosophical Fiction',
        'A profound exploration of faith, doubt, and reason in a Russian family.', 1880, 2, 2),
       ('A Tale of Two Cities', 'Charles Dickens', '978-0141439600', 'Historical Fiction',
        'A story of love and sacrifice during the French Revolution.', 1859, 2, 2),
       ('Slaughterhouse-Five', 'Kurt Vonnegut', '978-0385333849', 'Science Fiction',
        'A satirical novel about the bombing of Dresden during WWII and time travel.', 1969, 2, 2),
       ('The Kite Runner', 'Khaled Hosseini', '978-1594631931', 'Contemporary Fiction',
        'A story of friendship and redemption set in Afghanistan.', 2003, 2, 2),
       ('One Hundred Years of Solitude', 'Gabriel García Márquez', '978-0060883287', 'Magical Realism',
        'The multi-generational story of the Buendía family in the town of Macondo.', 1967, 2, 2),
       ('Don Quixote', 'Miguel de Cervantes', '978-0060934347', 'Classic Literature',
        'A delusional knight sets out on a comical quest for chivalry and adventure.', 1605, 2, 2),
       ('The Stranger', 'Albert Camus', '978-0679720201', 'Existential Fiction',
        'A detached man commits a senseless murder and reflects on life’s absurdity.', 1942, 2, 2),
       ('Les Misérables', 'Victor Hugo', '978-0451419439', 'Historical Fiction',
        'An ex-convict struggles for redemption in 19th-century France.', 1862, 2, 2),
       ('The Old Man and the Sea', 'Ernest Hemingway', '978-0684801223', 'Literary Fiction',
        'An aging fisherman battles a giant marlin in the Gulf Stream.', 1952, 2, 2);

-- Insert book copies
INSERT INTO book_copies (book_id, inventory_number, status)
VALUES
-- 1984 copies
(1, 'INV-001-001', 'AVAILABLE'),
(1, 'INV-001-002', 'AVAILABLE'),
(1, 'INV-001-003', 'AVAILABLE'),
-- To Kill a Mockingbird copies
(2, 'INV-002-001', 'AVAILABLE'),
(2, 'INV-002-002', 'AVAILABLE'),
-- The Great Gatsby copies
(3, 'INV-003-001', 'AVAILABLE'),
(3, 'INV-003-002', 'AVAILABLE'),
-- Pride and Prejudice copies
(4, 'INV-004-001', 'AVAILABLE'),
(4, 'INV-004-002', 'AVAILABLE'),
-- The Catcher in the Rye copy
(5, 'INV-005-001', 'AVAILABLE'),
-- Brave New World copies
(6, 'INV-006-001', 'AVAILABLE'),
(6, 'INV-006-002', 'AVAILABLE'),
-- Animal Farm copies
(7, 'INV-007-001', 'AVAILABLE'),
(7, 'INV-007-002', 'AVAILABLE'),
-- The Lord of the Rings copies
(8, 'INV-008-001', 'AVAILABLE'),
(8, 'INV-008-002', 'AVAILABLE'),
(8, 'INV-008-003', 'AVAILABLE'),
-- Harry Potter copies
(9, 'INV-009-001', 'AVAILABLE'),
(9, 'INV-009-002', 'AVAILABLE'),
(9, 'INV-009-003', 'AVAILABLE'),
(9, 'INV-009-004', 'AVAILABLE'),
-- The Hobbit copies
(10, 'INV-010-001', 'AVAILABLE'),
(10, 'INV-010-002', 'AVAILABLE'),
-- Fahrenheit 451 copies (book_id = 11)
(11, 'INV-011-001', 'AVAILABLE'),
(11, 'INV-011-002', 'AVAILABLE'),
(11, 'INV-011-003', 'AVAILABLE'),
-- The Alchemist copies (book_id = 12)
(12, 'INV-012-001', 'AVAILABLE'),
(12, 'INV-012-002', 'AVAILABLE'),
-- The Book Thief copies (book_id = 13)
(13, 'INV-013-001', 'AVAILABLE'),
(13, 'INV-013-002', 'AVAILABLE'),
-- Moby-Dick copies (book_id = 14)
(14, 'INV-014-001', 'AVAILABLE'),
(14, 'INV-014-002', 'AVAILABLE'),
-- Jane Eyre copies (book_id = 15)
(15, 'INV-015-001', 'AVAILABLE'),
(15, 'INV-015-002', 'AVAILABLE'),
-- The Road copies (book_id = 16)
(16, 'INV-016-001', 'AVAILABLE'),
(16, 'INV-016-002', 'AVAILABLE'),
-- Life of Pi copies (book_id = 17)
(17, 'INV-017-001', 'AVAILABLE'),
(17, 'INV-017-002', 'AVAILABLE'),
-- Crime and Punishment copies (book_id = 18)
(18, 'INV-018-001', 'AVAILABLE'),
(18, 'INV-018-002', 'AVAILABLE'),
-- Wuthering Heights copies (book_id = 19)
(19, 'INV-019-001', 'AVAILABLE'),
(19, 'INV-019-002', 'AVAILABLE'),
-- The Picture of Dorian Gray copies (book_id = 20)
(20, 'INV-020-001', 'AVAILABLE'),
(20, 'INV-020-002', 'AVAILABLE'),
-- Dracula copies (book_id = 21)
(21, 'INV-021-001', 'AVAILABLE'),
(21, 'INV-021-002', 'AVAILABLE'),
-- The Brothers Karamazov copies (book_id = 22)
(22, 'INV-022-001', 'AVAILABLE'),
(22, 'INV-022-002', 'AVAILABLE'),
-- A Tale of Two Cities copies (book_id = 23)
(23, 'INV-023-001', 'AVAILABLE'),
(23, 'INV-023-002', 'AVAILABLE'),
-- Slaughterhouse-Five copies (book_id = 24)
(24, 'INV-024-001', 'AVAILABLE'),
(24, 'INV-024-002', 'AVAILABLE'),
-- The Kite Runner copies (book_id = 25)
(25, 'INV-025-001', 'AVAILABLE'),
(25, 'INV-025-002', 'AVAILABLE'),
-- One Hundred Years of Solitude copies (book_id = 26)
(26, 'INV-026-001', 'AVAILABLE'),
(26, 'INV-026-002', 'AVAILABLE'),
-- Don Quixote copies (book_id = 27)
(27, 'INV-027-001', 'AVAILABLE'),
(27, 'INV-027-002', 'AVAILABLE'),
-- The Stranger copies (book_id = 28)
(28, 'INV-028-001', 'AVAILABLE'),
(28, 'INV-028-002', 'AVAILABLE'),
-- Les Misérables copies (book_id = 29)
(29, 'INV-029-001', 'AVAILABLE'),
(29, 'INV-029-002', 'AVAILABLE'),
-- The Old Man and the Sea copies (book_id = 30)
(30, 'INV-030-001', 'AVAILABLE'),
(30, 'INV-030-002', 'AVAILABLE');

-- Insert sample orders
INSERT INTO book_orders (user_id, book_id, book_copy_id, order_type, status, issue_date, due_date, librarian_id)
VALUES (3, 1, 1, 'HOME', 'ISSUED', NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), 2),
       (4, 2, 4, 'READING_ROOM', 'ISSUED', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 2);

-- Update book copy status for issued books
UPDATE book_copies
SET status = 'ISSUED'
WHERE id IN (1, 4);

-- Update available copies count
UPDATE books
SET available_copies = available_copies - 1
WHERE id IN (1, 2);