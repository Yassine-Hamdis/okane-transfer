package com.okanetransfer.dto.request;

import com.okanetransfer.entity.enums.TransferType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateTransferRequest {

    // ── Sender ────────────────────────────────────────────────────────────────
    @NotBlank(message = "Sender first name is required")
    private String senderFirstName;

    @NotBlank(message = "Sender last name is required")
    private String senderLastName;

    @NotBlank(message = "Sender phone is required")
    private String senderPhone;

    @NotBlank(message = "Sender ID number is required")
    private String senderIdNumber;   // plain — will be AES-encrypted in service

    @NotNull(message = "Sender country is required")
    private Long senderCountryId;

    // ── Recipient ─────────────────────────────────────────────────────────────
    @NotBlank(message = "Recipient first name is required")
    private String recipientFirstName;

    @NotBlank(message = "Recipient last name is required")
    private String recipientLastName;

    @NotBlank(message = "Recipient phone is required")
    private String recipientPhone;

    @NotNull(message = "Recipient country is required")
    private Long recipientCountryId;

    // ── Financials ────────────────────────────────────────────────────────────
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    private BigDecimal sentAmount;

    @NotNull(message = "Currency is required")
    private Long sentCurrencyId;

    @NotNull(message = "Corridor is required")
    private Long corridorId;

    @NotNull(message = "Transfer type is required")
    private TransferType transferType;
}