package com.okanetransfer.service;

import com.okanetransfer.dto.request.RegisterRequest;
import com.okanetransfer.dto.response.AuthResponse;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.security.EncryptionService;
import com.okanetransfer.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EncryptionService encryptionService;
    private final JavaMailSender mailSender;

    // 👉 L'injection de ton nouveau service SMS
    private final SmsService smsService;

    @Value("${mail.username}")
    private String mailUsername;

    @Transactional
    public AuthResponse registerClient(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé.");
        }

        String generatedEmailToken = UUID.randomUUID().toString();
        String generatedPhoneOtp = String.format("%06d", new Random().nextInt(999999));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .idNumberEncrypted(encryptionService.encrypt(request.getIdNumber()))
                .role(Role.ROLE_CLIENT)
                .active(false)
                .emailVerified(false)
                .phoneVerified(false)
                .emailToken(generatedEmailToken)
                .phoneOtp(generatedPhoneOtp)
                .build();

        userRepository.save(user);

        // 🚀 ENVOI DU VRAI EMAIL
        sendVerificationEmail(user.getEmail(), generatedEmailToken);

        // 📱 ENVOI DU VRAI SMS VIA TWILIO
        smsService.sendOtpSms(user.getPhone(), generatedPhoneOtp);

        return AuthResponse.builder()
                .message("Compte créé. Veuillez vérifier votre email et votre numéro de téléphone pour l'activer.")
                .build();
    }

    private void sendVerificationEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(toEmail);
        message.setSubject("Activation de votre compte");

        // Construction du lien cliquable pointant vers l'URL Tomcat de validation
        String verificationUrl = "http://localhost:8080/backend_war_exploded/api/v1/auth/verify-email?token=" + token;

        message.setText("Bonjour,\n\n"
                + "Merci pour votre inscription. Veuillez activer votre compte en cliquant sur le lien suivant :\n"
                + verificationUrl + "\n\n"
                + "Cordialement,\nL'équipe de support.");

        mailSender.send(message);
    }

    @Transactional
    public String verifyEmail(String token) {
        User user = userRepository.findByEmailToken(token)
                .orElseThrow(() -> new RuntimeException("Lien de vérification invalide ou expiré."));

        user.setEmailVerified(true);
        user.setEmailToken(null); // On détruit le jeton pour qu'il ne soit plus réutilisable

        checkAndActivateUser(user);
        return "Adresse email vérifiée avec succès.";
    }

    @Transactional
    public String verifyPhone(String phone, String otp) {
        User user = userRepository.findByPhoneAndPhoneOtp(phone, otp)
                .orElseThrow(() -> new RuntimeException("Code SMS invalide."));

        user.setPhoneVerified(true);
        user.setPhoneOtp(null); // On détruit le code OTP

        checkAndActivateUser(user);
        return "Numéro de téléphone vérifié avec succès.";
    }

    // Méthode privée qui bascule le compte en actif si tout est bon
    private void checkAndActivateUser(User user) {
        if (user.isEmailVerified() && user.isPhoneVerified()) {
            user.setActive(true);
        }
        userRepository.save(user);
    }
}