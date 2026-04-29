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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock private BookService bookService;
    @Mock private BookRepository bookRepository;
    @Mock private SipScanClient sipScanClient;

    @InjectMocks
    private PurchaseService purchaseService;

    private PurchaseRequest request;
    private PqrDto pqrDto;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        pqrDto = new PqrDto("1", "Clean Code", "Juan Pérez", 5);
        request = new PurchaseRequest("Clean Code", "Robert C. Martin", "978-0132350884", pqrDto);

        bookResponse = new BookResponse();
        bookResponse.setId(1L);
        bookResponse.setTitle("Clean Code");
        bookResponse.setAuthor("Robert C. Martin");
        bookResponse.setIsbn("978-0132350884");
        bookResponse.setAvailable(true);
        bookResponse.setCreatedAt(LocalDateTime.now());
    }

    private void mockSipScan() {
        when(sipScanClient.login()).thenReturn("fake-jwt-token");
        when(sipScanClient.sendReceiptText(anyString(), anyString()))
                .thenReturn("550e8400-e29b-41d4-a716-446655440000");
        when(sipScanClient.waitForReceiptViaWebSocket(anyString(), anyString()))
                .thenReturn("http://3.12.170.176:8000/v2/receipts/550e8400-e29b-41d4-a716-446655440000/pdf");
    }

    @Test
    void processPurchase_newBook_success() {
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(false);
        when(bookService.create(any())).thenReturn(bookResponse);
        mockSipScan();

        PurchaseResponse response = purchaseService.processPurchase(request);

        assertNotNull(response);
        assertEquals("Clean Code", response.libro().getTitle());
        assertEquals("Clean Code", response.pqr().asunto());
        assertNotNull(response.receipt());
        assertNotNull(response.pdf_url());
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
        mockSipScan();

        PurchaseResponse response = purchaseService.processPurchase(request);

        assertNotNull(response);
        assertEquals("Clean Code", response.libro().getTitle());
        verify(bookService, never()).create(any());
    }

    @Test
    void processPurchase_noIsbn_generatesIsbn() {
        PurchaseRequest requestNoIsbn = new PurchaseRequest(
                "Clean Code", "Robert C. Martin", null, pqrDto
        );

        when(bookService.create(any())).thenReturn(bookResponse);
        mockSipScan();

        PurchaseResponse response = purchaseService.processPurchase(requestNoIsbn);

        assertNotNull(response);
    }

    @Test
    void processPurchase_receiptHasPdfUrl() {
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(false);
        when(bookService.create(any())).thenReturn(bookResponse);
        mockSipScan();

        PurchaseResponse response = purchaseService.processPurchase(request);

        assertNotNull(response.pdf_url());
        assertTrue(response.pdf_url().contains("receipts"));
        assertEquals("Biblioteca Central", response.receipt().empresa());
        assertEquals("901000123", response.receipt().nit());
    }

    @Test
    void processPurchase_pqrDataIsPreserved() {
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(false);
        when(bookService.create(any())).thenReturn(bookResponse);
        mockSipScan();

        PurchaseResponse response = purchaseService.processPurchase(request);

        assertEquals("1", response.pqr().id());
        assertEquals("Clean Code", response.pqr().asunto());
        assertEquals("Juan Pérez", response.pqr().responsable());
        assertEquals(5, response.pqr().conteo());
    }

    @Test
    void processPurchase_receiptContainsBookInfo() {
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(false);
        when(bookService.create(any())).thenReturn(bookResponse);
        mockSipScan();

        PurchaseResponse response = purchaseService.processPurchase(request);

        assertTrue(response.receipt().item().contains("Clean Code"));
        assertTrue(response.receipt().item().contains("Robert C. Martin"));
        assertEquals(85000L, response.receipt().valor());
    }

    @Test
    void processPurchase_existingBook_receiptHasCorrectData() {
        Book existingBook = new Book();
        existingBook.setId(2L);
        existingBook.setTitle("Clean Code");
        existingBook.setAuthor("Robert C. Martin");
        existingBook.setIsbn("978-0132350884");
        existingBook.setAvailable(true);

        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(true);
        when(bookRepository.findByIsbn("978-0132350884")).thenReturn(Optional.of(existingBook));
        mockSipScan();

        PurchaseResponse response = purchaseService.processPurchase(request);

        assertNotNull(response.pdf_url());
        assertNotNull(response.receipt());
        assertEquals("Clean Code", response.pqr().asunto());
        assertEquals(2L, response.libro().getId());
    }
}