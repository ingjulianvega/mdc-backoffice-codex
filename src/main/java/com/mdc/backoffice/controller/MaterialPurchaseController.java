package com.mdc.backoffice.controller;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.service.MaterialPurchaseService;
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
@RequestMapping(value = "/api/v1/material-purchases", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Material purchases", description = "Compras de materiales e inventario")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Datos invalidos o stock inconsistente",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemDTO.class))),
    @ApiResponse(responseCode = "404", description = "Compra, proveedor o material inexistente",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemDTO.class)))
})
public class MaterialPurchaseController {
    private final MaterialPurchaseService service;

    @PostMapping
    @Operation(operationId = "createMaterialPurchase", summary = "Registrar compra e incrementar inventario")
    @ApiResponse(responseCode = "201", description = "Compra registrada")
    public ResponseEntity<MaterialPurchaseResponseDTO> create(@Valid @RequestBody MaterialPurchaseRequestDTO request) {
        var response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(operationId = "listMaterialPurchases", summary = "Listar compras por proveedor y fechas inclusivas")
    @ApiResponse(responseCode = "200", description = "Pagina de compras")
    public PageResponseDTO<MaterialPurchaseResponseDTO> findAll(
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @ParameterObject @PageableDefault(sort = "purchaseDate") Pageable pageable) {
        return PageResponseDTO.from(service.findAll(supplierId, startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getMaterialPurchaseById", summary = "Consultar compra con sus items")
    @ApiResponse(responseCode = "200", description = "Detalle de compra")
    public MaterialPurchaseResponseDTO findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteMaterialPurchase", summary = "Anular compra y descontar inventario")
    @ApiResponse(responseCode = "204", description = "Compra anulada", content = @Content)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
