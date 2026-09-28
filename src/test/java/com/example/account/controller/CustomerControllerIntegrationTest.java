package com.example.account.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Random;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateCustomer() throws Exception {

        Random rand = new Random();
        int randomInt = rand.nextInt();

        String requestBody = """
            { 
              "firstName": "John", 
              "lastName": "Doe", 
              "email": "john.doe.integration%d@example.com" 
            } 
            """.formatted(randomInt);

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email")
                        .value("john.doe.integration%d@example.com".formatted(randomInt)))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldRejectCustomerWithBlankFirstName() throws Exception {

        String requestBody = """
            {
                "firstName": "",
                "lastName": "Doe",
                "email": "john.doe@example.com"
            }
            """;

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.firstName").exists());
    }

    @Test
    void shouldRejectDuplicateCustomerEmail() throws Exception {
        Random rand = new Random();
        int randomInt = rand.nextInt();

        String requestBody = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "duplicate.customer%d@example.com"
            }
            """.formatted(randomInt);

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Customer with this email already exists"));
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        Random rand = new Random();
        int randomInt = rand.nextInt();

        String requestBody = """
            {
                "firstName": "Jane",
                "lastName": "Smith",
                "email": "jane.smith.integration%d@example.com"
            }
            """.formatted(randomInt);

        MvcResult createResult = mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody =
                createResult.getResponse().getContentAsString();

        String customerId = JsonPath.read(responseBody, "$.id");

        mockMvc.perform(
                        get("/api/customers/{customerId}", customerId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email")
                        .value("jane.smith.integration%d@example.com".formatted(randomInt)));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {

        UUID customerId = UUID.randomUUID();

        mockMvc.perform(
                        get("/api/customers/{customerId}", customerId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Customer not found"));
    }

    @Test
    void shouldGetAllCustomers() throws Exception {
        Random rand = new Random();
        int randomInt = rand.nextInt();

        String firstCustomer = """
            {
                "firstName": "Alice",
                "lastName": "Brown",
                "email": "alice.integration%d@example.com"
            }
            """.formatted(randomInt);

        String secondCustomer = """
            {
                "firstName": "Bob",
                "lastName": "Green",
                "email": "bob.integration%d@example.com"
            }
            """.formatted(randomInt);

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstCustomer)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondCustomer)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/customers")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.email == 'alice.integration%d@example.com')]".formatted(randomInt))
                        .exists())
                .andExpect(jsonPath("$[?(@.email == 'bob.integration%d@example.com')]".formatted(randomInt))
                        .exists());
    }
}