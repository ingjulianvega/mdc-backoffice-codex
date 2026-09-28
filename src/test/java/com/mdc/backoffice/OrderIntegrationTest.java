package com.mdc.backoffice;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:orders;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class OrderIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired CustomerService customers;
    @Autowired ProductService products;
    @Autowired OrderRepository orders;
    @Autowired OrderItemRepository items;
    @Autowired ProductRepository productRepository;
    @Autowired CustomerRepository customerRepository;
    final JsonMapper json = JsonMapper.builder().build();
    final Instant date = Instant.parse("2026-09-28T12:00:00Z");
    UUID customerId, productId;

    @BeforeEach void setup() {
        orders.deleteAll(); productRepository.deleteAll(); customerRepository.deleteAll();
        customerId = customers.create(new CustomerRequestDTO("CC", "123", "Ana", "Diaz", null, "ana@example.com")).id();
        productId = products.create(new ProductRequestDTO("Candle", null, 50L, 10, null)).id();
    }
    String body(int quantity) {
        return json.writeValueAsString(new OrderRequestDTO(customerId, date, List.of(
                new OrderItemRequestDTO(productId, quantity, 3000000000L, null, "T1", "Carrier"))));
    }
    @Test void persistsReadsFiltersAndDeletesWithStockRestoration() throws Exception {
        var result = mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(body(2)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.totalAmount").value(6000000000L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").exists()).andReturn();
        var id = UUID.fromString(json.readTree(result.getResponse().getContentAsString()).get("id").asText());
        assertEquals(8, products.findById(productId).stockQuantity());
        assertEquals(1, items.count());
        mvc.perform(get("/api/v1/orders/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.customer.id").value(customerId.toString()))
                .andExpect(jsonPath("$.items[0].productName").value("Candle"))
                .andExpect(jsonPath("$.items[0].trackingNumber").value("T1"));
        mvc.perform(get("/api/v1/orders").param("customerId", customerId.toString())
                .param("startDate", date.toString()).param("endDate", date.toString()).param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
        for (var query : List.of(get("/api/v1/orders").param("customerId", UUID.randomUUID().toString()),
                get("/api/v1/orders").param("startDate", date.plusSeconds(1).toString()),
                get("/api/v1/orders").param("endDate", date.minusSeconds(1).toString()))) {
            mvc.perform(query).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        }
        mvc.perform(delete("/api/v1/orders/{id}", id)).andExpect(status().isNoContent());
        assertEquals(10, products.findById(productId).stockQuantity());
        assertEquals(0, orders.count()); assertEquals(0, items.count());
        mvc.perform(delete("/api/v1/orders/{id}", id)).andExpect(status().isNotFound());
        assertEquals(10, products.findById(productId).stockQuantity());
    }
    @Test void insufficientStockLeavesDatabaseUnchanged() throws Exception {
        mvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON).content(body(11)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        assertEquals(10, products.findById(productId).stockQuantity());
        assertEquals(0, orders.count()); assertEquals(0, items.count());
    }
    @Test void databaseFailureRollsBackPreviouslyWrittenStock() throws Exception {
        // Bypass HTTP length validation to force a persistence error after stock was updated.
        var request = new OrderRequestDTO(customerId, date, List.of(
                new OrderItemRequestDTO(productId, 2, 5L, null, "X".repeat(101), null)));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> orderService.create(request));
        assertEquals(10, products.findById(productId).stockQuantity());
        assertEquals(0, orders.count()); assertEquals(0, items.count());
    }
    @Autowired OrderService orderService;

    @Test void concurrentOrdersCannotOversell() throws Exception {
        var start = new java.util.concurrent.CountDownLatch(1);
        var request = new OrderRequestDTO(customerId, date, List.of(
                new OrderItemRequestDTO(productId, 6, 5L, null, null, null)));
        java.util.concurrent.Callable<Boolean> create = () -> {
            start.await();
            try {
                orderService.create(request);
                return true;
            } catch (com.mdc.backoffice.exception.InvalidOrderException expected) {
                return false;
            }
        };
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var first = executor.submit(create);
            var second = executor.submit(create);
            start.countDown();
            assertNotEquals(first.get(10, java.util.concurrent.TimeUnit.SECONDS),
                    second.get(10, java.util.concurrent.TimeUnit.SECONDS));
        }
        assertEquals(4, products.findById(productId).stockQuantity());
        assertEquals(1, orders.count());
    }
}
