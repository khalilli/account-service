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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAccount() throws Exception {
        Random rand = new Random();
        int randomInt = rand.nextInt();

        String customerRequest = """
                {
                    "firstName": "Account",
                    "lastName": "Test",
                    "email": "account.test.integration%d@example.com"
                }
                """.formatted(randomInt);

        MvcResult customerResult = mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(customerRequest)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String customerResponse =
                customerResult.getResponse().getContentAsString();

        String customerId =
                JsonPath.read(customerResponse, "$.id");

        String accountRequest = """
                {
                    "customerId": "%s"
                }
                """.formatted(customerId);

        mockMvc.perform(
                        post("/api/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(accountRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountNumber").exists())
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("AZN"))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.createdAt").exists());
    }
}