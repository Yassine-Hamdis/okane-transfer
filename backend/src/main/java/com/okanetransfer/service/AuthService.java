package com.okanetransfer.service;

import com.okanetransfer.dto.request.LoginRequest;
import com.okanetransfer.dto.request.RegisterRequest;
import com.okanetransfer.dto.request.TwoFactorRequest;
import com.okanetransfer.dto.response.LoginResponse;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.NotificationChannel;
import com.okanetransfer.entity.enums.OtpType;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired private UserRepository     userRepository;
    @Autowired private PasswordEncoder    passwordEncoder;
    @Autowired private JwtUtil            jwtUtil;
    @Autowired private OtpService         otpService;
    @Autowired private NotificationService notificationService;
    @Autowired private AuditService       auditService;

    // ─────────────────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────────────────

    /**
     * Login flow:
     *
     * Case A — 2FA disabled:
     *   1. Validate credentials
     *   2. Return JWT immediately
     *
     * Case B — 2FA enabled:
     *   1. Validate credentials
     *   2. Generate OTP → send via SMS
     *   3. Return response with requiresTwoFactor=true (no token yet)
     *   4. Client calls /verify-2fa with the OTP
     *   5. Return JWT
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        // ── 1. Find user ─────────────────────────────
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditService.log(null, "LOGIN_FAILED", ipAddress);
                    return new IllegalArgumentException("Invalid email or password");
                });

        // ── 2. Check active ──────────────────────────
        if (!user.isActive()) {
            auditService.log(user.getId(), "LOGIN_FAILED_SUSPENDED", ipAddress);
            throw new IllegalArgumentException("Account is suspended");
        }

        // ── 3. Check password ────────────────────────
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditService.log(user.getId(), "LOGIN_FAILED_WRONG_PASSWORD", ipAddress);
            throw new IllegalArgumentException("Invalid email or password");
        }

        // ── 4. 2FA check ─────────────────────────────
        if (user.isTwoFactorEnabled()) {
            String otp = otpService.generateAndSave(user.getId(), OtpType.TWO_FACTOR);

            notificationService.send(
                    user.getId(),
                    "Your OkaneTransfer verification code",
                    "Your login code is: " + otp +
                            "\nValid for 5 minutes. Do not share it.",
                    NotificationChannel.SMS,
                    user.getPhone()
            );

            auditService.log(user.getId(), "LOGIN_2FA_SENT", ipAddress);

            return LoginResponse.builder()
                    .requiresTwoFactor(true)
                    .mustChangePassword(user.isMustChangePassword())
                    .role(user.getRole().name())
                    .fullName(user.getFirstName() + " " + user.getLastName())
                    .userId(user.getId())
                    .build();
        }

        // ── 5. Issue token ───────────────────────────
        String token = jwtUtil.generateAccessToken(
                user.getEmail(), user.getRole().name());

        auditService.log(user.getId(), "LOGIN_SUCCESS", ipAddress);

        return LoginResponse.builder()
                .accessToken(token)
                .role(user.getRole().name())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .userId(user.getId())
                .mustChangePassword(user.isMustChangePassword())
                .requiresTwoFactor(false)
                .build();
    }

    // ─────────────────────────────────────────────────────
    //  VERIFY 2FA
    // ─────────────────────────────────────────────────────

    @Transactional
    public LoginResponse verifyTwoFactor(TwoFactorRequest request,
                                         String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        boolean valid = otpService.verify(
                user.getId(), request.getOtpCode(), OtpType.TWO_FACTOR);

        if (!valid) {
            auditService.log(user.getId(), "2FA_FAILED", ipAddress);
            throw new IllegalArgumentException("Invalid or expired OTP code");
        }

        String token = jwtUtil.generateAccessToken(
                user.getEmail(), user.getRole().name());

        auditService.log(user.getId(), "2FA_SUCCESS", ipAddress);

        return LoginResponse.builder()
                .accessToken(token)
                .role(user.getRole().name())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .userId(user.getId())
                .mustChangePassword(user.isMustChangePassword())
                .requiresTwoFactor(false)
                .build();
    }

    // ─────────────────────────────────────────────────────
    //  REGISTER (self-registration for ROLE_CLIENT only)
    // ─────────────────────────────────────────────────────

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.ROLE_CLIENT)
                .active(true)
                .twoFactorEnabled(false)
                .mustChangePassword(false)
                .build();

        userRepository.save(user);

        // Welcome notification
        notificationService.send(
                user.getId(),
                "Welcome to OkaneTransfer!",
                "Hello " + user.getFirstName() + ",\n\n" +
                        "Your account has been created successfully.\n" +
                        "You can now track your transfers online.",
                NotificationChannel.EMAIL,
                user.getEmail()
        );

        auditService.log(user.getId(), "CLIENT_REGISTERED",
                "User", user.getId(), null);

        log.info("New client registered: {}", user.getEmail());
    }
}