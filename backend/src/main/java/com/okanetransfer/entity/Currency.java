package com.okanetransfer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "currencies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 3)
    private String code;       // USD, EUR, MAD...

    @Column(nullable = false)
    private String name;       // US Dollar, Euro...

    @Column(nullable = false)
    private String symbol;     // $, €, د.م...

    @Column(nullable = false)
    private boolean active = true;
}