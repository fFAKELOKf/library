package com.khazhimetov.library.service;

import com.khazhimetov.library.dao.BookDao;
import com.khazhimetov.library.exception.ServiceException;
import com.khazhimetov.library.model.Book;
import com.khazhimetov.library.model.Page;
import com.khazhimetov.library.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookDao bookDao;

    private BookServiceImpl bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl(bookDao);
    }

    @Test
    void findById_ReturnsBook_WhenExists() {
        // Given
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Test Book");

        when(bookDao.findById(bookId)).thenReturn(Optional.of(book));

        // When
        Optional<Book> result = bookService.findById(bookId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(bookId, result.get().getId());
        assertEquals("Test Book", result.get().getTitle());
        verify(bookDao).findById(bookId);
    }

    @Test
    void findAll_ReturnsAllBooks() {
        // Given
        Book book1 = new Book();
        book1.setId(1L);
        Book book2 = new Book();
        book2.setId(2L);

        List<Book> books = Arrays.asList(book1, book2);
        when(bookDao.findAll()).thenReturn(books);

        // When
        List<Book> result = bookService.findAll();

        // Then
        assertEquals(2, result.size());
        verify(bookDao).findAll();
    }

    @Test
    void findAllPaginated_ReturnsPageOfBooks() {
        // Given
        int pageNumber = 1;
        int pageSize = 10;
        List<Book> books = Arrays.asList(new Book(), new Book());
        Page<Book> page = new Page<>(books, pageNumber, pageSize, 25L);

        when(bookDao.findAllPaginated(pageNumber, pageSize)).thenReturn(page);

        // When
        Page<Book> result = bookService.findAllPaginated(pageNumber, pageSize);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(pageNumber, result.getPageNumber());
        assertEquals(pageSize, result.getPageSize());
        assertEquals(25L, result.getTotalElements());
        verify(bookDao).findAllPaginated(pageNumber, pageSize);
    }

    @Test
    void findAllPaginated_HandlesInvalidPageParams() {
        // Given
        int negativePageNumber = -1;
        int zeroPageSize = 0;
        List<Book> books = Collections.emptyList();
        Page<Book> page = new Page<>(books, 0, 12, 0L);

        when(bookDao.findAllPaginated(0, 12)).thenReturn(page);

        // When
        Page<Book> result = bookService.findAllPaginated(negativePageNumber, zeroPageSize);

        // Then
        verify(bookDao).findAllPaginated(0, 12); // Should use defaults
    }

    @Test
    void search_ReturnsMatchingBooks() {
        // Given
        String searchTerm = "Java";
        Book book1 = new Book();
        book1.setTitle("Java Programming");
        Book book2 = new Book();
        book2.setTitle("Java Basics");

        List<Book> books = Arrays.asList(book1, book2);
        when(bookDao.search(searchTerm)).thenReturn(books);

        // When
        List<Book> result = bookService.search(searchTerm);

        // Then
        assertEquals(2, result.size());
        verify(bookDao).search(searchTerm);
    }

    @Test
    void search_ReturnsAllBooks_WhenSearchTermEmpty() {
        // Given
        String emptySearchTerm = "   ";
        List<Book> allBooks = Arrays.asList(new Book(), new Book(), new Book());
        when(bookDao.findAll()).thenReturn(allBooks);

        // When
        List<Book> result = bookService.search(emptySearchTerm);

        // Then
        assertEquals(3, result.size());
        verify(bookDao).findAll();
        verify(bookDao, never()).search(anyString());
    }

    @Test
    void searchPaginated_ReturnsPageOfMatchingBooks() {
        // Given
        String searchTerm = "Programming";
        int pageNumber = 0;
        int pageSize = 5;
        List<Book> books = Arrays.asList(new Book());
        Page<Book> page = new Page<>(books, pageNumber, pageSize, 10L);

        when(bookDao.searchPaginated(searchTerm, pageNumber, pageSize)).thenReturn(page);

        // When
        Page<Book> result = bookService.searchPaginated(searchTerm, pageNumber, pageSize);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(10L, result.getTotalElements());
        verify(bookDao).searchPaginated(searchTerm, pageNumber, pageSize);
    }

    @Test
    void searchPaginated_ReturnsAllPaginated_WhenSearchTermEmpty() {
        // Given
        String emptySearchTerm = "";
        int pageNumber = 0;
        int pageSize = 10;
        Page<Book> page = new Page<>(Collections.emptyList(), pageNumber, pageSize, 0L);

        when(bookDao.findAllPaginated(pageNumber, pageSize)).thenReturn(page);

        // When
        Page<Book> result = bookService.searchPaginated(emptySearchTerm, pageNumber, pageSize);

        // Then
        verify(bookDao).findAllPaginated(pageNumber, pageSize);
        verify(bookDao, never()).searchPaginated(anyString(), anyInt(), anyInt());
    }

    @Test
    void findByGenrePaginated_ReturnsPageOfBooksInGenre() {
        // Given
        String genre = "Fiction";
        int pageNumber = 0;
        int pageSize = 10;
        List<Book> books = Arrays.asList(new Book());
        Page<Book> page = new Page<>(books, pageNumber, pageSize, 5L);

        when(bookDao.findByGenrePaginated(genre, pageNumber, pageSize)).thenReturn(page);

        // When
        Page<Book> result = bookService.findByGenrePaginated(genre, pageNumber, pageSize);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(5L, result.getTotalElements());
        verify(bookDao).findByGenrePaginated(genre, pageNumber, pageSize);
    }

    @Test
    void save_CreatesNewBook_WithDefaultValues() {
        // Given
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("Author");
        // No totalCopies or availableCopies set

        when(bookDao.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return book;
        });

        // When
        Book savedBook = bookService.save(newBook);

        // Then
        assertEquals(1L, savedBook.getId());
        verify(bookDao).save(argThat(book ->
                book.getTotalCopies() == 1 && book.getAvailableCopies() == 1));
    }

    @Test
    void save_CreatesNewBook_WithProvidedValues() {
        // Given
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("Author");
        newBook.setTotalCopies(5);
        newBook.setAvailableCopies(3);

        when(bookDao.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return book;
        });

        // When
        Book savedBook = bookService.save(newBook);

        // Then
        verify(bookDao).save(argThat(book ->
                book.getTotalCopies() == 5 && book.getAvailableCopies() == 3));
    }

    @Test
    void update_UpdatesBook_WhenExists() {
        // Given
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setTitle("Old Title");

        Book updateBook = new Book();
        updateBook.setId(1L);
        updateBook.setTitle("New Title");

        when(bookDao.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookDao.update(updateBook)).thenReturn(updateBook);

        // When
        Book result = bookService.update(updateBook);

        // Then
        assertEquals("New Title", result.getTitle());
        verify(bookDao).findById(1L);
        verify(bookDao).update(updateBook);
    }

    @Test
    void update_ThrowsException_WhenBookNotFound() {
        // Given
        Book updateBook = new Book();
        updateBook.setId(999L);

        when(bookDao.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> bookService.update(updateBook));

        assertTrue(exception.getMessage().contains("Book not found"));
        verify(bookDao, never()).update(any());
    }

    @Test
    void delete_CallsDao() {
        // Given
        Long bookId = 1L;
        when(bookDao.deleteById(bookId)).thenReturn(true);

        // When
        boolean result = bookService.delete(bookId);

        // Then
        assertTrue(result);
        verify(bookDao).deleteById(bookId);
    }

    @Test
    void delete_ReturnsFalse_WhenBookNotFound() {
        // Given
        Long bookId = 999L;
        when(bookDao.deleteById(bookId)).thenReturn(false);

        // When
        boolean result = bookService.delete(bookId);

        // Then
        assertFalse(result);
        verify(bookDao).deleteById(bookId);
    }
}