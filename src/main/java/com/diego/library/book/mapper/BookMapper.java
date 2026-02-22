package com.diego.library.book.mapper;

import com.diego.library.book.dto.BookRequest;
import com.diego.library.book.dto.BookResponse;
import com.diego.library.book.entity.Book;

public class BookMapper {

    public static Book toEntity(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        return book;
    }

    public static BookResponse toResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setIsbn(book.getIsbn());
        response.setAvailable(book.isAvailable());
        response.setCreatedAt(book.getCreatedAt());
        return response;
    }
}