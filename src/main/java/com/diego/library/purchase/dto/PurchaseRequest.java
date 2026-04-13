package com.diego.library.purchase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PurchaseRequest(
        @NotBlank String titulo_libro,
        @NotBlank String autor,
        String isbn,
        @NotNull PqrDto pqr
) {}
