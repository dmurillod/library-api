package com.diego.library.book.service;

import com.diego.library.book.dto.BookRequest;
import com.diego.library.book.dto.BookResponse;
import com.diego.library.book.entity.Book;
import com.diego.library.book.mapper.BookMapper;
import com.diego.library.book.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public BookResponse create(BookRequest request) {
        if (repository.existsByIsbn(request.getIsbn())) {
            throw new RuntimeException("Book with this ISBN already exists");
        }

        Book book = BookMapper.toEntity(request);
        Book saved = repository.save(book);

        return BookMapper.toResponse(saved);
    }

    public BookResponse findById(Long id) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        return BookMapper.toResponse(book);
    }

    public List<BookResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(BookMapper::toResponse)
                .collect(Collectors.toList());
    }

    public BookResponse update(Long id, BookRequest request) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Validar duplicado solo si cambia el ISBN
        if (!book.getIsbn().equals(request.getIsbn())
                && repository.existsByIsbn(request.getIsbn())) {
            throw new RuntimeException("Book with this ISBN already exists");
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());

        Book updated = repository.save(book);

        return BookMapper.toResponse(updated);
    }

    public void delete(Long id) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        repository.delete(book);
    }
}
