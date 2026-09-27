package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.model.entity.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.*;

class DepartmentMapperTest {
    private final DepartmentMapper mapper = Mappers.getMapper(DepartmentMapper.class);
    private final UUID departmentId = UUID.randomUUID();

    @Test
    void requestMapsToEntity() {
        var entity = mapper.toEntity(new DepartmentRequestDTO("Antioquia"));
        assertEquals("Antioquia", entity.getName());
        assertNull(entity.getId());
        assertNull(entity.getCreatedAt());

    }

    @Test
    void entityMapsToRecordIncludingAudit() {
        var entity = new DepartmentEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Antioquia");
        entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2026-02-01T00:00:00Z"));
        entity.setCreatedBy("creator");
        entity.setUpdatedBy("editor");

        var response = mapper.toResponse(entity);
        assertEquals(entity.getId(), response.id());
        assertEquals(entity.getName(), response.name());
        assertEquals(entity.getCreatedAt(), response.createdAt());
        assertEquals(entity.getUpdatedAt(), response.updatedAt());
        assertEquals("creator", response.createdBy());
        assertEquals("editor", response.updatedBy());

    }

    @Test
    void updatePreservesIdentityAndAudit() {
        var entity = new DepartmentEntity();
        var id = UUID.randomUUID();
        var created = Instant.now();
        entity.setId(id);
        entity.setName("Old");
        entity.setCreatedAt(created);
        entity.setCreatedBy("creator");
        entity.setUpdatedAt(created);
        entity.setUpdatedBy("editor");
        mapper.update(new DepartmentRequestDTO("Antioquia"), entity);
        assertEquals("Antioquia", entity.getName());
        assertEquals(id, entity.getId());
        assertEquals(created, entity.getCreatedAt());
        assertEquals("creator", entity.getCreatedBy());
        assertEquals(created, entity.getUpdatedAt());
        assertEquals("editor", entity.getUpdatedBy());
    }
}
