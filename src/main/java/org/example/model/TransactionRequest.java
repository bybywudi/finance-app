package org.example.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequest(
        @Positive(message = "amount must bigger than 0")
        BigDecimal amount,

        @NotNull(message = "transaction type should not be empty")
        TransactionType type,

        String description
) {
    public Transaction toEntity() {
        return new Transaction(null, amount, type, LocalDateTime.now(), description);
    }
}