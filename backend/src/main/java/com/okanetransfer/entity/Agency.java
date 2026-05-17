package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "agencies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String address;

    @NotBlank
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Column(nullable = false)
    private String country;

    @Column(name = "daily_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyLimit;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // All users (agents, managers) of this agency
    @OneToMany(mappedBy = "agency", fetch = FetchType.LAZY)
    private List<User> users;

    // Cash register of this agency
    @OneToOne(mappedBy = "agency", cascade = CascadeType.ALL)
    private CashRegister cashRegister;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}