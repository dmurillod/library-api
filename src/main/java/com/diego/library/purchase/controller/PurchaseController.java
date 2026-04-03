package com.diego.library.purchase.controller;

import com.diego.library.book.dto.BookRequest;
import com.diego.library.book.dto.BookResponse;
import com.diego.library.book.service.BookService;
import com.diego.library.purchase.dto.PurchaseRequest;
import com.diego.library.purchase.dto.PurchaseResponse;
import com.diego.library.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/books")
@Tag(name = "Purchase v2", description = "Orden de compra de libros - flujo multicloud")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final BookService bookService;

    public PurchaseController(PurchaseService purchaseService, BookService bookService) {
        this.purchaseService = purchaseService;
        this.bookService = bookService;
    }

    @PostMapping("/purchase")
    @Operation(summary = "Recibe orden de compra desde MS PQR y genera factura en cadena")
    public ResponseEntity<PurchaseResponse> purchase(
            @Valid @RequestBody PurchaseRequest request) {
        return ResponseEntity.ok(purchaseService.processPurchase(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifica un libro en tiempo real")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }
}
