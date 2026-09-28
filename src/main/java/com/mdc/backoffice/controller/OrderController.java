package com.mdc.backoffice.controller;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Orders", description = "Pedidos e inventario")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Datos invalidos o stock inconsistente",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemDTO.class))),
    @ApiResponse(responseCode = "404", description = "Pedido, cliente, producto o direccion inexistente",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemDTO.class)))
})
public class OrderController {
    private final OrderService service;

    @PostMapping
    @Operation(operationId = "createOrder", summary = "Registrar pedido y descontar inventario")
    @ApiResponse(responseCode = "201", description = "Pedido registrado")
    public ResponseEntity<OrderResponseDTO> create(@Valid @RequestBody OrderRequestDTO request) {
        var response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(operationId = "listOrders", summary = "Listar pedidos por cliente y fechas inclusivas")
    @ApiResponse(responseCode = "200", description = "Pagina de pedidos")
    public PageResponseDTO<OrderResponseDTO> findAll(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @ParameterObject @PageableDefault(sort = "orderDate") Pageable pageable) {
        return PageResponseDTO.from(service.findAll(customerId, startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getOrderById", summary = "Consultar pedido con sus items")
    @ApiResponse(responseCode = "200", description = "Detalle de pedido")
    public OrderResponseDTO findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteOrder", summary = "Eliminar pedido y reponer inventario")
    @ApiResponse(responseCode = "204", description = "Pedido eliminado", content = @Content)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
