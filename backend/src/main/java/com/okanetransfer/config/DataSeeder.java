package com.okanetransfer.config;

import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public DataSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (userRepository.existsByRole(Role.ROLE_ADMIN)) return;

        String email = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@okane.com");
        String password = System.getenv().getOrDefault("ADMIN_PASSWORD", "Admin@12345");

        User admin = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email(email.toLowerCase())
                .phone("0000000000")
                .password(encoder.encode(password))
                .role(Role.ROLE_ADMIN)
                .active(true)
                .twoFactorEnabled(false)
                .build();

        userRepository.save(admin);
        System.out.println("[SEED] Admin created: " + email);
    }
}