package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerifyRequest {

    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP code must be exactly 6 digits")
    private String code;
}