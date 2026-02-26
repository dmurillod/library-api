package com.diego.library.book.controller;

import com.diego.library.book.dto.BookRequest;
import com.diego.library.book.dto.BookResponse;
import com.diego.library.book.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    private MockMvc mockMvc;

    private BookRequest validRequest;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        validRequest = new BookRequest();
        validRequest.setTitle("Clean Code");
        validRequest.setAuthor("Robert C. Martin");
        validRequest.setIsbn("9780132350884");

        bookResponse = new BookResponse();
        bookResponse.setId(1L);
        bookResponse.setTitle("Clean Code");
        bookResponse.setAuthor("Robert C. Martin");
        bookResponse.setIsbn("9780132350884");
        bookResponse.setAvailable(true);
        bookResponse.setCreatedAt(LocalDateTime.now());
    }

    // ──────────────────────────────────────────
    // POST /api/books
    // ──────────────────────────────────────────

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        when(bookService.create(any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"))
                .andExpect(jsonPath("$.available").value(true));

        verify(bookService).create(any(BookRequest.class));
    }

    @Test
    void create_shouldReturn400_whenTitleIsBlank() throws Exception {
        validRequest.setTitle("");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void create_shouldReturn400_whenAuthorIsBlank() throws Exception {
        validRequest.setAuthor("");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void create_shouldReturn400_whenIsbnIsBlank() throws Exception {
        validRequest.setIsbn("");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void create_shouldReturn400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    // ──────────────────────────────────────────
    // GET /api/books/{id}
    // ──────────────────────────────────────────

    @Test
    void findById_shouldReturn200_whenBookExists() throws Exception {
        when(bookService.findById(1L)).thenReturn(bookResponse);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.isbn").value("9780132350884"));

        verify(bookService).findById(1L);
    }

    @Test
    void findById_shouldReturn404_whenBookNotFound() throws Exception {
        when(bookService.findById(99L)).thenThrow(new RuntimeException("Book not found"));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────
    // GET /api/books
    // ──────────────────────────────────────────

    @Test
    void findAll_shouldReturn200_withListOfBooks() throws Exception {
        when(bookService.findAll()).thenReturn(List.of(bookResponse));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));

        verify(bookService).findAll();
    }

    @Test
    void findAll_shouldReturn200_withEmptyList() throws Exception {
        when(bookService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ──────────────────────────────────────────
    // PUT /api/books/{id}
    // ──────────────────────────────────────────

    @Test
    void update_shouldReturn200_whenRequestIsValid() throws Exception {
        when(bookService.update(eq(1L), any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Clean Code"));

        verify(bookService).update(eq(1L), any(BookRequest.class));
    }

    @Test
    void update_shouldReturn400_whenTitleIsBlank() throws Exception {
        validRequest.setTitle("");

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void update_shouldReturn404_whenBookNotFound() throws Exception {
        when(bookService.update(eq(99L), any(BookRequest.class)))
                .thenThrow(new RuntimeException("Book not found"));

        mockMvc.perform(put("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────
    // DELETE /api/books/{id}
    // ──────────────────────────────────────────

    @Test
    void delete_shouldReturn200_whenBookExists() throws Exception {
        doNothing().when(bookService).delete(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).delete(1L);
    }

    @Test
    void delete_shouldReturn204_whenBookExists() throws Exception {
        doNothing().when(bookService).delete(1L);
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
        verify(bookService).delete(1L);
    }
}
