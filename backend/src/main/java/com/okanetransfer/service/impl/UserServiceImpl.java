package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.CreateUserRequest;
import com.okanetransfer.dto.request.UpdateUserRequest;
import com.okanetransfer.dto.response.UserResponse;
import com.okanetransfer.entity.Agency;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.exception.BadRequestException;
import com.okanetransfer.exception.ConflictException;
import com.okanetransfer.exception.NotFoundException;
import com.okanetransfer.repository.AgencyRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public UserServiceImpl(UserRepository userRepository, AgencyRepository agencyRepository) {
        this.userRepository = userRepository;
        this.agencyRepository = agencyRepository;
    }

    private static UserResponse toResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setFirstName(u.getFirstName());
        r.setLastName(u.getLastName());
        r.setEmail(u.getEmail());
        r.setPhone(u.getPhone());
        r.setRole(u.getRole());
        r.setActive(u.isActive());
        r.setTwoFactorEnabled(u.isTwoFactorEnabled());
        r.setAgencyId(u.getAgency() != null ? u.getAgency().getId() : null);
        return r;
    }

    private void validateRoleAgency(Role role, Long agencyId) {
        if ((role == Role.ROLE_AGENT || role == Role.ROLE_MANAGER) && agencyId == null) {
            throw new BadRequestException("agencyId is required for ROLE_AGENT / ROLE_MANAGER");
        }
    }

    private User getCurrentUserOrNull() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(UserServiceImpl::toResponse).toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id)));
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new ConflictException("Email already used");
        }

        validateRoleAgency(request.getRole(), request.getAgencyId());

        Agency agency = null;
        if (request.getAgencyId() != null) {
            agency = agencyRepository.findById(request.getAgencyId())
                    .orElseThrow(() -> new NotFoundException("Agency not found: " + request.getAgencyId()));
        }

        // Pour ADMIN/CLIENT => agency null
        if (request.getRole() == Role.ROLE_ADMIN || request.getRole() == Role.ROLE_CLIENT) {
            agency = null;
        }

        User creator = getCurrentUserOrNull();

        User u = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .agency(agency)
                .createdBy(creator)
                .active(true)
                .twoFactorEnabled(false)
                .build();

        userRepository.save(u);
        return toResponse(u);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        Agency agency = null;
        if (request.getAgencyId() != null) {
            agency = agencyRepository.findById(request.getAgencyId())
                    .orElseThrow(() -> new NotFoundException("Agency not found: " + request.getAgencyId()));
        }

        // Si ADMIN/CLIENT => agency null
        if (u.getRole() == Role.ROLE_ADMIN || u.getRole() == Role.ROLE_CLIENT) {
            agency = null;
        } else {
            // si AGENT/MANAGER => agency obligatoire (si on veut stricte)
            validateRoleAgency(u.getRole(), (agency != null ? agency.getId() : null));
        }

        u.setFirstName(request.getFirstName());
        u.setLastName(request.getLastName());
        u.setPhone(request.getPhone());
        u.setAgency(agency);

        userRepository.save(u);
        return toResponse(u);
    }

    @Override
    @Transactional
    public void suspendUser(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        u.setActive(false);
        userRepository.save(u);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
        u.setActive(true);
        userRepository.save(u);
    }

    @Override
    public List<UserResponse> getUsersByAgency(Long agencyId) {
        return userRepository.findByAgencyId(agencyId).stream().map(UserServiceImpl::toResponse).toList();
    }
}