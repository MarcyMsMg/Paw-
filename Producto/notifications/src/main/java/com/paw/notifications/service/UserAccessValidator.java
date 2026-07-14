package com.paw.notifications.service;

import org.springframework.stereotype.Service;

import com.paw.notifications.client.UserAccessResponse;
import com.paw.notifications.client.UsersClient;
import com.paw.notifications.domain.AccountStatus;
import com.paw.notifications.exception.ApiException;
import com.paw.notifications.security.AuthenticatedUser;

@Service
public class UserAccessValidator {
    private final UsersClient usersClient;

    public UserAccessValidator(UsersClient usersClient) {
        this.usersClient = usersClient;
    }

    public UserAccessResponse requireActive(AuthenticatedUser authenticatedUser) {
        UserAccessResponse user = usersClient.findAccess(authenticatedUser.id());
        if (user.status() != AccountStatus.ACTIVE) {
            throw ApiException.forbidden("The user account is not active");
        }
        if (user.role() != authenticatedUser.role()) {
            throw ApiException.unauthorized("The token role no longer matches the user account");
        }
        return user;
    }
}