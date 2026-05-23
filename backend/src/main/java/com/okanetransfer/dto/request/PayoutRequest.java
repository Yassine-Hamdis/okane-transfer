package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayoutRequest {

    @NotBlank(message = "Withdrawal code is required")
    private String withdrawalCode;

    @NotBlank(message = "Recipient ID number is required")
    private String recipientIdNumber;   // plain — will be AES-encrypted in service
}