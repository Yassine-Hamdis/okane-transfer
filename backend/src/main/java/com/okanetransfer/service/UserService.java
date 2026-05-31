package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateUserRequest;
import com.okanetransfer.dto.request.UpdateUserRequest;
import com.okanetransfer.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void suspendUser(Long id);
    void activateUser(Long id);
    List<UserResponse> getUsersByAgency(Long agencyId);
}