package com.mdc.backoffice.controller;

import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.model.enum_.*;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.impl.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PetCandlePhotoController.class)
@Import({PetCandlePhotoServiceImpl.class, PetCandlePhotoMapperImpl.class})
class PetCandlePhotoControllerTest {
    static final String BASE = "/api/v1/pet-candle-photos";
    @Autowired MockMvc mvc;
    @MockitoBean PetCandlePhotoRepository photos;
    @MockitoBean PetCandleRepository candles;
    final UUID id = UUID.randomUUID();
    final UUID candleId = UUID.randomUUID();

    @Test void createsListsAndDeletesPhoto() throws Exception {
        var candle = new PetCandleEntity(); candle.setId(candleId);
        when(candles.findById(candleId)).thenReturn(Optional.of(candle));
        when(photos.save(any())).thenAnswer(inv -> {
            PetCandlePhotoEntity photo = inv.getArgument(0); photo.setId(id); return photo;
        });
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petCandleId\":\"" + candleId + "\",\"photoUrl\":\"https://example.com/p.jpg\",\"isPrimary\":true}"))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost" + BASE + "/" + id))
                .andExpect(jsonPath("$.petCandleId").value(candleId.toString()))
                .andExpect(jsonPath("$.photoUrl").value("https://example.com/p.jpg"))
                .andExpect(jsonPath("$.isPrimary").value(true));
        var photo = new PetCandlePhotoEntity(); photo.setId(id); photo.setPetCandle(candle);
        when(candles.existsById(candleId)).thenReturn(true);
        when(photos.findAllByPetCandleId(candleId)).thenReturn(List.of(photo));
        mvc.perform(get(BASE + "/by-pet-candle/{id}", candleId)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));
        when(photos.findById(id)).thenReturn(Optional.of(photo));
        mvc.perform(delete(BASE + "/{id}", id)).andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(photos).delete(photo);
    }

    @Test void existingCandleWithNoPhotosReturnsEmptyList() throws Exception {
        when(candles.existsById(candleId)).thenReturn(true);
        mvc.perform(get(BASE + "/by-pet-candle/{id}", candleId))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "LONG"})
    void invalidPhotoUrlReturns400(String url) throws Exception {
        if (url.equals("LONG")) url = "x".repeat(501);
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petCandleId\":\"" + candleId + "\",\"photoUrl\":\"" + url + "\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.photoUrl").exists());
        verifyNoInteractions(photos, candles);
    }

    @Test void missingParentAndMalformedUuidReturn400() throws Exception {
        for (var request : List.of(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"photoUrl\":\"url\"}"),
                post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"petCandleId\":\"bad\",\"photoUrl\":\"url\"}"),
                get(BASE + "/by-pet-candle/invalid"), delete(BASE + "/invalid"))) {
            mvc.perform(request).andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        }
        verifyNoInteractions(candles, photos);
    }

    @Test void nonexistentResourcesReturn404() throws Exception {
        for (var request : List.of(get(BASE + "/by-pet-candle/{id}", candleId), delete(BASE + "/{id}", id),
                post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petCandleId\":\"" + candleId + "\",\"photoUrl\":\"url\"}"))) {
            mvc.perform(request).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        }
    }
}
