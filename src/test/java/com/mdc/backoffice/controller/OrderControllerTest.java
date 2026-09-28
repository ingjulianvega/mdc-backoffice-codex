package com.mdc.backoffice.controller;

import com.mdc.backoffice.exception.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.service.OrderService;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    static final String BASE = "/api/v1/orders";
    @Autowired MockMvc mvc;
    @MockitoBean OrderService service;
    final JsonMapper json = JsonMapper.builder().build();
    final UUID id = UUID.randomUUID();
    final UUID customerId = UUID.randomUUID();
    final UUID productId = UUID.randomUUID();
    final Instant date = Instant.parse("2026-09-27T12:00:00Z");

    OrderRequestDTO request() {
        return new OrderRequestDTO(customerId, date,
                List.of(new OrderItemRequestDTO(productId, 2, 3L, null, null, null)));
    }

    OrderResponseDTO response() {
        return new OrderResponseDTO(id,
                new CustomerResponseDTO(customerId, "CC", "123", "Ana", "Diaz", null, "a@b.co", null, null, null, null),
                date, 6L, List.of(new OrderItemResponseDTO(
                        UUID.randomUUID(), productId, "Wax", 2, 3L, 6L, null, null, null)), "PENDING", date);
    }

    @Test
    void createReturns201LocationAndNestedContract() throws Exception {
        when(service.create(request())).thenReturn(response());
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost" + BASE + "/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.customer.id").value(customerId.toString()))
                .andExpect(jsonPath("$.orderDate").value(date.toString()))
                .andExpect(jsonPath("$.createdAt").value(date.toString()))
                .andExpect(jsonPath("$.totalAmount").value(6))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$.items[0].productName").value("Wax"))
                .andExpect(jsonPath("$.items[0].subtotal").value(6));
        verify(service).create(request());
    }

    @Test
    void detailIncludesItemsAndDeleteReturns204() throws Exception {
        when(service.findById(id)).thenReturn(response());
        mvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
        mvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).delete(id);
    }

    @Test
    void forwardsFiltersAndPagination() throws Exception {
        when(service.findAll(eq(customerId), eq(date), eq(date), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response()), PageRequest.of(1, 2), 3));
        mvc.perform(get(BASE).param("customerId", customerId.toString())
                        .param("startDate", date.toString()).param("endDate", date.toString())
                        .param("page", "1").param("size", "2").param("sort", "orderDate,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.number").value(1));
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(eq(customerId), eq(date), eq(date), captor.capture());
        assertEquals(PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "orderDate")), captor.getValue());
    }

    @Test
    void listWithoutFilters() throws Exception {
        when(service.findAll(isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(Page.empty());
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}", "{", "{\"customerId\":\"invalid\",\"items\":[]}",
        "{\"items\":[]}", "{\"items\":[null]}",
        "{\"items\":[{}]}",
        "{\"items\":[{\"quantity\":0,\"unitPrice\":1}]}",
        "{\"items\":[{\"quantity\":1,\"unitPrice\":-1}]}",
        "{\"items\":[{\"quantity\":0.00001,\"unitPrice\":1}]}",
        "{\"items\":[{\"quantity\":1,\"unitPrice\":9223372036854775808}]}"
    })
    void invalidBodiesReturnProblem400WithoutCallingService(String body) throws Exception {
        if (body.startsWith("{\"items\"")) {
            body = body.replaceFirst("\\{", "{\"customerId\":\"" + customerId + "\",\"orderDate\":\"" + date + "\",");
            body = body.replace("{\"quantity\"", "{\"productId\":\"" + productId + "\",\"quantity\"");
        }
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("urn:problem:http:400"));
        verifyNoInteractions(service);
    }

    @Test
    void missingQuantityIsRejectedByNestedValidation() throws Exception {
        var body = "{\"customerId\":\"" + customerId + "\",\"orderDate\":\"" + date + "\",\"items\":[{\"productId\":\"" + productId + "\",\"unitPrice\":1}]}";
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").exists());
        verifyNoInteractions(service);
    }

    @Test
    void malformedQueryAndPathReturn400() throws Exception {
        for (var operation : List.of(get(BASE + "/invalid"), get(BASE).param("customerId", "invalid"),
                get(BASE).param("startDate", "yesterday"), get(BASE).param("endDate", "invalid"))) {
            mvc.perform(operation).andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        }
        verifyNoInteractions(service);
    }

    @Test
    void missingReferencesReturn404OnCreate() throws Exception {
        for (String resource : List.of("Customer", "Product")) {
            doThrow(new ResourceNotFoundException(resource, id)).when(service).create(request());
            mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request())))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("urn:problem:http:404"))
                    .andExpect(jsonPath("$.title").value("Not Found"))
                    .andExpect(jsonPath("$.detail").value(resource + " not found: " + id))
                    .andExpect(jsonPath("$.instance").value(BASE));
        }
    }

    @Test
    void missingOrderReturns404ForReadAndDelete() throws Exception {
        when(service.findById(id)).thenThrow(new ResourceNotFoundException("Order", id));
        doThrow(new ResourceNotFoundException("Order", id)).when(service).delete(id);
        for (var operation : List.of(get(BASE + "/{id}", id), delete(BASE + "/{id}", id))) {
            mvc.perform(operation).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.instance").value(BASE + "/" + id));
        }
    }

    @Test
    void businessInconsistenciesReturnProblem400() throws Exception {
        when(service.create(request())).thenThrow(new InvalidOrderException("Stock out of range"));
        doThrow(new InvalidOrderException("Stock out of range")).when(service).delete(id);
        when(service.findAll(isNull(), eq(date), eq(date.minusSeconds(1)), any(Pageable.class)))
                .thenThrow(new InvalidOrderException("Invalid date range"));
        for (var operation : List.of(
                post(BASE).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request())),
                delete(BASE + "/{id}", id),
                get(BASE).param("startDate", date.toString()).param("endDate", date.minusSeconds(1).toString()))) {
            mvc.perform(operation).andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.detail").exists());
        }
    }
}
