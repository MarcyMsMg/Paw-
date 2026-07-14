package com.paw.adoptions.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.adoptions.common.ApiResponse;
import com.paw.adoptions.dto.AnimalCreateRequest;
import com.paw.adoptions.dto.AnimalResponse;
import com.paw.adoptions.dto.AnimalUpdateRequest;
import com.paw.adoptions.service.AnimalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/adoptions/ngo/animals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NGO')")
public class NgoAnimalController {

    private final AnimalService animalService;

    @GetMapping
    public ApiResponse<List<AnimalResponse>> listOwned() {
        return ApiResponse.ok(animalService.listOwned());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AnimalResponse>> create(
            @Valid @RequestBody AnimalCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Animal created", animalService.create(request)));
    }

    @PutMapping("/{animalId}")
    public ApiResponse<AnimalResponse> update(
            @PathVariable UUID animalId,
            @Valid @RequestBody AnimalUpdateRequest request
    ) {
        return ApiResponse.ok("Animal updated", animalService.update(animalId, request));
    }

    @DeleteMapping("/{animalId}")
    public ApiResponse<Void> retire(@PathVariable UUID animalId) {
        animalService.retire(animalId);
        return ApiResponse.ok("Animal retired", null);
    }
}
