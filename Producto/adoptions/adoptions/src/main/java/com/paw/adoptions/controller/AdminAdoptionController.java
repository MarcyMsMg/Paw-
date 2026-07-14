package com.paw.adoptions.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.adoptions.common.ApiResponse;
import com.paw.adoptions.dto.AnimalResponse;
import com.paw.adoptions.service.AnimalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/adoptions/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAdoptionController {

    private final AnimalService animalService;

    @GetMapping("/animals")
    public ApiResponse<List<AnimalResponse>> listAllAnimals() {
        return ApiResponse.ok(animalService.listAllForAdmin());
    }
}
