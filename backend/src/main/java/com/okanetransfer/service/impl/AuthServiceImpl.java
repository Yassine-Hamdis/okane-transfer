package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.LoginRequest;
import com.okanetransfer.dto.request.RegisterRequest;
import com.okanetransfer.dto.request.Verify2FaRequest;
import com.okanetransfer.dto.response.AuthResponse;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.OtpType;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.exception.BadRequestException;
import com.okanetransfer.exception.ConflictException;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.security.JwtUtil;
import com.okanetransfer.service.AuthService;
import com.okanetransfer.service.OtpService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, OtpService otpService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!user.isActive()) throw new BadRequestException("Account disabled");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        AuthResponse resp = AuthResponse.builder()
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole().name())
                .build();

        if (user.isTwoFactorEnabled()) {
            otpService.generateAndSave(user.getId(), OtpType.TWO_FACTOR);
            resp.setTwoFactorRequired(true);
            resp.setAccessToken(null);
            return resp;
        }

        resp.setTwoFactorRequired(false);
        resp.setAccessToken(jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name()));
        return resp;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new ConflictException("Email already used");
        }

        User u = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_CLIENT)
                .active(true)
                .twoFactorEnabled(false)
                .build();

        userRepository.save(u);
    }

    @Override
    public AuthResponse verify2fa(Verify2FaRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!user.isActive()) throw new BadRequestException("Account disabled");

        boolean ok = otpService.verify(user.getId(), request.getOtpCode(), OtpType.TWO_FACTOR);
        if (!ok) throw new BadRequestException("Invalid OTP");

        return AuthResponse.builder()
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole().name())
                .twoFactorRequired(false)
                .accessToken(jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name()))
                .build();
    }
}