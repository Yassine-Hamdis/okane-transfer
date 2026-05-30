package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.OperationType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CashOperationResponse {
    private Long          id;
    private OperationType type;
    private BigDecimal    amount;
    private String        currencyCode;
    private BigDecimal    balanceAfter;
    private String        agentName;
    private Long          transferId;
    private String        note;
    private LocalDateTime createdAt;
}