package com.paw.adoptions.controller;

import com.paw.adoptions.common.ApiResponse;
import com.paw.adoptions.dto.FormTemplateRequest;
import com.paw.adoptions.dto.FormTemplateResponse;
import com.paw.adoptions.service.AdoptionFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/adoptions/ngo/form-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NGO')")
public class AdoptionFormTemplateController {

    private final AdoptionFormService formService;

    @GetMapping
    public ApiResponse<List<FormTemplateResponse>> list() {
        return ApiResponse.ok(formService.listOwned());
    }

    @GetMapping("/{templateId}")
    public ApiResponse<FormTemplateResponse> get(@PathVariable UUID templateId) {
        return ApiResponse.ok(formService.getOwned(templateId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FormTemplateResponse>> create(
            @Valid @RequestBody FormTemplateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Adoption form template created", formService.create(request)));
    }

    @PutMapping("/{templateId}")
    public ApiResponse<FormTemplateResponse> update(
            @PathVariable UUID templateId,
            @Valid @RequestBody FormTemplateRequest request
    ) {
        return ApiResponse.ok("Adoption form template updated", formService.update(templateId, request));
    }

    @DeleteMapping("/{templateId}")
    public ApiResponse<Void> deactivate(@PathVariable UUID templateId) {
        formService.deactivate(templateId);
        return ApiResponse.ok("Adoption form template deactivated", null);
    }
}
