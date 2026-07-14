package com.paw.users.controller;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paw.users.dto.response.AdminStatsResponse;
import com.paw.users.dto.response.ApiResponse;
import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.NgoRegistrationStatus;
import com.paw.users.enums.UserRole;
import com.paw.users.model.User;
import com.paw.users.repository.NgoRegistrationRequestRepository;
import com.paw.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final UserRepository userRepository;
    private final NgoRegistrationRequestRepository ngoRegistrationRequestRepository;

    @GetMapping("/stats")
    public ApiResponse<AdminStatsResponse> stats() {
        var users = userRepository.findAll();
        Map<String, Long> usersByRole = toStatusMap(
                users.stream().collect(Collectors.groupingBy(User::getRole, () -> new EnumMap<>(UserRole.class), Collectors.counting())),
                UserRole.values()
        );
        Map<String, Long> usersByStatus = toStatusMap(
                users.stream().collect(Collectors.groupingBy(User::getStatus, () -> new EnumMap<>(AccountStatus.class), Collectors.counting())),
                AccountStatus.values()
        );

        long pendingNgoRequests = ngoRegistrationRequestRepository.findAll().stream()
                .filter(request -> request.getStatus() == NgoRegistrationStatus.PENDING)
                .count();

        return new ApiResponse<>(true, "Estadisticas encontradas", new AdminStatsResponse(
                users.size(),
                usersByStatus.getOrDefault(AccountStatus.ACTIVE.name(), 0L),
                pendingNgoRequests,
                users.stream().filter(user -> user.getRole() == UserRole.NGO && user.getStatus() == AccountStatus.ACTIVE).count(),
                usersByRole.getOrDefault(UserRole.NATURAL_PERSON.name(), 0L),
                usersByRole,
                usersByStatus
        ));
    }

    private <E extends Enum<E>> Map<String, Long> toStatusMap(Map<E, Long> source, E[] values) {
        return java.util.Arrays.stream(values)
                .collect(Collectors.toMap(Enum::name, value -> source.getOrDefault(value, 0L), (a, b) -> a, java.util.LinkedHashMap::new));
    }
}