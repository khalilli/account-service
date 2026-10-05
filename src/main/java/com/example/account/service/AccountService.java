package com.example.account.service;

import com.example.account.dto.account.AccountResponse;
import com.example.account.dto.account.CreateAccountRequest;
import com.example.account.entity.Account;
import com.example.account.entity.Customer;
import com.example.account.exception.InsufficientBalanceException;
import com.example.account.exception.ResourceNotFoundException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public List<AccountResponse> getAllAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(this::toAccountResponse)
                .toList();
    }

    public AccountResponse getAccount(UUID accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found")
                );

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getCustomer().getId(),
                account.getCreatedAt()
        );
    }

    public AccountResponse createAccount(CreateAccountRequest request) {

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setCustomer(customer);

        Account savedAccount = accountRepository.save(account);

        return new AccountResponse(
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                savedAccount.getBalance(),
                savedAccount.getCurrency(),
                savedAccount.getCustomer().getId(),
                savedAccount.getCreatedAt()
        );
    }

    @Transactional
    public AccountResponse deposit(UUID accountId, BigDecimal amount) {

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found")
                );

        account.setBalance(account.getBalance().add(amount));

        Account savedAccount = accountRepository.save(account);

        return toAccountResponse(savedAccount);
    }

    @Transactional
    public AccountResponse withdraw(UUID accountId, BigDecimal amount) {

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found")
                );

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));

        Account savedAccount = accountRepository.save(account);

        return toAccountResponse(savedAccount);
    }

    public List<AccountResponse> getAccountsByCustomer(UUID customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found");
        }

        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toAccountResponse)
                .toList();
    }

    private AccountResponse toAccountResponse(Account account) {

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getCustomer().getId(),
                account.getCreatedAt()
        );
    }

    private String generateAccountNumber() {
        return "AZ" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 20)
                .toUpperCase();
    }
}