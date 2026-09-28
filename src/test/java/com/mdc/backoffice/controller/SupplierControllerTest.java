package com.mdc.backoffice.controller;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.service.*;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
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

@WebMvcTest(SupplierController.class)
class SupplierControllerTest {
    static final String BASE = "/api/v1/suppliers";
    @Autowired MockMvc mvc;
    @MockitoBean SupplierService service;
    final UUID id = UUID.randomUUID();
    final SupplierRequestDTO request = new SupplierRequestDTO("Wax company", "Ana", "supplier@example.com", "3001234567", "900123");
    final SupplierResponseDTO response = new SupplierResponseDTO(id, "Wax company", "Ana", "supplier@example.com", "3001234567", "900123", null, null, null, null);
    final JsonMapper json = JsonMapper.builder().build();

    @Test
    void createReturnsJsonAndLocation() throws Exception {
        when(service.create(request)).thenReturn(response);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost" + BASE + "/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.companyName").value("Wax company"))
                ;
        verify(service).create(request);
    }

    @Test
    void readAndUpdateReturnJson() throws Exception {
        when(service.findById(id)).thenReturn(response);
        when(service.update(id, request)).thenReturn(response);
        mvc.perform(get(BASE + "/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
        mvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id.toString()));
        verify(service).update(id, request);
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(service).delete(id);
    }

    @Test
    void filtersAndPaginationAreForwarded() throws Exception {
        when(service.findAll(eq("wax"), eq("wax"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(1, 2), 3));
        mvc.perform(get(BASE).param("companyName", "wax").param("taxId", "wax")
                        .param("page", "1").param("size", "2").param("sort", "id,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.number").value(1));
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(eq("wax"), eq("wax"), captor.capture());
        assertEquals(PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "id")), captor.getValue());
    }

    @Test
    void defaultListAllowsNoFilters() throws Exception {
        when(service.findAll(isNull(), isNull(), any(Pageable.class))).thenReturn(Page.empty());
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(isNull(), isNull(), captor.capture());
        assertEquals(Sort.by("companyName"), captor.getValue().getSort());
    }

    @Test
    void missingResourceUsesProblemDetailForAllOperations() throws Exception {
        when(service.findById(id)).thenThrow(new ResourceNotFoundException("Supplier", id));
        when(service.update(id, request)).thenThrow(new ResourceNotFoundException("Supplier", id));
        doThrow(new ResourceNotFoundException("Supplier", id)).when(service).delete(id);
        for (var operation : List.of(get(BASE + "/{id}", id),
                put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)),
                delete(BASE + "/{id}", id))) {
            mvc.perform(operation).andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("urn:problem:http:404"))
                    .andExpect(jsonPath("$.title").value("Not Found"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.detail").value("Supplier not found: " + id))
                    .andExpect(jsonPath("$.instance").value(BASE + "/" + id));
        }
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("companyName", null),
                Arguments.of("companyName", " "),
                Arguments.of("companyName", "x".repeat(151)),
                Arguments.of("contactName", "x".repeat(101)),
                Arguments.of("email", "x".repeat(151)),
                Arguments.of("email", "invalid-email"),
                Arguments.of("phone", "x".repeat(31)),
                Arguments.of("taxId", null),
                Arguments.of("taxId", " "),
                Arguments.of("taxId", "x".repeat(31)));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void invalidRequestsFailBeforeService(String field, Object value) throws Exception {
        var body = new LinkedHashMap<String, Object>();
        body.put("companyName", "Wax company");
        body.put("contactName", "Ana");
        body.put("email", "supplier@example.com");
        body.put("phone", "3001234567");
        body.put("taxId", "900123");
        body.put(field, value);
        for (var operation : List.of(post(BASE), put(BASE + "/{id}", id))) {
            mvc.perform(operation.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("urn:problem:http:400"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").exists());
        }
        verifyNoInteractions(service);
    }

    @Test
    void malformedInputAndMissingFieldsReturn400() throws Exception {
        mvc.perform(get(BASE + "/invalid")).andExpect(status().isBadRequest());
        for (String body : List.of("{", "{}")) {
            mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        }
        verifyNoInteractions(service);
    }


    @Test
    void duplicatesReturnProblemDetail() throws Exception {
        when(service.create(request)).thenThrow(new com.mdc.backoffice.exception.DuplicateResourceException("Already exists"));
        when(service.update(id, request)).thenThrow(new com.mdc.backoffice.exception.DuplicateResourceException("Already exists"));
        for (var operation : List.of(post(BASE), put(BASE + "/{id}", id))) {
            mvc.perform(operation.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("urn:problem:http:409"))
                    .andExpect(jsonPath("$.title").value("Conflict"))
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.detail").value("Already exists"))
                    .andExpect(jsonPath("$.instance").exists());
        }
    }
}
