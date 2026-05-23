package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "countries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank
    @Size(max = 3)
    @Column(name = "code", nullable = false, unique = true, length = 3)
    private String code;

    @Column(name = "allows_sending", nullable = false)
    @Builder.Default
    private boolean allowsSending = true;

    @Column(name = "allows_receiving", nullable = false)
    @Builder.Default
    private boolean allowsReceiving = true;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}