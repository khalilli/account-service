package com.example.account.service;

import com.example.account.dto.account.AccountResponse;
import com.example.account.entity.Account;
import com.example.account.entity.Customer;
import com.example.account.exception.InsufficientBalanceException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldDepositMoney() {

        UUID accountId = UUID.randomUUID();

        Account account = new Account();
        account.setId(accountId);
        account.setAccountNumber("AZ123456789");
        account.setBalance(new BigDecimal("100.00"));
        account.setCurrency("AZN");

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());

        account.setCustomer(customer);

        when(accountRepository.findByIdForUpdate(accountId))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(account))
                .thenReturn(account);

        AccountResponse response =
                accountService.deposit(
                        accountId,
                        new BigDecimal("50.00")
                );

        assertEquals(
                new BigDecimal("150.00"),
                response.balance()
        );

        verify(accountRepository).findByIdForUpdate(accountId);
        verify(accountRepository).save(account);
    }

    @Test
    void shouldRejectWithdrawalWhenBalanceIsInsufficient() {

        UUID accountId = UUID.randomUUID();

        Account account = new Account();
        account.setId(accountId);
        account.setAccountNumber("AZ123456789");
        account.setBalance(new BigDecimal("100.00"));
        account.setCurrency("AZN");

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());

        account.setCustomer(customer);

        when(accountRepository.findByIdForUpdate(accountId))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.withdraw(
                        accountId,
                        new BigDecimal("150.00")
                )
        );

        verify(accountRepository).findByIdForUpdate(accountId);
        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void shouldWithdrawMoney() {

        UUID accountId = UUID.randomUUID();

        Account account = new Account();
        account.setId(accountId);
        account.setAccountNumber("AZ123456789");
        account.setBalance(new BigDecimal("100.00"));
        account.setCurrency("AZN");

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());

        account.setCustomer(customer);

        when(accountRepository.findByIdForUpdate(accountId))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(account))
                .thenReturn(account);

        AccountResponse response =
                accountService.withdraw(
                        accountId,
                        new BigDecimal("30.00")
                );

        assertEquals(
                new BigDecimal("70.00"),
                response.balance()
        );

        verify(accountRepository).findByIdForUpdate(accountId);
        verify(accountRepository).save(account);
    }
}