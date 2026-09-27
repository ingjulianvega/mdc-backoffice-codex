package com.mdc.backoffice;

import com.mdc.backoffice.exception.ResourceNotFoundException;
import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.repository.*;
import com.mdc.backoffice.service.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GeographyIntegrationTest {
    @Autowired DepartmentService departments;
    @Autowired CityService cities;
    @Autowired CityRepository cityRepository;
    @Autowired DepartmentRepository departmentRepository;

    @BeforeEach
    void cleanDatabase() {
        cityRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    @Test
    void persistsRelationshipAuditAndFiltersWithPagination() {
        var first = departments.create(new DepartmentRequestDTO("Antioquia"));
        var second = departments.create(new DepartmentRequestDTO("Cundinamarca"));
        var medellin = cities.create(new CityRequestDTO("Medellin", first.id()));
        cities.create(new CityRequestDTO("Bello", first.id()));
        cities.create(new CityRequestDTO("Bogota", second.id()));

        var stored = cities.findById(medellin.id());
        assertEquals(first.id(), stored.departmentId());
        assertNotNull(stored.createdAt());
        assertNotNull(stored.updatedAt());
        assertEquals("system", stored.createdBy());
        assertEquals("system", stored.updatedBy());
        assertNotNull(departments.findById(first.id()).createdAt());

        var filtered = cities.findAll(first.id(), PageRequest.of(0, 1, Sort.by("name")));
        assertEquals(2, filtered.getTotalElements());
        assertEquals("Bello", filtered.getContent().getFirst().name());
        assertEquals("Medellin", cities.findAll(first.id(), PageRequest.of(1, 1, Sort.by("name")))
                .getContent().getFirst().name());
        assertEquals(3, cities.findAll(null, Pageable.unpaged()).getTotalElements());
        assertTrue(cities.findAll(UUID.randomUUID(), Pageable.unpaged()).isEmpty());
        assertEquals(2, departments.findAll(PageRequest.of(0, 1)).getTotalElements());
    }

    @Test
    void updatesAndDeletesWithoutChangingCreationAudit() {
        var first = departments.create(new DepartmentRequestDTO("First"));
        var second = departments.create(new DepartmentRequestDTO("Second"));
        var city = cities.create(new CityRequestDTO("Old", first.id()));
        var original = cities.findById(city.id());
        cities.update(city.id(), new CityRequestDTO("New", second.id()));
        var updated = cities.findById(city.id());
        assertEquals("New", updated.name());
        assertEquals(second.id(), updated.departmentId());
        assertEquals(original.createdAt(), updated.createdAt());
        assertEquals(original.createdBy(), updated.createdBy());

        departments.update(first.id(), new DepartmentRequestDTO("Renamed"));
        assertEquals("Renamed", departments.findById(first.id()).name());
        cities.delete(city.id());
        assertThrows(ResourceNotFoundException.class, () -> cities.findById(city.id()));
        departments.delete(first.id());
        assertThrows(ResourceNotFoundException.class, () -> departments.findById(first.id()));
    }

    @Test
    void referencedDepartmentCannotBeDeleted() {
        var department = departments.create(new DepartmentRequestDTO("Parent"));
        var city = cities.create(new CityRequestDTO("Child", department.id()));
        assertThrows(DataIntegrityViolationException.class, () -> departments.delete(department.id()));
        assertEquals(department.id(), cities.findById(city.id()).departmentId());
        assertEquals(department.id(), departments.findById(department.id()).id());
    }
}
