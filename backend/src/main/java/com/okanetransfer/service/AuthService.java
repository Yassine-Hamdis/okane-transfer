package com.okanetransfer.service;

import com.okanetransfer.dto.request.LoginRequest;
import com.okanetransfer.dto.request.RegisterRequest;
import com.okanetransfer.dto.request.Verify2FaRequest;
import com.okanetransfer.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    void register(RegisterRequest request);
    AuthResponse verify2fa(Verify2FaRequest request);
}