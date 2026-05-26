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
public class TransferTrackResponse {
    private String         withdrawalCode;
    private TransferStatus status;
    private TransferType   transferType;
    private String         recipientFullName;
    private BigDecimal     receivedAmount;
    private String         receivedCurrency;
    private String         sendingAgency;
    private LocalDateTime  createdAt;
    private LocalDateTime  expiresAt;
    private LocalDateTime  paidAt;
}