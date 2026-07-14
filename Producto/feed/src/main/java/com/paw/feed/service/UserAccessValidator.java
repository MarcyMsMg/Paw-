package com.paw.feed.service;

import org.springframework.stereotype.Service;

import com.paw.feed.client.UserAccessResponse;
import com.paw.feed.client.UsersClient;
import com.paw.feed.exception.ApiException;
import com.paw.feed.security.AccountStatus;
import com.paw.feed.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserAccessValidator {

    private final UsersClient usersClient;

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
