package com.okanetransfer.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashBalanceResponse {
    private Long       currencyId;
    private String     currencyCode;
    private String     currencySymbol;
    private BigDecimal currentBalance;
    private LocalDateTime updatedAt;
}