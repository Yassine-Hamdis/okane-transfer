package com.okanetransfer.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashRegisterResponse {
    private Long                     id;
    private Long                     agencyId;
    private String                   agencyName;
    private List<CashBalanceResponse> balances;
    private LocalDateTime            lastClosedAt;
}
