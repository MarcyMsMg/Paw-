package com.paw.users.dto.response;

import java.util.Map;

public record AdminStatsResponse(
        long totalUsers,
        long activeUsers,
        long pendingNgoRequests,
        long activeNgos,
        long naturalPersons,
        Map<String, Long> usersByRole,
        Map<String, Long> usersByStatus
) {
}