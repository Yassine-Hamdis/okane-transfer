package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "currencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 3)
    @Column(name = "code", nullable = false, unique = true, length = 3)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100 , unique = true)
    private String name;

    @NotBlank
    @Size(max = 5)
    @Column(name = "symbol", nullable = false, length = 5, unique = true)
    private String symbol;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}