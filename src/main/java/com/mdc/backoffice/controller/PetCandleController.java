package com.mdc.backoffice.controller;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.service.PetCandleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Invalid request",
        content = @Content(mediaType = "application/problem+json",
            schema = @Schema(implementation = ApiProblemDTO.class))),
    @ApiResponse(responseCode = "404", description = "Resource not found",
        content = @Content(mediaType = "application/problem+json",
            schema = @Schema(implementation = ApiProblemDTO.class)))
})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pet-candles")
public class PetCandleController {
    private final PetCandleService service;

    @Operation(operationId = "createPetCandle", summary = "Create PetCandle")
    @PostMapping
    public ResponseEntity<PetCandleResponseDTO> create(@Valid @RequestBody PetCandleCreateDTO request) {
        var response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(operationId = "getPetCandleById")
    @GetMapping("/{id}")
    public PetCandleResponseDTO findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @Operation(operationId = "getPetCandleByOrderItem")
    @GetMapping("/by-order-item/{orderItemId}")
    public PetCandleResponseDTO findByOrderItemId(@PathVariable UUID orderItemId) {
        return service.findByOrderItemId(orderItemId);
    }

    @Operation(operationId = "updatePetCandle")
    @PutMapping("/{id}")
    public PetCandleResponseDTO update(@PathVariable UUID id, @Valid @RequestBody PetCandleUpdateDTO request) {
        return service.update(id, request);
    }

    @Operation(operationId = "updatePetCandleStatuses")
    @PatchMapping("/{id}/statuses")
    public PetCandleResponseDTO updateStatuses(@PathVariable UUID id,
            @Valid @RequestBody PetCandleStatusUpdateDTO request) {
        return service.updateStatuses(id, request);
    }

}
