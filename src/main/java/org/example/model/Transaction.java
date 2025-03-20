package org.example.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(
        Long id,
        @Positive(message = "amount must bigger than 0")
        BigDecimal amount,
        @NotNull(message = "transaction type should not be empty")
        TransactionType type,
        LocalDateTime timestamp,
        String description
) {
    public Transaction withId(Long id) {
        return new Transaction(id, this.amount, this.type, this.timestamp, this.description);
    }
}