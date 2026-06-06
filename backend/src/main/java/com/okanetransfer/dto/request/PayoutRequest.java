package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayoutRequest {

    @NotBlank
    @Size(max = 8)
    private String withdrawalCode;

    // Plain ID — will be AES encrypted before saving
    @NotBlank
    @Size(max = 100)
    private String recipientIdNumber;
}