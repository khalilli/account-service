package com.example.account.dto.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        BigDecimal balance,
        String currency,
        UUID customerId,
        Instant createdAt
) {
}