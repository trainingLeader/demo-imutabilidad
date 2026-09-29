package com.inmutabilidad.demo_imutabilidad.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsAdjustedPriceForOneProduct() throws Exception {
        mockMvc.perform(get("/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(6525000.00));
    }

    @Test
    void returnsAdjustedPriceInProductList() throws Exception {
        mockMvc.perform(get("/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].price").value(6525000.00));
    }

    @Test
    void returnsNotFoundProblemWhenProductDoesNotExist() throws Exception {
        mockMvc.perform(get("/products/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Product with id 999 was not found"));
    }
}
