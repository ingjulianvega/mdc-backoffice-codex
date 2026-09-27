package com.mdc.backoffice.controller;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.service.CityService;
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

@Tag(name = "Cities", description = "Gestion de ciudades")
@RestController
@RequestMapping(value = "/api/v1/cities", produces = MediaType.APPLICATION_JSON_VALUE)
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
public class CityController {
    private final CityService service;

    @Operation(operationId = "createCity", summary = "Crear ciudad")
    @ApiResponse(responseCode = "201", description = "Recurso creado")
    @PostMapping
    public ResponseEntity<CityResponseDTO> create(@Valid @RequestBody CityRequestDTO request) {
        var response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(operationId = "listCities", summary = "Listar con paginacion")
    @ApiResponse(responseCode = "200", description = "Pagina de resultados")
    @GetMapping
    public PageResponseDTO<CityResponseDTO> findAll(@RequestParam(required = false) UUID departmentId, @ParameterObject @PageableDefault(sort = "name") Pageable pageable) {
        return PageResponseDTO.from(service.findAll(departmentId, pageable));
    }

    @Operation(operationId = "getCityById", summary = "Consultar por UUID")
    @ApiResponse(responseCode = "200", description = "Recurso encontrado")
    @GetMapping("/{id}")
    public CityResponseDTO findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(operationId = "updateCity", summary = "Actualizar ciudad")
    @ApiResponse(responseCode = "200", description = "Recurso actualizado")
    @PutMapping("/{id}")
    public CityResponseDTO update(@PathVariable UUID id, @Valid @RequestBody CityRequestDTO request) {
        return service.update(id, request);
    }

    @Operation(operationId = "deleteCity", summary = "Eliminar ciudad")
    @ApiResponse(responseCode = "204", description = "Recurso eliminado", content = @Content)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
