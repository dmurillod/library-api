package com.diego.library.purchase.dto;

import com.diego.library.book.dto.BookResponse;

public record PurchaseResponse(
        PqrDto pqr,
        BookResponse libro,
        ReceiptResponse receipt,
        String pdf_url
) {}
