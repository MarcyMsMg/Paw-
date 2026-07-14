package com.paw.adoptions.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.adoptions.common.ApiResponse;
import com.paw.adoptions.dto.AdoptionApplicationCreateRequest;
import com.paw.adoptions.dto.AdoptionApplicationDecisionRequest;
import com.paw.adoptions.dto.AdoptionApplicationResponse;
import com.paw.adoptions.facade.AdoptionFacade;
import com.paw.adoptions.service.AdoptionApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/adoptions")
@RequiredArgsConstructor
public class AdoptionApplicationController {

    private final AdoptionFacade adoptionFacade;
    private final AdoptionApplicationService applicationService;

    @PostMapping("/animals/{animalId}/applications")
    @PreAuthorize("hasRole('NATURAL_PERSON')")
    public ResponseEntity<ApiResponse<AdoptionApplicationResponse>> submit(
            @PathVariable UUID animalId,
            @Valid @RequestBody AdoptionApplicationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Adoption application submitted",
                        adoptionFacade.submitApplication(animalId, request)
                ));
    }

    @GetMapping("/applications")
    public ApiResponse<List<AdoptionApplicationResponse>> listForCurrentUser() {
        return ApiResponse.ok(applicationService.listForCurrentUser());
    }

    @PatchMapping("/applications/{applicationId}/decision")
    @PreAuthorize("hasRole('NGO')")
    public ApiResponse<AdoptionApplicationResponse> decide(
            @PathVariable UUID applicationId,
            @Valid @RequestBody AdoptionApplicationDecisionRequest request
    ) {
        return ApiResponse.ok(
                "Adoption application updated",
                adoptionFacade.decideApplication(applicationId, request)
        );
    }
}
