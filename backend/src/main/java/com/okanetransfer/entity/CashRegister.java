package com.okanetransfer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cash_registers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One cash register per agency
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false, unique = true)
    private Agency agency;

    @Column(name = "last_closed_at")
    private LocalDateTime lastClosedAt;
}