package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateUserRequest;
import com.okanetransfer.dto.request.UpdateUserRequest;
import com.okanetransfer.dto.response.UserResponse;
import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.AgencyRepository;
import com.okanetransfer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired private UserRepository    userRepository;
    @Autowired private AgencyRepository  agencyRepository;
    @Autowired private PasswordEncoder   passwordEncoder;
    @Autowired private AuditService      auditService;

    // ─────────────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findUser(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByAgency(Long agencyId) {
        return userRepository.findAllByAgencyId(agencyId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findAllByRole(role)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────────────

    @Transactional
    public UserResponse createUser(CreateUserRequest request, Long createdById) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: "
                    + request.getEmail());
        }

        // Agents and managers must have an agency
        if ((request.getRole() == Role.ROLE_AGENT ||
                request.getRole() == Role.ROLE_MANAGER)
                && request.getAgencyId() == null) {
            throw new IllegalArgumentException(
                    "Agency is required for AGENT and MANAGER roles");
        }

        Agency agency = null;
        if (request.getAgencyId() != null) {
            agency = agencyRepository.findById(request.getAgencyId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Agency", request.getAgencyId()));
        }

        User createdBy = findUser(createdById);

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .agency(agency)
                .createdBy(createdBy)
                .active(true)
                .twoFactorEnabled(false)
                .mustChangePassword(true)
                .build();

        User saved = userRepository.save(user);

        auditService.log(createdById, "USER_CREATED",
                "User", saved.getId(),
                "{\"role\":\"" + request.getRole() + "\"}");

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request,
                                   Long updatedById) {
        User user = findUser(id);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        auditService.log(updatedById, "USER_UPDATED", "User", id, null);
        return toResponse(user);
    }

    // ─────────────────────────────────────────────────────
    //  SUSPEND / ACTIVATE
    // ─────────────────────────────────────────────────────

    @Transactional
    public void suspendUser(Long id, Long adminId) {
        User user = findUser(id);
        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new IllegalArgumentException("Cannot suspend an admin user");
        }
        user.setActive(false);
        userRepository.save(user);
        auditService.log(adminId, "USER_SUSPENDED", "User", id, null);
    }

    @Transactional
    public void activateUser(Long id, Long adminId) {
        User user = findUser(id);
        user.setActive(true);
        userRepository.save(user);
        auditService.log(adminId, "USER_ACTIVATED", "User", id, null);
    }

    // ─────────────────────────────────────────────────────
    //  CHANGE PASSWORD (by admin)
    // ─────────────────────────────────────────────────────

    @Transactional
    public void resetPassword(Long id, String newPassword, Long adminId) {
        User user = findUser(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);
        auditService.log(adminId, "PASSWORD_RESET", "User", id, null);
    }

    // ─────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole())
                .active(u.isActive())
                .twoFactorEnabled(u.isTwoFactorEnabled())
                .mustChangePassword(u.isMustChangePassword())
                .agencyId(u.getAgency() != null ? u.getAgency().getId() : null)
                .agencyName(u.getAgency() != null ? u.getAgency().getName() : null)
                .createdAt(u.getCreatedAt())
                .build();
    }
}