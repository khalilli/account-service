package com.example.account.dto.account;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAccountRequest(
        @NotNull
        UUID customerId
) {
}