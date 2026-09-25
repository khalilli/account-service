package com.example.account.controller;

import com.example.account.dto.account.AccountResponse;
import com.example.account.dto.account.CreateAccountRequest;
import com.example.account.dto.account.MoneyOperationRequest;
import com.example.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{accountId}")
    public AccountResponse getAccount(
            @PathVariable UUID accountId
    ) {
        return accountService.getAccount(accountId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        return accountService.createAccount(request);
    }

    @PostMapping("/{accountId}/deposit")
    public AccountResponse deposit(
            @PathVariable UUID accountId,
            @Valid @RequestBody MoneyOperationRequest request
    ) {
        return accountService.deposit(accountId, request.amount());
    }

    @PostMapping("/{accountId}/withdraw")
    public AccountResponse withdraw(
            @PathVariable UUID accountId,
            @Valid @RequestBody MoneyOperationRequest request
    ) {
        return accountService.withdraw(accountId, request.amount());
    }

    @GetMapping("/customer/{customerId}")
    public List<AccountResponse> getAccountsByCustomer(
            @PathVariable UUID customerId
    ) {
        return accountService.getAccountsByCustomer(customerId);
    }
}