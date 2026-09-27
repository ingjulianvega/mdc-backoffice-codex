package com.mdc.backoffice.service.impl;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.mapper.*;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import com.mdc.backoffice.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {
    @Mock AddressRepository repository;
    @Mock AddressMapper mapper;
    @Mock CityRepository cityRepository;
    @InjectMocks AddressServiceImpl service;
    final UUID id = UUID.randomUUID();
    final UUID customerId = UUID.randomUUID();
    final UUID cityId = UUID.randomUUID();
    final AddressRequestDTO request = new AddressRequestDTO("Medellin", null, cityId, customerId);
    final AddressResponseDTO response = new AddressResponseDTO(id, "Medellin", null, null, customerId, null, null);
    AddressEntity entity;

    @BeforeEach
    void setUp() {
        entity = new AddressEntity();
        entity.setId(id);
        entity.setStreetAddress("Medellin");
        entity.setCustomerId(customerId);
        var city = new CityEntity();
        city.setId(cityId);
        entity.setCity(city);
    }

    @Test
    void createSavesAndMapsResult() {
        when(cityRepository.findById(cityId)).thenReturn(Optional.of(entity.getCity()));
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);
        assertEquals(response, service.create(request));
        verify(repository).save(entity);
        verify(mapper).toEntity(request);
        verify(mapper).toResponse(entity);
    }

    @Test
    void findExistingIdMapsResult() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);
        assertEquals(response, service.findById(id));
    }

    @Test
    void missingIdThrowsNotFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
        verifyNoInteractions(mapper);
    }

    @Test
    void updateMapsAndSavesExistingEntity() {
        when(cityRepository.findById(cityId)).thenReturn(Optional.of(entity.getCity()));
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);
        assertEquals(response, service.update(id, request));
        verify(mapper).update(request, entity);
        verify(repository).save(entity);
    }

    @Test
    void updateMissingIdDoesNotSave() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(id, request));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteExistingEntity() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        service.delete(id);
        verify(repository).delete(entity);
    }

    @Test
    void deleteMissingIdDoesNotDelete() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
        verify(repository, never()).delete(any(AddressEntity.class));
    }

    @Test
    void listPassesPageableAndMapsPage() {
        var pageable = PageRequest.of(1, 2, Sort.by("streetAddress"));
        when(repository.findAll(ArgumentMatchers.<Specification<AddressEntity>>any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 3));
        when(mapper.toResponse(entity)).thenReturn(response);
        var result = service.findAll(cityId, customerId, pageable);
        assertEquals(List.of(response), result.getContent());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getNumber());
        verify(repository).findAll(ArgumentMatchers.<Specification<AddressEntity>>any(), eq(pageable));
    }
    @Test
    void missingCityDoesNotSave() {
        when(cityRepository.findById(cityId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void updateWithMissingCityDoesNotSave() {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(cityRepository.findById(cityId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(id, request));
        verify(repository, never()).save(any());
        verifyNoInteractions(mapper);
    }
    private AddressServiceImpl serviceWithRealMapper() {
        var realMapper = org.mapstruct.factory.Mappers.getMapper(AddressMapper.class);
        org.springframework.test.util.ReflectionTestUtils.setField(realMapper, "cityMapper",
                org.mapstruct.factory.Mappers.getMapper(CityMapper.class));
        return new AddressServiceImpl(repository, realMapper, cityRepository);
    }

    @Test
    void realMapperPreservesCustomerAndMapsNestedCityOnCreate() {
        when(cityRepository.findById(cityId)).thenReturn(Optional.of(entity.getCity()));
        when(repository.save(any(AddressEntity.class))).thenAnswer(invocation -> {
            AddressEntity saved = invocation.getArgument(0);
            assertNull(saved.getId());
            assertEquals(customerId, saved.getCustomerId());
            assertEquals("Medellin", saved.getStreetAddress());
            assertSame(entity.getCity(), saved.getCity());
            saved.setId(id);
            return saved;
        });
        var result = serviceWithRealMapper().create(request);
        assertEquals(id, result.id());
        assertEquals(customerId, result.customerId());
        assertEquals(cityId, result.city().id());
    }

    @Test
    void realMapperAllowsCreationWithoutAdditionalInfo() {
        when(cityRepository.findById(cityId)).thenReturn(Optional.of(entity.getCity()));
        when(repository.save(any(AddressEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var result = serviceWithRealMapper().create(
                new AddressRequestDTO("Calle", null, cityId, customerId));
        assertEquals(customerId, result.customerId());
        assertNull(result.additionalInfo());
    }

    @Test
    void realMapperReplacesCityAndCustomerWhilePreservingAuditAndId() {
        var createdAt = java.time.Instant.parse("2026-01-01T00:00:00Z");
        entity.setCreatedAt(createdAt);
        entity.setCreatedBy("original");
        entity.setUpdatedAt(createdAt);
        entity.setUpdatedBy("original");
        var newCity = new CityEntity();
        newCity.setId(UUID.randomUUID());
        var newCustomer = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(cityRepository.findById(newCity.getId())).thenReturn(Optional.of(newCity));
        when(repository.save(entity)).thenReturn(entity);
        var result = serviceWithRealMapper().update(id,
                new AddressRequestDTO("Carrera 5 # 10A", "Casa", newCity.getId(), newCustomer));
        assertEquals(id, result.id());
        assertEquals("Carrera 5 # 10A", result.streetAddress());
        assertEquals("Casa", result.additionalInfo());
        assertEquals(newCustomer, result.customerId());
        assertEquals(newCity.getId(), result.city().id());
        assertEquals(createdAt, result.createdAt());
        assertEquals(createdAt, result.updatedAt());
        assertEquals("original", entity.getCreatedBy());
        assertEquals("original", entity.getUpdatedBy());
        verify(repository).flush();
    }

    @Test
    void realMapperClearsOptionalFieldsOnPut() {
        entity.setAdditionalInfo("Casa");
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(cityRepository.findById(cityId)).thenReturn(Optional.of(entity.getCity()));
        when(repository.save(entity)).thenReturn(entity);
        var result = serviceWithRealMapper().update(id,
                new AddressRequestDTO("Calle", null, cityId, customerId));
        assertEquals(customerId, result.customerId());
        assertNull(result.additionalInfo());
    }

    @Test
    void unfilteredEmptyPageIsSupported() {
        var pageable = PageRequest.of(0, 20);
        when(repository.findAll(ArgumentMatchers.<Specification<AddressEntity>>any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));
        assertTrue(service.findAll(null, null, pageable).isEmpty());
        verifyNoInteractions(mapper, cityRepository);
    }
}
