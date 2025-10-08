package com.khazhimetov.library.dao;

import com.khazhimetov.library.dao.impl.BookDaoImpl;
import com.khazhimetov.library.exception.DatabaseException;
import com.khazhimetov.library.model.Book;
import com.khazhimetov.library.model.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookDaoImplTest {

    @Mock
    private ConnectionPool connectionPool;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private BookDaoImpl bookDao;
    private MockedStatic<ConnectionPool> mockedConnectionPool;

    @BeforeEach
    void setUp() {
        // Keep the MockedStatic open for the entire test
        mockedConnectionPool = mockStatic(ConnectionPool.class);
        mockedConnectionPool.when(ConnectionPool::getInstance).thenReturn(connectionPool);
        bookDao = new BookDaoImpl();
    }

    @AfterEach
    void tearDown() {
        // Close the MockedStatic after each test
        if (mockedConnectionPool != null) {
            mockedConnectionPool.close();
        }
    }

    @Test
    void findById_ReturnsBook_WhenBookExists() throws SQLException {
        // Given
        Long bookId = 1L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        mockBookResultSet(bookId);

        // When
        Optional<Book> result = bookDao.findById(bookId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(bookId, result.get().getId());
        assertEquals("Test Book", result.get().getTitle());
        assertEquals("Test Author", result.get().getAuthor());

        verify(preparedStatement).setLong(1, bookId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findById_ReturnsEmpty_WhenBookNotFound() throws SQLException {
        // Given
        Long bookId = 999L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        Optional<Book> result = bookDao.findById(bookId);

        // Then
        assertFalse(result.isPresent());
        verify(preparedStatement).setLong(1, bookId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findAllPaginated_ReturnsPageOfBooks() throws SQLException {
        // Given
        int pageNumber = 0;
        int pageSize = 10;
        long totalElements = 25L;

        when(connectionPool.getConnection()).thenReturn(connection);

        // Mock count query
        PreparedStatement countStmt = mock(PreparedStatement.class);
        ResultSet countRs = mock(ResultSet.class);
        when(connection.prepareStatement(contains("COUNT"))).thenReturn(countStmt);
        when(countStmt.executeQuery()).thenReturn(countRs);
        when(countRs.next()).thenReturn(true);
        when(countRs.getLong(1)).thenReturn(totalElements);

        // Mock page query
        when(connection.prepareStatement(contains("LIMIT"))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // 2 books

        mockBookResultSet(1L);

        // When
        Page<Book> result = bookDao.findAllPaginated(pageNumber, pageSize);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(pageNumber, result.getPageNumber());
        assertEquals(pageSize, result.getPageSize());
        assertEquals(totalElements, result.getTotalElements());
        assertEquals(3, result.getTotalPages()); // 25 / 10 = 3 pages

        verify(preparedStatement).setInt(1, pageSize);
        verify(preparedStatement).setInt(2, pageNumber * pageSize);
        verify(connectionPool, times(2)).releaseConnection(connection);
    }

    @Test
    void search_ReturnsMatchingBooks() throws SQLException {
        // Given
        String searchTerm = "Java";
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // 2 results

        mockBookResultSet(1L);

        // When
        List<Book> result = bookDao.search(searchTerm);

        // Then
        assertEquals(2, result.size());

        String expectedPattern = "%Java%";
        verify(preparedStatement, times(4)).setString(anyInt(), eq(expectedPattern));
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void searchPaginated_ReturnsPageOfMatchingBooks() throws SQLException {
        // Given
        String searchTerm = "Programming";
        int pageNumber = 0;
        int pageSize = 5;
        long totalElements = 12L;

        when(connectionPool.getConnection()).thenReturn(connection);

        // Mock count query
        PreparedStatement countStmt = mock(PreparedStatement.class);
        ResultSet countRs = mock(ResultSet.class);
        when(connection.prepareStatement(argThat(sql ->
                sql != null && sql.contains("COUNT") && sql.contains("LIKE"))))
                .thenReturn(countStmt);
        when(countStmt.executeQuery()).thenReturn(countRs);
        when(countRs.next()).thenReturn(true);
        when(countRs.getLong(1)).thenReturn(totalElements);

        // Mock page query
        when(connection.prepareStatement(argThat(sql ->
                sql != null && sql.contains("LIMIT") && sql.contains("LIKE"))))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        mockBookResultSet(1L);

        // When
        Page<Book> result = bookDao.searchPaginated(searchTerm, pageNumber, pageSize);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(totalElements, result.getTotalElements());

        String expectedPattern = "%Programming%";
        verify(preparedStatement, times(4)).setString(anyInt(), eq(expectedPattern));
        verify(preparedStatement).setInt(5, pageSize);
        verify(preparedStatement).setInt(6, pageNumber * pageSize);
    }

    @Test
    void save_CreatesNewBook_WithTransaction() throws SQLException {
        // Given
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("New Author");
        newBook.setIsbn("978-1234567890");
        newBook.setGenre("Fiction");
        newBook.setDescription("Test description");
        newBook.setPublicationYear(2024);
        newBook.setTotalCopies(5);
        newBook.setAvailableCopies(5);

        ResultSet generatedKeys = mock(ResultSet.class);

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getLong(1)).thenReturn(10L);

        // When
        Book savedBook = bookDao.save(newBook);

        // Then
        assertEquals(10L, savedBook.getId());
        assertEquals("New Book", savedBook.getTitle());

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(preparedStatement).setString(1, newBook.getTitle());
        verify(preparedStatement).setString(2, newBook.getAuthor());
        verify(preparedStatement).setString(3, newBook.getIsbn());
        verify(preparedStatement).setString(4, newBook.getGenre());
        verify(preparedStatement).setString(5, newBook.getDescription());
        verify(preparedStatement).setObject(6, newBook.getPublicationYear());
        verify(preparedStatement).setInt(7, newBook.getTotalCopies());
        verify(preparedStatement).setInt(8, newBook.getAvailableCopies());

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void save_RollsBackTransaction_OnError() throws SQLException {
        // Given
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("New Author");
        newBook.setTotalCopies(1); // Set required fields
        newBook.setAvailableCopies(1);

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException("DB Error"));

        // When & Then
        assertThrows(DatabaseException.class, () -> bookDao.save(newBook));
        verify(connection).rollback();
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void update_UpdatesExistingBook() throws SQLException {
        // Given
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Updated Title");
        book.setAuthor("Updated Author");
        book.setIsbn("978-0987654321");
        book.setGenre("Science Fiction");
        book.setDescription("Updated description");
        book.setPublicationYear(2025);
        book.setTotalCopies(10);
        book.setAvailableCopies(8);

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        Book updatedBook = bookDao.update(book);

        // Then
        assertEquals(book.getId(), updatedBook.getId());
        assertEquals(book.getTitle(), updatedBook.getTitle());

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(preparedStatement).setString(1, book.getTitle());
        verify(preparedStatement).setString(2, book.getAuthor());
        verify(preparedStatement).setLong(9, book.getId());

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void update_ThrowsDatabaseException_WhenNoRowsAffected() throws SQLException {
        // Given
        Book book = new Book();
        book.setId(999L);
        book.setTitle("Non-existent Book");
        book.setAuthor("Unknown Author");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When & Then
        DatabaseException exception = assertThrows(DatabaseException.class, () -> bookDao.update(book));
        assertTrue(exception.getMessage().contains("Updating book failed"));

        verify(connection).setAutoCommit(false);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void updateAvailableCopies_UpdatesCopiesCount() throws SQLException {
        // Given
        Long bookId = 1L;
        int delta = -1; // Decrease by 1

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        bookDao.updateAvailableCopies(bookId, delta);

        // Then
        verify(preparedStatement).setInt(1, delta);
        verify(preparedStatement).setLong(2, bookId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void updateAvailableCopies_ThrowsDatabaseException_WhenNoRowsAffected() throws SQLException {
        // Given
        Long bookId = 999L;
        int delta = 1;

        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When & Then
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> bookDao.updateAvailableCopies(bookId, delta));
        assertTrue(exception.getMessage().contains("Updating available copies failed"));

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void deleteById_ReturnsTrue_WhenBookDeleted() throws SQLException {
        // Given
        Long bookId = 1L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        boolean result = bookDao.deleteById(bookId);

        // Then
        assertTrue(result);
        verify(preparedStatement).setLong(1, bookId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void deleteById_ReturnsFalse_WhenBookNotFound() throws SQLException {
        // Given
        Long bookId = 999L;
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // When
        boolean result = bookDao.deleteById(bookId);

        // Then
        assertFalse(result);
        verify(preparedStatement).setLong(1, bookId);
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findByGenrePaginated_ReturnsPageOfBooksInGenre() throws SQLException {
        // Given
        String genre = "Fiction";
        int pageNumber = 0;
        int pageSize = 10;
        long totalElements = 15L;

        when(connectionPool.getConnection()).thenReturn(connection);

        // Mock count query
        PreparedStatement countStmt = mock(PreparedStatement.class);
        ResultSet countRs = mock(ResultSet.class);
        when(connection.prepareStatement(argThat(sql ->
                sql != null && sql.contains("COUNT") && sql.contains("genre"))))
                .thenReturn(countStmt);
        when(countStmt.executeQuery()).thenReturn(countRs);
        when(countRs.next()).thenReturn(true);
        when(countRs.getLong(1)).thenReturn(totalElements);

        // Mock page query
        when(connection.prepareStatement(argThat(sql ->
                sql != null && sql.contains("LIMIT") && sql.contains("genre"))))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        mockBookResultSet(1L);

        // When
        Page<Book> result = bookDao.findByGenrePaginated(genre, pageNumber, pageSize);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(totalElements, result.getTotalElements());

        verify(preparedStatement).setString(1, genre);
        verify(preparedStatement).setInt(2, pageSize);
        verify(preparedStatement).setInt(3, pageNumber * pageSize);
    }

    @Test
    void findAll_ReturnsAllBooks() throws SQLException {
        // Given
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false); // 3 books

        mockBookResultSet(1L);

        // When
        List<Book> result = bookDao.findAll();

        // Then
        assertEquals(3, result.size());
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    void findByGenre_ReturnsBooksInGenre() throws SQLException {
        // Given
        String genre = "Mystery";
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // 2 books

        mockBookResultSet(1L);

        // When
        List<Book> result = bookDao.findByGenre(genre);

        // Then
        assertEquals(2, result.size());
        verify(preparedStatement).setString(1, genre);
        verify(connectionPool).releaseConnection(connection);
    }

    private void mockBookResultSet(Long bookId) throws SQLException {
        when(resultSet.getLong("id")).thenReturn(bookId);
        when(resultSet.getString("title")).thenReturn("Test Book");
        when(resultSet.getString("author")).thenReturn("Test Author");
        when(resultSet.getString("isbn")).thenReturn("978-1234567890");
        when(resultSet.getString("genre")).thenReturn("Fiction");
        when(resultSet.getString("description")).thenReturn("Test description");
        when(resultSet.getObject("publication_year", Integer.class)).thenReturn(2024);
        when(resultSet.getInt("total_copies")).thenReturn(5);
        when(resultSet.getInt("available_copies")).thenReturn(3);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
    }
}