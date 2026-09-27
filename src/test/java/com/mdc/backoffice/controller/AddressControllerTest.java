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

@WebMvcTest(AddressController.class)
class AddressControllerTest {
    private static final String BASE = "/api/v1/addresses";
    @Autowired MockMvc mvc;
    @MockitoBean AddressService service;
    private final UUID id = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID cityId = UUID.randomUUID();
    private final AddressResponseDTO response =
            new AddressResponseDTO(id, "Example", null, new CityResponseDTO(cityId, "Medellin", null, null, null, null, null), customerId, null, null);

    private String body(String streetAddressJson) {
        return "{\"streetAddress\":" + streetAddressJson + ",\"cityId\":\"" + cityId + "\",\"customerId\":\"" + customerId + "\"}";
    }

    @Test
    void getExistingReturnsJson() throws Exception {
        when(service.findById(id)).thenReturn(response);
        mvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.streetAddress").value("Example"));
    }

    @Test
    void createReturnsCreatedAndLocation() throws Exception {
        when(service.create(any())).thenReturn(response);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("\"Example\"")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost" + BASE + "/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
        verify(service).create(new AddressRequestDTO("Example", null, cityId, customerId));
    }

    @Test
    void updateReturnsUpdatedResource() throws Exception {
        when(service.update(eq(id), any())).thenReturn(response);
        mvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body("\"Example\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.streetAddress").value("Example"));
        verify(service).update(id, new AddressRequestDTO("Example", null, cityId, customerId));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isNoContent());
        verify(service).delete(id);
    }

    @Test
    void listPassesPaginationAndFilter() throws Exception {
        when(service.findAll(eq(cityId), eq(customerId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(1, 2), 3));
        mvc.perform(get(BASE).param("page", "1").param("size", "2").param("sort", "streetAddress,desc").param("cityId", cityId.toString()).param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(1));
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(eq(cityId), eq(customerId), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(2, captor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("streetAddress").getDirection());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void invalidStreetAddressReturnsProblemDetail(String streetAddress) throws Exception {
        var json = streetAddress == null ? "null" : "\"" + streetAddress + "\"";
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body(json)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:problem:http:400"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value(BASE))
                .andExpect(jsonPath("$.errors.streetAddress").exists());
        verifyNoInteractions(service);
    }

    @Test
    void overlongStreetAddressIsRejectedOnUpdate() throws Exception {
        mvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"" + "a".repeat(256) + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.streetAddress").exists());
        verifyNoInteractions(service);
    }

    @Test
    void maximumLengthStreetAddressIsAccepted() throws Exception {
        when(service.create(any())).thenReturn(response);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"" + "a".repeat(255) + "\"")))
                .andExpect(status().isCreated());
    }

    @Test
    void missingResourceReturnsProblemDetail() throws Exception {
        when(service.findById(id)).thenThrow(new ResourceNotFoundException("Address", id));
        mvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Address not found: " + id))
                .andExpect(jsonPath("$.instance").value(BASE + "/" + id));
    }

    @Test
    void missingUpdateReturnsNotFound() throws Exception {
        when(service.update(eq(id), any())).thenThrow(new ResourceNotFoundException("Address", id));
        mvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body("\"Example\"")))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingDeleteReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Address", id)).when(service).delete(id);
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
    @Test
    void missingCityIsBadRequest() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"streetAddress\":\"Address\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cityId").exists());
        verifyNoInteractions(service);
    }

    @Test
    void unknownCityIsNotFound() throws Exception {
        when(service.create(any())).thenThrow(new ResourceNotFoundException("City", cityId));
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("\"Address\"")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void invalidCityFilterIsBadRequest() throws Exception {
        mvc.perform(get(BASE).param("cityId", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }
    @ParameterizedTest
    @ValueSource(strings = {"streetAddress", "additionalInfo"})
    void oversizedFieldsAreRejected(String field) throws Exception {
        var json = tools.jackson.databind.json.JsonMapper.builder().build();
        var values = new HashMap<String, Object>();
        values.put("streetAddress", "Calle");
        values.put("cityId", cityId.toString());
        values.put("customerId", customerId.toString());
        values.put(field, "a".repeat(256));
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(values)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + field).exists());
        verifyNoInteractions(service);
    }

    @Test
    void additionalInfoMayBeOmitted() throws Exception {
        when(service.create(any())).thenReturn(response);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"Calle\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.city.id").value(cityId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()));
        verify(service).create(new AddressRequestDTO("Calle", null, cityId, customerId));
    }

    @Test
    void unknownCityOnUpdateReturnsProblemDetail() throws Exception {
        when(service.update(eq(id), any())).thenThrow(new ResourceNotFoundException("City", cityId));
        mvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body("\"Example\"")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:problem:http:404"))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("City not found: " + cityId))
                .andExpect(jsonPath("$.instance").value(BASE + "/" + id));
    }

    @Test
    void invalidCustomerFilterIsRejected() throws Exception {
        mvc.perform(get(BASE).param("customerId", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        verifyNoInteractions(service);
    }

    @Test
    void defaultPaginationWithoutFilters() throws Exception {
        when(service.findAll(isNull(), isNull(), any(Pageable.class))).thenReturn(Page.empty());
        mvc.perform(get(BASE)).andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAll(isNull(), isNull(), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(Sort.by("streetAddress"), captor.getValue().getSort());
    }

    static java.util.stream.Stream<Arguments> invalidRequiredIds() {
        return java.util.stream.Stream.of("cityId", "customerId").flatMap(field ->
                java.util.stream.Stream.of("POST", "PUT").flatMap(method ->
                        java.util.stream.Stream.of("missing", "null", "empty", "blank", "malformed")
                                .map(value -> Arguments.of(field, method, value))));
    }

    @ParameterizedTest
    @MethodSource("invalidRequiredIds")
    void requiredIdsReturnProblemDetail(String field, String method, String value) throws Exception {
        var values = new HashMap<String, Object>();
        values.put("streetAddress", "Calle 10 # 43-20");
        values.put("cityId", cityId.toString());
        values.put("customerId", customerId.toString());
        switch (value) {
            case "missing" -> values.remove(field);
            case "null" -> values.put(field, null);
            case "empty" -> values.put(field, "");
            case "blank" -> values.put(field, " ");
            default -> values.put(field, "invalid-uuid");
        }
        var request = method.equals("POST") ? post(BASE) : put(BASE + "/{id}", id);
        var json = tools.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(values);
        mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:problem:http:400"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value(method.equals("POST") ? BASE : BASE + "/" + id));
        verifyNoInteractions(service);
    }

    @Test
    void fullPayloadPreservesAdditionalInfo() throws Exception {
        var request = new AddressRequestDTO("Calle 10 # 43-20", "Apartamento 301", cityId, customerId);
        var result = new AddressResponseDTO(id, request.streetAddress(), request.additionalInfo(),
                response.city(), customerId, null, null);
        when(service.create(request)).thenReturn(result);
        var json = tools.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(request);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.streetAddress").value(request.streetAddress()))
                .andExpect(jsonPath("$.additionalInfo").value(request.additionalInfo()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.street").doesNotExist())
                .andExpect(jsonPath("$.number").doesNotExist())
                .andExpect(jsonPath("$.complement").doesNotExist());
        verify(service).create(request);
    }
}
