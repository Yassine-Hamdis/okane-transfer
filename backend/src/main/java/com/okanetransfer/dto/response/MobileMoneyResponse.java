package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.MobileMoneyStatus;
import com.okanetransfer.entity.enums.MobileOperator;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileMoneyResponse {
    private Long              id;
    private Long              transferId;
    private String            withdrawalCode;
    private MobileOperator    operator;
    private String            walletPhone;
    private MobileMoneyStatus status;
    private String            operatorReference;
    private LocalDateTime     sentAt;
    private LocalDateTime     reconciledAt;
}