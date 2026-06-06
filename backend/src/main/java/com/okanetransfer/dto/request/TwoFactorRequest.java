package com.okanetransfer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String otpCode;
}