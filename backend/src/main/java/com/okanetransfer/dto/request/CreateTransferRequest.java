package com.okanetransfer.dto.request;

import com.okanetransfer.entity.enums.TransferType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransferRequest {

    // ── Sender ───────────────────────────────────────
    @NotBlank
    @Size(max = 100)
    private String senderFirstName;

    @NotBlank
    @Size(max = 100)
    private String senderLastName;

    @NotBlank
    @Size(max = 20)
    private String senderPhone;

    // Plain ID number — will be AES encrypted before saving
    @Size(max = 100)
    private String senderIdNumber;

    // Optional — if sender has email, auto-create client account
    private String senderEmail;

    @NotNull
    private Long senderCountryId;

    // ── Recipient ────────────────────────────────────
    @NotBlank
    @Size(max = 100)
    private String recipientFirstName;

    @NotBlank
    @Size(max = 100)
    private String recipientLastName;

    @NotBlank
    @Size(max = 20)
    private String recipientPhone;

    @NotNull
    private Long recipientCountryId;

    // ── Amount ───────────────────────────────────────
    @NotNull
    @DecimalMin(value = "1.00", message = "Amount must be at least 1")
    private BigDecimal sentAmount;

    @NotNull
    private Long sentCurrencyId;

    // ── Corridor & Type ──────────────────────────────
    @NotNull
    private Long corridorId;

    private TransferType transferType = TransferType.STANDARD;

    // Optional agent note
    private String notes;
}
