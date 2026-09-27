package com.example.account.service;

import com.example.account.dto.CreateCustomerRequest;
import com.example.account.dto.CustomerResponse;
import com.example.account.entity.Customer;
import com.example.account.exception.DuplicateResourceException;
import com.example.account.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() {

        CreateCustomerRequest request =
                new CreateCustomerRequest(
                        "John",
                        "Doe",
                        "john@example.com"
                );

        Customer savedCustomer = new Customer();
        savedCustomer.setId(UUID.randomUUID());
        savedCustomer.setFirstName("John");
        savedCustomer.setLastName("Doe");
        savedCustomer.setEmail("john@example.com");
        savedCustomer.setCreatedAt(Instant.now());

        when(customerRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(savedCustomer);

        CustomerResponse response =
                customerService.createCustomer(request);

        assertEquals(savedCustomer.getId(), response.id());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("john@example.com", response.email());

        verify(customerRepository).existsByEmail(request.email());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldRejectDuplicateCustomerEmail() {

        CreateCustomerRequest request =
                new CreateCustomerRequest(
                        "John",
                        "Doe",
                        "john@example.com"
                );

        when(customerRepository.existsByEmail(request.email()))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> customerService.createCustomer(request)
        );

        verify(customerRepository).existsByEmail(request.email());
        verify(customerRepository, never())
                .save(any(Customer.class));
    }
}