package com.paw.campaigns.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.paw.campaigns.dto.request.CreateCampaignRequest;
import com.paw.campaigns.dto.request.RaiseAmountRequest;
import com.paw.campaigns.dto.request.UpdateCampaignRequest;
import com.paw.campaigns.dto.response.ApiResponse;
import com.paw.campaigns.dto.response.CampaignResponse;
import com.paw.campaigns.dto.response.CampaignStatsResponse;
import com.paw.campaigns.service.CampaignService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    // Clave compartida para autenticar llamadas internas (donations -> campaigns).
    @Value("${paw.security.internal-api-key:}")
    private String internalApiKey;

    @GetMapping
    public ApiResponse<List<CampaignResponse>> findAll(
            @RequestParam(required = false) UUID ngoId
    ) {
        List<CampaignResponse> data = (ngoId != null)
                ? campaignService.findByNgo(ngoId)
                : campaignService.findAllActive();

        return new ApiResponse<>(true, "Campanas encontradas", data);
    }

    @GetMapping("/{id}")
    public ApiResponse<CampaignResponse> findById(@PathVariable UUID id) {
        return new ApiResponse<>(true, "Campana encontrada", campaignService.findById(id));
    }

    @GetMapping("/stats/ngo")
    @PreAuthorize("hasRole('NGO')")
    public ApiResponse<CampaignStatsResponse> statsForNgo(@RequestParam UUID ngoId) {
        return new ApiResponse<>(true, "Estadisticas de campanas encontradas", campaignService.statsForNgo(ngoId));
    }

    @GetMapping("/stats/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CampaignStatsResponse> statsForAdmin() {
        return new ApiResponse<>(true, "Estadisticas de campanas encontradas", campaignService.statsForAdmin());
    }

    @PostMapping
    @PreAuthorize("hasRole('NGO')")
    public ApiResponse<CampaignResponse> create(
            @Valid @RequestBody CreateCampaignRequest request
    ) {
        return new ApiResponse<>(true, "Campana creada correctamente", campaignService.create(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('NGO')")
    public ApiResponse<CampaignResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCampaignRequest request
    ) {
        return new ApiResponse<>(true, "Campana actualizada correctamente", campaignService.update(id, request));
    }

    @PatchMapping("/{id}/finish")
    @PreAuthorize("hasAnyRole('NGO', 'ADMIN')")
    public ApiResponse<CampaignResponse> finish(@PathVariable UUID id) {
        return new ApiResponse<>(true, "Campana finalizada correctamente", campaignService.finish(id));
    }

    @PatchMapping("/{id}/raise")
    public ApiResponse<CampaignResponse> raise(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String providedApiKey,
            @Valid @RequestBody RaiseAmountRequest request
    ) {
        validateInternalKey(providedApiKey);
        return new ApiResponse<>(
                true,
                "Monto recaudado actualizado",
                campaignService.addRaisedAmount(id, request.amount())
        );
    }

    @PatchMapping("/{id}/lower")
    public ApiResponse<CampaignResponse> lower(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Internal-Api-Key", required = false) String providedApiKey,
            @Valid @RequestBody RaiseAmountRequest request
    ) {
        validateInternalKey(providedApiKey);
        return new ApiResponse<>(
                true,
                "Monto recaudado descontado",
                campaignService.subtractRaisedAmount(id, request.amount())
        );
    }

    // Autentica las llamadas internas servicio-a-servicio comparando la clave en
    // tiempo constante. Sin clave configurada o inválida => 401.
    private void validateInternalKey(String providedApiKey) {
        if (internalApiKey == null || internalApiKey.isBlank()
                || providedApiKey == null
                || !MessageDigest.isEqual(
                        internalApiKey.getBytes(StandardCharsets.UTF_8),
                        providedApiKey.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid internal API key");
        }
    }
}
