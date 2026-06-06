package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.OperationType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashOperationResponse {
    private Long          id;
    private OperationType type;
    private BigDecimal    amount;
    private String        currencyCode;
    private BigDecimal    balanceAfter;
    private Long          agentId;
    private String        agentName;
    private Long          transferId;
    private String        withdrawalCode;
    private String        note;
    private LocalDateTime createdAt;
}