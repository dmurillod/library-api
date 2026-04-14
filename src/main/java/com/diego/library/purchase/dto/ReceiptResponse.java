package com.diego.library.purchase.dto;

public record ReceiptResponse(
        String id,
        String empresa,
        String nit,
        String item,
        Long valor,
        String fecha,
        String pdf_url
) {}