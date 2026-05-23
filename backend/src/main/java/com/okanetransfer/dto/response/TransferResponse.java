package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.TransferStatus;
import com.okanetransfer.entity.enums.TransferType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransferResponse {

    private Long           id;
    private String         withdrawalCode;
    private TransferStatus status;

    private String         senderFullName;
    private String         recipientFullName;

    private BigDecimal     sentAmount;
    private String         sentCurrencyCode;

    private BigDecimal     feeAmount;

    private BigDecimal     receivedAmount;
    private String         receivedCurrencyCode;

    private BigDecimal     exchangeRate;
    private TransferType   transferType;

    private String         sendingAgencyName;
    private boolean        requiresAdminApproval;

    private LocalDateTime  createdAt;
    private LocalDateTime  expiresAt;
    private LocalDateTime  paidAt;
}