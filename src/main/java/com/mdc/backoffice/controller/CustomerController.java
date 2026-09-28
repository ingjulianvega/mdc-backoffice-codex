package com.mdc.backoffice.controller;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Customers", description = "Gestion del catalogo")
@RestController
@RequestMapping(value = "/api/v1/customers", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Peticion invalida",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemDTO.class))),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemDTO.class))),
    @ApiResponse(responseCode = "409", description = "Conflicto de integridad referencial",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemDTO.class))),
    @ApiResponse(responseCode = "500", description = "Error interno",
        content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblemDTO.class)))
})
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService service;

    @Operation(operationId = "createCustomer", summary = "Crear recurso")
    @ApiResponse(responseCode = "201", description = "Recurso creado")
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@Valid @RequestBody CustomerRequestDTO request) {
        var response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(operationId = "listCustomers", summary = "Listar con paginacion")
    @ApiResponse(responseCode = "200", description = "Pagina de resultados")
    @GetMapping
    public PageResponseDTO<CustomerResponseDTO> findAll(@RequestParam(required = false) String name, @RequestParam(required = false) String email, @RequestParam(required = false) String documentNumber, @ParameterObject @PageableDefault(sort = "firstName") Pageable pageable) {
        return PageResponseDTO.from(service.findAll(name, email, documentNumber, pageable));
    }

    @Operation(operationId = "getCustomerById", summary = "Consultar por UUID")
    @ApiResponse(responseCode = "200", description = "Recurso encontrado")
    @GetMapping("/{id}")
    public CustomerResponseDTO findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(operationId = "updateCustomer", summary = "Actualizar recurso")
    @ApiResponse(responseCode = "200", description = "Recurso actualizado")
    @PutMapping("/{id}")
    public CustomerResponseDTO update(@PathVariable UUID id, @Valid @RequestBody CustomerRequestDTO request) {
        return service.update(id, request);
    }

    @Operation(operationId = "deleteCustomer", summary = "Eliminar recurso")
    @ApiResponse(responseCode = "204", description = "Recurso eliminado", content = @Content)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
