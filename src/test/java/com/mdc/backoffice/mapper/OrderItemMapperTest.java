package com.mdc.backoffice.mapper;

import com.mdc.backoffice.model.entity.OrderItemEntity;
import com.mdc.backoffice.model.enum_.OrderItemStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderItemMapperTest {
    private final OrderItemMapper mapper = Mappers.getMapper(OrderItemMapper.class);

    @ParameterizedTest
    @EnumSource(OrderItemStatus.class)
    void exposesPersistedItemStatus(OrderItemStatus status) {
        var entity = new OrderItemEntity();
        entity.setStatus(status);
        assertEquals(status.name(), mapper.toResponse(entity).status());
    }
}
