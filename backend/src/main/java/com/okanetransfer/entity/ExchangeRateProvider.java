package com.okanetransfer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exchange_rate_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String baseUrl;

    @Column(nullable = false, length = 500)
    private String apiKey;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = false;

    @Column(length = 1000)
    private String description;
}
