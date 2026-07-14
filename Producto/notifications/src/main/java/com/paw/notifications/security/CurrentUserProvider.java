package com.paw.notifications.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.paw.notifications.exception.ApiException;

@Component
public class CurrentUserProvider {
    public AuthenticatedUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw ApiException.unauthorized("Authentication required");
        }
        return user;
    }
}