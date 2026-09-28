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

/** HTTP -> validation -> real service -> generated mappers; only persistence is mocked. */
@WebMvcTest(PetCandleController.class)
@Import({PetCandleServiceImpl.class, PetCandleMapperImpl.class, PetCandlePhotoMapperImpl.class})
class PetCandleControllerTest {
    static final String BASE = "/api/v1/pet-candles";
    @Autowired MockMvc mvc;
    @MockitoBean PetCandleRepository candles;
    @MockitoBean OrderItemRepository items;
    @MockitoBean OrderRepository orders;
    @MockitoBean MaterialRepository materials;
    final UUID id = UUID.randomUUID();
    OrderEntity order;
    OrderItemEntity item;
    PetCandleEntity candle;

    @BeforeEach void setup() {
        order = new OrderEntity(); order.setId(UUID.randomUUID()); order.setStatus(OrderStatus.PENDING);
        item = new OrderItemEntity(); item.setId(UUID.randomUUID()); item.setOrder(order);
        order.getItems().add(item);
        candle = new PetCandleEntity(); candle.setId(id); candle.setOrderItem(item); candle.setPetName("Luna");
    }

    @Test void createsInitialRecordWithLocation() throws Exception {
        when(items.findById(item.getId())).thenReturn(Optional.of(item));
        when(orders.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(candles.save(any())).thenAnswer(inv -> {
            PetCandleEntity entity = inv.getArgument(0); entity.setId(id); return entity;
        });
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderItemId\":\"" + item.getId() + "\"}"))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "http://localhost" + BASE + "/" + id))
                .andExpect(jsonPath("$.orderItemId").value(item.getId().toString()))
                .andExpect(jsonPath("$.baseStatus").value("PENDING"))
                .andExpect(jsonPath("$.headStatus").value("PENDING"))
                .andExpect(jsonPath("$.labelStatus").value("PENDING"))
                .andExpect(jsonPath("$.assemblyStatus").value("PENDING"))
                .andExpect(jsonPath("$.boxStatus").value("PENDING"))
                .andExpect(jsonPath("$.shippingGuideStatus").value("PENDING"))
                .andExpect(jsonPath("$.packagingStatus").value("PENDING"))
                .andExpect(jsonPath("$.carrierStatus").value("PENDING"))
                .andExpect(jsonPath("$.photos").isEmpty());
    }

    @Test void getsDetailWithPhotosAndFindsByItem() throws Exception {
        var photo = new PetCandlePhotoEntity(); photo.setId(UUID.randomUUID()); photo.setPetCandle(candle);
        photo.setPhotoUrl("https://example.com/luna.jpg"); photo.setIsPrimary(true); candle.getPhotos().add(photo);
        when(candles.findById(id)).thenReturn(Optional.of(candle));
        when(candles.findByOrderItemId(item.getId())).thenReturn(Optional.of(candle));
        for (var request : List.of(get(BASE + "/{id}", id), get(BASE + "/by-order-item/{id}", item.getId()))) {
            mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("$.petName").value("Luna"))
                    .andExpect(jsonPath("$.photos[0].petCandleId").value(id.toString()))
                    .andExpect(jsonPath("$.photos[0].isPrimary").value(true));
        }
    }

    @Test void updatesPersonalization() throws Exception {
        when(candles.findOrderIdById(id)).thenReturn(Optional.of(order.getId()));
        when(orders.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(candles.findById(id)).thenReturn(Optional.of(candle));
        when(candles.save(any())).thenAnswer(inv -> inv.getArgument(0));
        mvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petName\":\"Sol\",\"specie\":\"Dog\",\"breed\":\"Mix\",\"customMessage\":\"Love\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.petName").value("Sol"))
                .andExpect(jsonPath("$.specie").value("Dog")).andExpect(jsonPath("$.breed").value("Mix"))
                .andExpect(jsonPath("$.customMessage").value("Love"))
                .andExpect(jsonPath("$.baseStatus").value("PENDING"));
    }

    @Test void partialThenFinalPatchPropagatesThroughRealService() throws Exception {
        when(candles.findOrderIdById(id)).thenReturn(Optional.of(order.getId()));
        when(orders.findByIdForUpdate(order.getId())).thenReturn(Optional.of(order));
        when(candles.findById(id)).thenReturn(Optional.of(candle));
        mvc.perform(patch(BASE + "/{id}/statuses", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"baseStatus\":\"COMPLETED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.baseStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.headStatus").value("PENDING"));
        verify(items, never()).save(any());
        mvc.perform(patch(BASE + "/{id}/statuses", id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"headStatus":"AT_WORKSHOP_ONE","labelStatus":"AT_WORKSHOP_ONE",
                         "assemblyStatus":"ASSEMBLED","boxStatus":"AVAILABLE","shippingGuideStatus":"AT_WORKSHOP_ONE",
                         "packagingStatus":"PACKAGED","carrierStatus":"DELIVERED"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.carrierStatus").value("DELIVERED"));
        assertEquals(OrderItemStatus.COMPLETED, item.getStatus());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(items).save(item); verify(orders).save(order);
    }

    @ParameterizedTest
    @ValueSource(strings = {"baseStatus","headStatus","labelStatus","assemblyStatus","boxStatus",
            "shippingGuideStatus","packagingStatus","carrierStatus"})
    void invalidStatusReturnsProblem400(String field) throws Exception {
        mvc.perform(patch(BASE + "/{id}/statuses", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"" + field + "\":\"INVALID\"}"))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.detail").value("Invalid " + field + ": INVALID"));
        verifyNoInteractions(candles, items, orders);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{", "{\"orderItemId\":\"invalid\"}"})
    void invalidCreateReturns400(String body) throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(candles, items, orders);
    }

    @Test void invalidPathAndBlankOrOversizedNameReturn400() throws Exception {
        mvc.perform(get(BASE + "/invalid")).andExpect(status().isBadRequest());
        for (String name : List.of("", " ", "x".repeat(256))) {
            mvc.perform(put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"petName\":\"" + name + "\"}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.petName").exists());
        }
        verifyNoInteractions(candles);
    }

    @Test void missingReferencesReturn404() throws Exception {
        for (var request : List.of(get(BASE + "/{id}", id), get(BASE + "/by-order-item/{id}", id),
                post(BASE).contentType(MediaType.APPLICATION_JSON).content("{\"orderItemId\":\"" + id + "\"}"),
                put(BASE + "/{id}", id).contentType(MediaType.APPLICATION_JSON).content("{\"petName\":\"Luna\"}"),
                patch(BASE + "/{id}/statuses", id).contentType(MediaType.APPLICATION_JSON).content("{}"))) {
            mvc.perform(request).andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value("urn:problem:http:404"));
        }
    }
}
