package com.diego.library.purchase.service;

import com.diego.library.book.dto.BookResponse;
import com.diego.library.book.entity.Book;
import com.diego.library.book.repository.BookRepository;
import com.diego.library.book.service.BookService;
import com.diego.library.purchase.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private WebClient receiptsWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private PurchaseService purchaseService;

    private PurchaseRequest request;
    private PqrDto pqrDto;
    private BookResponse bookResponse;
    private ReceiptResponse receiptResponse;

    @BeforeEach
    void setUp() {
        pqrDto = new PqrDto(1L, "Clean Code", "Juan Pérez", 5);

        request = new PurchaseRequest(
                "Clean Code",
                "Robert C. Martin",
                "978-0132350884",
                pqrDto
        );

        bookResponse = new BookResponse();
        bookResponse.setId(1L);
        bookResponse.setTitle("Clean Code");
        bookResponse.setAuthor("Robert C. Martin");
        bookResponse.setIsbn("978-0132350884");
        bookResponse.setAvailable(true);
        bookResponse.setCreatedAt(LocalDateTime.now());

        receiptResponse = new ReceiptResponse(
                1L,
                "Biblioteca Central",
                "900.123.456-1",
                "Clean Code - Robert C. Martin",
                85000L,
                "2026-04-03",
                "https://storage.googleapis.com/receipts/factura-1.pdf"
        );
    }

    @SuppressWarnings("unchecked")
    private void mockWebClient() {
        when(receiptsWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ReceiptResponse.class))
                .thenReturn(Mono.just(receiptResponse));
    }

    @Test
    void processPurchase_newBook_success() {
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(false);
        when(bookService.create(any())).thenReturn(bookResponse);
        mockWebClient();

        PurchaseResponse response = purchaseService.processPurchase(request);

        assertNotNull(response);
        assertEquals("Clean Code", response.libro().getTitle());
        assertEquals("Clean Code", response.pqr().asunto());
        assertNotNull(response.receipt());
        assertEquals("https://storage.googleapis.com/receipts/factura-1.pdf", response.pdf_url());
    }

    @Test
    void processPurchase_existingBook_success() {
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setTitle("Clean Code");
        existingBook.setAuthor("Robert C. Martin");
        existingBook.setIsbn("978-0132350884");
        existingBook.setAvailable(true);

        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(true);
        when(bookRepository.findByIsbn("978-0132350884")).thenReturn(Optional.of(existingBook));
        mockWebClient();

        PurchaseResponse response = purchaseService.processPurchase(request);

        assertNotNull(response);
        assertEquals("Clean Code", response.libro().getTitle());
        verify(bookService, never()).create(any());
    }

    @Test
    void processPurchase_noIsbn_generatesIsbn() {
        PurchaseRequest requestNoIsbn = new PurchaseRequest(
                "Clean Code",
                "Robert C. Martin",
                null,
                pqrDto
        );

        when(bookService.create(any())).thenReturn(bookResponse);
        mockWebClient();

        PurchaseResponse response = purchaseService.processPurchase(requestNoIsbn);

        assertNotNull(response);
    }
}