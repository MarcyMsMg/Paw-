package com.paw.adoptions.strategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.paw.adoptions.exception.ApiException;
import com.paw.adoptions.security.UserRole;

@Component
public class ApplicationListingStrategyResolver {

    private final Map<UserRole, ApplicationListingStrategy> strategies = new EnumMap<>(UserRole.class);

    public ApplicationListingStrategyResolver(List<ApplicationListingStrategy> availableStrategies) {
        for (ApplicationListingStrategy strategy : availableStrategies) {
            strategies.put(strategy.supportedRole(), strategy);
        }
    }

    public ApplicationListingStrategy resolve(UserRole role) {
        ApplicationListingStrategy strategy = strategies.get(role);
        if (strategy == null) {
            throw ApiException.forbidden("No application listing strategy exists for this role");
        }
        return strategy;
    }
}
