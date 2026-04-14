package com.diego.library.purchase.service;

import com.diego.library.book.dto.BookRequest;
import com.diego.library.book.dto.BookResponse;
import com.diego.library.book.repository.BookRepository;
import com.diego.library.book.service.BookService;
import com.diego.library.purchase.dto.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PurchaseService {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final SipScanClient sipScanClient;

    public PurchaseService(BookService bookService,
                           BookRepository bookRepository,
                           SipScanClient sipScanClient) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.sipScanClient = sipScanClient;
    }

    public PurchaseResponse processPurchase(PurchaseRequest request) {

        // 1. Registrar libro en BD
        BookResponse savedBook = getOrCreateBook(request);

        // 2. Login al ERP de Daniel
        String token = sipScanClient.login();

        // 3. Construir texto de factura
        String facturaText = buildFacturaText(savedBook, request);

        // 4. Enviar texto y obtener receipt_id
        String receiptId = sipScanClient.sendReceiptText(token, facturaText);

        // 5. Polling hasta que el PDF esté listo (máx 30 segundos)
        String pdfUrl = waitForPdf(token, receiptId);

        // 6. Construir respuesta enriquecida
        ReceiptResponse receipt = new ReceiptResponse(
                receiptId,
                "Biblioteca Central",
                "901000123",
                savedBook.getTitle() + " - " + savedBook.getAuthor(),
                85000L,
                LocalDate.now().toString(),
                pdfUrl
        );

        return new PurchaseResponse(request.pqr(), savedBook, receipt, pdfUrl);
    }

    private BookResponse getOrCreateBook(PurchaseRequest request) {
        if (request.isbn() != null && bookRepository.existsByIsbn(request.isbn())) {
            return bookRepository.findByIsbn(request.isbn())
                    .map(b -> {
                        BookResponse r = new BookResponse();
                        r.setId(b.getId());
                        r.setTitle(b.getTitle());
                        r.setAuthor(b.getAuthor());
                        r.setIsbn(b.getIsbn());
                        r.setAvailable(b.isAvailable());
                        r.setCreatedAt(b.getCreatedAt());
                        return r;
                    }).orElseThrow();
        }
        BookRequest bookRequest = new BookRequest();
        bookRequest.setTitle(request.titulo_libro());
        bookRequest.setAuthor(request.autor());
        bookRequest.setIsbn(request.isbn() != null ? request.isbn()
                : "GEN-" + request.titulo_libro().replaceAll("\\s+", "-").toUpperCase()
                + "-" + System.currentTimeMillis());
        return bookService.create(bookRequest);
    }

    private String waitForPdf(String token, String receiptId) {
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (sipScanClient.isPdfReady(token, receiptId)) {
                break;
            }
        }
        return "http://3.12.170.176:8000/v2/receipts/" + receiptId + "/pdf";
    }

    private String buildFacturaText(BookResponse book, PurchaseRequest request) {
        return "Biblioteca Central\n" +
                "NIT: 900.123.456-1\n" +
                "Orden de Compra\n" +
                "Fecha: " + LocalDate.now() + "\n" +
                "Libro: " + book.getTitle() + "\n" +
                "Autor: " + book.getAuthor() + "\n" +
                "ISBN: " + book.getIsbn() + "\n" +
                "Valor: $85.000\n" +
                "Solicitado por PQR: " + request.pqr().asunto() + "\n" +
                "Responsable: " + request.pqr().responsable() + "\n" +
                "Cantidad de solicitudes: " + request.pqr().conteo();
    }
}