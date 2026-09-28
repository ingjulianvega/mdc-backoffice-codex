package com.mdc.backoffice.controller;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.service.*;
import java.util.*;
import java.math.BigDecimal;
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

@WebMvcTest(MaterialController.class)
class MaterialControllerTest {
    static final String BASE = "/api/v1/materials";
    @Autowired MockMvc mvc;
    @MockitoBean MaterialService service;
    final UUID id = UUID.randomUUID();
    final UUID productId = UUID.randomUUID();
    final UUID materialId = UUID.randomUUID();
    final MaterialRequestDTO request = new MaterialRequestDTO("Wax", "g", new BigDecimal("12.5000"));
    final MaterialResponseDTO response = new MaterialResponseDTO(id, "Wax", "g", new BigDecimal("12.5000"), null, null, null, null);
    final JsonMapper json = JsonMapper.builder().build();

    @Test
    void createReturnsJsonAndLocation() throws Exception {
        when(service.create(request)).thenReturn(response);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost" + BASE + "/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.currentStock").value(12.5))
                .andExpect(jsonPath("$.unitOfMeasure").value("g"));
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
        when(service.findAll(eq("wax"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(1, 2), 3));
        mvc.perform(get(BASE).param("name", "wax")
                        .param("page", "1").param("size", "2").param("sort", "id,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.number").value(1));
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(eq("wax"), captor.capture());
        assertEquals(PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "id")), captor.getValue());
    }

    @Test
    void defaultListAllowsNoFilters() throws Exception {
        when(service.findAll(isNull(), any(Pageable.class))).thenReturn(Page.empty());
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(isNull(), captor.capture());
        assertEquals(Sort.by("name"), captor.getValue().getSort());
    }

    @Test
    void missingResourceUsesProblemDetailForAllOperations() throws Exception {
        when(service.findById(id)).thenThrow(new ResourceNotFoundException("Material", id));
        when(service.update(id, request)).thenThrow(new ResourceNotFoundException("Material", id));
        doThrow(new ResourceNotFoundException("Material", id)).when(service).delete(id);
        for (var operation : List.of(get(BASE + "/{id}", id),
                put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)),
                delete(BASE + "/{id}", id))) {
            mvc.perform(operation).andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("urn:problem:http:404"))
                    .andExpect(jsonPath("$.title").value("Not Found"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.detail").value("Material not found: " + id))
                    .andExpect(jsonPath("$.instance").value(BASE + "/" + id));
        }
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("name", null),
                Arguments.of("unitOfMeasure", null),
                Arguments.of("currentStock", null),
                Arguments.of("name", " "),
                Arguments.of("name", "x".repeat(151)),
                Arguments.of("unitOfMeasure", ""),
                Arguments.of("unitOfMeasure", "x".repeat(51)),
                Arguments.of("currentStock", -1),
                Arguments.of("currentStock", "100000000"),
                Arguments.of("currentStock", "0.00001"));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void invalidRequestsFailBeforeService(String field, Object value) throws Exception {
        var body = new LinkedHashMap<String, Object>();
        body.put("name", "Wax");
        body.put("unitOfMeasure", "g");
        body.put("currentStock", 0);
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

}

