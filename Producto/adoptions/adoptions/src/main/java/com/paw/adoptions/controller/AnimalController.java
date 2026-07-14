package com.paw.adoptions.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paw.adoptions.common.ApiResponse;
import com.paw.adoptions.domain.AnimalStatus;
import com.paw.adoptions.dto.AnimalResponse;
import com.paw.adoptions.dto.AdoptionFormResponse;
import com.paw.adoptions.service.AdoptionFormService;
import com.paw.adoptions.service.AnimalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/adoptions/animals")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;
    private final AdoptionFormService formService;

    @GetMapping
    public ApiResponse<List<AnimalResponse>> list(
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) AnimalStatus status,
            @RequestParam(required = false) UUID ngoId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String age
    ) {
        return ApiResponse.ok(animalService.listPublic(species, size, status, ngoId, location, age));
    }

    @GetMapping("/{animalId}")
    public ApiResponse<AnimalResponse> findById(@PathVariable UUID animalId) {
        return ApiResponse.ok(animalService.getPublic(animalId));
    }

    @GetMapping("/{animalId}/form")
    public ApiResponse<AdoptionFormResponse> getAdoptionForm(@PathVariable UUID animalId) {
        return ApiResponse.ok(formService.getForAnimal(animalId));
    }
}
