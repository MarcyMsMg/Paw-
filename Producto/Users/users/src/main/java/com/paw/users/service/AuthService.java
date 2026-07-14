package com.paw.users.service;

import com.paw.users.dto.request.LoginRequest;
import com.paw.users.dto.request.NaturalPersonRegisterRequest;
import com.paw.users.dto.request.NgoRegisterRequest;
import com.paw.users.dto.response.AuthResponse;
import com.paw.users.dto.response.NgoRegistrationRequestResponse;

public interface AuthService {

    AuthResponse registerNaturalPerson(NaturalPersonRegisterRequest request);

    NgoRegistrationRequestResponse requestNgoRegistration(NgoRegisterRequest request);

    AuthResponse login(LoginRequest request);
}