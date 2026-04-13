package com.diego.library.purchase.service;

import com.diego.library.book.dto.BookRequest;
import com.diego.library.book.dto.BookResponse;
import com.diego.library.book.repository.BookRepository;
import com.diego.library.book.service.BookService;
import com.diego.library.purchase.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Map;

@Service
public class PurchaseService {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final WebClient receiptsWebClient;

    public PurchaseService(BookService bookService,
                           BookRepository bookRepository,
                           WebClient receiptsWebClient) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.receiptsWebClient = receiptsWebClient;
    }

    public PurchaseResponse processPurchase(PurchaseRequest request) {

        // 1. Registrar libro en BD (si ya existe por ISBN, lo reutiliza)
        BookResponse savedBook;
        if (request.isbn() != null && bookRepository.existsByIsbn(request.isbn())) {
            savedBook = bookRepository.findByIsbn(request.isbn())
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
        } else {
            BookRequest bookRequest = new BookRequest();
            bookRequest.setTitle(request.titulo_libro());
            bookRequest.setAuthor(request.autor());
            bookRequest.setIsbn(request.isbn() != null ? request.isbn() : generateIsbn(request.titulo_libro()));
            savedBook = bookService.create(bookRequest);
        }

        // 2. Construir payload para el MS de Daniel
        Map<String, Object> receiptPayload = Map.of(
                "empresa", "Biblioteca Central",
                "nit", "900.123.456-1",
                "item", savedBook.getTitle() + " - " + savedBook.getAuthor(),
                "isbn", savedBook.getIsbn(),
                "valor", 85000,
                "fecha", LocalDate.now().toString(),
                "comprador", "Biblioteca Central",
                "libro", Map.of(
                        "id", savedBook.getId(),
                        "titulo", savedBook.getTitle(),
                        "autor", savedBook.getAuthor(),
                        "isbn", savedBook.getIsbn()
                ),
                "pqr", Map.of(
                        "id", request.pqr().id(),
                        "asunto", request.pqr().asunto(),
                        "responsable", request.pqr().responsable(),
                        "conteo", request.pqr().conteo()
                )
        );

        // 3. Llamar al MS de Daniel (Receipts en GCP)
        ReceiptResponse receipt = receiptsWebClient.post()
                .uri("/api/v2/receipts/generate")
                .bodyValue(receiptPayload)
                .retrieve()
                .bodyToMono(ReceiptResponse.class)
                .block();

        // 4. Devolver respuesta enriquecida con los 3 objetos
        return new PurchaseResponse(
                request.pqr(),
                savedBook,
                receipt,
                receipt != null ? receipt.pdf_url() : null
        );
    }

    // Genera un ISBN temporal si no viene en el request
    private String generateIsbn(String titulo) {
        return "GEN-" + titulo.replaceAll("\\s+", "-").toUpperCase()
                + "-" + System.currentTimeMillis();
    }
}
