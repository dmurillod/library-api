package com.diego.library.book.service;

import com.diego.library.book.dto.BookRequest;
import com.diego.library.book.entity.Book;
import com.diego.library.book.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookService service;

    private BookRequest buildRequest() {
        BookRequest request = new BookRequest();
        request.setTitle("Clean Code");
        request.setAuthor("Robert C. Martin");
        request.setIsbn("123456789");
        return request;
    }

    private Book buildBook() {
        Book book = new Book();
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setIsbn("123456789");
        return book;
    }

    @Test
    void create_shouldSaveBook_whenIsbnNotExists() {
        BookRequest request = buildRequest();
        Book book = buildBook();

        when(repository.existsByIsbn(request.getIsbn())).thenReturn(false);
        when(repository.save(any(Book.class))).thenReturn(book);

        var response = service.create(request);

        assertNotNull(response);
        verify(repository).save(any(Book.class));
    }

    @Test
    void create_shouldThrowException_whenIsbnExists() {
        BookRequest request = buildRequest();

        when(repository.existsByIsbn(request.getIsbn())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test
    void findById_shouldReturnBook_whenExists() {
        Book book = buildBook();

        when(repository.findById(1L)).thenReturn(Optional.of(book));

        var response = service.findById(1L);

        assertNotNull(response);
    }

    @Test
    void findById_shouldThrowException_whenNotExists() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.findById(1L));
    }

    @Test
    void findAll_shouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(buildBook()));

        var result = service.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void update_shouldUpdateBook_whenValid() {
        BookRequest request = buildRequest();
        Book book = buildBook();

        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(repository.save(any(Book.class))).thenReturn(book);

        var response = service.update(1L, request);

        assertNotNull(response);
        verify(repository).save(book);
    }

    @Test
    void update_shouldThrowException_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.update(1L, buildRequest()));
    }

    @Test
    void delete_shouldDeleteBook_whenExists() {
        Book book = buildBook();

        when(repository.findById(1L)).thenReturn(Optional.of(book));

        service.delete(1L);

        verify(repository).delete(book);
    }

    @Test
    void delete_shouldThrowException_whenNotExists() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.delete(1L));
    }
}
