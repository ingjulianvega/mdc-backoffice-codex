package com.mdc.backoffice.controller;

import com.mdc.backoffice.model.dto.*;
import com.mdc.backoffice.service.PetCandlePhotoService;
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
@RequestMapping("/api/v1/pet-candle-photos")
public class PetCandlePhotoController {
    private final PetCandlePhotoService service;

    @Operation(operationId = "createPetCandlePhoto", summary = "Create PetCandlePhoto")
    @PostMapping
    public ResponseEntity<PetCandlePhotoResponseDTO> create(@Valid @RequestBody PetCandlePhotoRequestDTO request) {
        var response = service.create(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(operationId = "listPetCandlePhotos")
    @GetMapping("/by-pet-candle/{petCandleId}")
    public List<PetCandlePhotoResponseDTO> findByPetCandleId(@PathVariable UUID petCandleId) {
        return service.findByPetCandleId(petCandleId);
    }

    @Operation(operationId = "deletePetCandlePhoto")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

}
