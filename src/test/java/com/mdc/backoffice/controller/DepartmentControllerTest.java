package com.mdc.backoffice.controller;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.service.*;
import java.util.*;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {
    private static final String BASE = "/api/v1/departments";
    @Autowired MockMvc mvc;
    @MockitoBean DepartmentService service;
    private final UUID id = UUID.randomUUID();
    private final UUID departmentId = UUID.randomUUID();
    private final DepartmentResponseDTO response =
            new DepartmentResponseDTO(id, "Example", null, null, null, null);

    private String body(String nameJson) {
        return "{\"name\":" + nameJson + "}";
    }

    @Test
    void getExistingReturnsJson() throws Exception {
        when(service.findById(id)).thenReturn(response);
        mvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Example"));
    }

    @Test
    void createReturnsCreatedAndLocation() throws Exception {
        when(service.create(any())).thenReturn(response);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("\"Example\"")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost" + BASE + "/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
        verify(service).create(new DepartmentRequestDTO("Example"));
    }

    @Test
    void updateReturnsUpdatedResource() throws Exception {
        when(service.update(eq(id), any())).thenReturn(response);
        mvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body("\"Example\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Example"));
        verify(service).update(id, new DepartmentRequestDTO("Example"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isNoContent());
        verify(service).delete(id);
    }

    @Test
    void listPassesPaginationAndFilter() throws Exception {
        when(service.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(1, 2), 3));
        mvc.perform(get(BASE).param("page", "1").param("size", "2").param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(1));
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(2, captor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("name").getDirection());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void invalidNameReturnsProblemDetail(String name) throws Exception {
        var json = name == null ? "null" : "\"" + name + "\"";
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body(json)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:problem:http:400"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value(BASE))
                .andExpect(jsonPath("$.errors.name").exists());
        verifyNoInteractions(service);
    }

    @Test
    void overlongNameIsRejectedOnUpdate() throws Exception {
        mvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"" + "a".repeat(101) + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
        verifyNoInteractions(service);
    }

    @Test
    void maximumLengthNameIsAccepted() throws Exception {
        when(service.create(any())).thenReturn(response);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"" + "a".repeat(100) + "\"")))
                .andExpect(status().isCreated());
    }

    @Test
    void missingResourceReturnsProblemDetail() throws Exception {
        when(service.findById(id)).thenThrow(new ResourceNotFoundException("Department", id));
        mvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Department not found: " + id))
                .andExpect(jsonPath("$.instance").value(BASE + "/" + id));
    }

    @Test
    void missingUpdateReturnsNotFound() throws Exception {
        when(service.update(eq(id), any())).thenThrow(new ResourceNotFoundException("Department", id));
        mvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body("\"Example\"")))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingDeleteReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Department", id)).when(service).delete(id);
        mvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void malformedUuidReturnsProblemDetail() throws Exception {
        mvc.perform(get(BASE + "/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void malformedJsonReturnsProblemDetail() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

}
