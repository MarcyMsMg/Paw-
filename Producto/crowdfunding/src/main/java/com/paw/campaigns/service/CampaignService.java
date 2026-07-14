package com.paw.campaigns.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.paw.campaigns.dto.request.CreateCampaignRequest;
import com.paw.campaigns.dto.request.UpdateCampaignRequest;
import com.paw.campaigns.dto.response.CampaignResponse;
import com.paw.campaigns.dto.response.CampaignStatsResponse;

public interface CampaignService {

    List<CampaignResponse> findAllActive();

    List<CampaignResponse> findByNgo(UUID ngoId);

    CampaignResponse findById(UUID id);

    CampaignResponse create(CreateCampaignRequest request);

    CampaignResponse update(UUID id, UpdateCampaignRequest request);

    CampaignResponse finish(UUID id);

    CampaignResponse addRaisedAmount(UUID id, BigDecimal amount);

    CampaignResponse subtractRaisedAmount(UUID id, BigDecimal amount);

    CampaignStatsResponse statsForNgo(UUID ngoId);

    CampaignStatsResponse statsForAdmin();
}