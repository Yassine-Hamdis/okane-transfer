package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.TransferStatus;
import com.okanetransfer.entity.enums.TransferType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    private Long           id;
    private String         withdrawalCode;

    // Sender info
    private String         senderFullName;
    private String         senderPhone;
    private String         senderCountry;

    // Recipient info
    private String         recipientFullName;
    private String         recipientPhone;
    private String         recipientCountry;

    // Amounts
    private BigDecimal     sentAmount;
    private String         sentCurrency;
    private BigDecimal     feeAmount;
    private BigDecimal     receivedAmount;
    private String         receivedCurrency;
    private BigDecimal     exchangeRate;

    // Fee breakdown
    private BigDecimal     feeFixed;
    private BigDecimal     feePercentage;

    // Type & Status
    private TransferType   transferType;
    private TransferStatus status;
    private boolean        requiresAdminApproval;
    private String         blockedReason;
    private String         cancellationReason;
    private String         notes;

    // Agencies & Agents
    private Long           sendingAgencyId;
    private String         sendingAgencyName;
    private Long           receivingAgencyId;
    private String         receivingAgencyName;
    private Long           sendingAgentId;
    private String         sendingAgentName;
    private Long           receivingAgentId;
    private String         receivingAgentName;

    // Client
    private Long           clientId;

    // Corridor
    private Long           corridorId;
    private String         corridorLabel;

    // Dates
    private LocalDateTime  createdAt;
    private LocalDateTime  paidAt;
    private LocalDateTime  expiresAt;
}