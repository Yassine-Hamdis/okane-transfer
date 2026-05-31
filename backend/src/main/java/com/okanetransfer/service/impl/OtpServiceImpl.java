package com.okanetransfer.service.impl;

import com.okanetransfer.entity.OtpCode;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.OtpType;
import com.okanetransfer.exception.BadRequestException;
import com.okanetransfer.exception.NotFoundException;
import com.okanetransfer.repository.OtpCodeRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.OtpService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpServiceImpl implements OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder otpEncoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    public OtpServiceImpl(OtpCodeRepository otpCodeRepository, UserRepository userRepository) {
        this.otpCodeRepository = otpCodeRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public String generateAndSave(Long userId, OtpType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        String rawCode = String.format("%06d", random.nextInt(1_000_000));
        String hash = otpEncoder.encode(rawCode);

        OtpCode otp = new OtpCode();
        otp.setUser(user);
        otp.setType(type);
        otp.setCodeHash(hash);
        otp.setUsed(false);
        otp.setBlocked(false);
        otp.setAttemptCount(0);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        otpCodeRepository.save(otp);

        // Simulation (remplacer par SMS/email plus tard)
        System.out.println("[2FA OTP] " + user.getEmail() + " => " + rawCode);

        return rawCode;
    }

    @Override
    @Transactional
    public boolean verify(Long userId, String rawCode, OtpType type) {
        OtpCode otp = otpCodeRepository.findTopByUserIdAndTypeOrderByCreatedAtDesc(userId, type)
                .orElseThrow(() -> new BadRequestException("OTP not found"));

        if (otp.isBlocked()) throw new BadRequestException("OTP blocked");
        if (otp.isUsed()) throw new BadRequestException("OTP already used");
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) throw new BadRequestException("OTP expired");

        boolean ok = otpEncoder.matches(rawCode, otp.getCodeHash());
        if (!ok) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            if (otp.getAttemptCount() >= 5) otp.setBlocked(true);
            otpCodeRepository.save(otp);
            return false;
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);
        return true;
    }
}