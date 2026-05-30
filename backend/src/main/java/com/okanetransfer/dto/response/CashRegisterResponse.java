package com.okanetransfer.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CashRegisterResponse {
    private Long                    id;
    private Long                    agencyId;
    private List<CashBalanceResponse> balances;
    private LocalDateTime           lastClosedAt;
}