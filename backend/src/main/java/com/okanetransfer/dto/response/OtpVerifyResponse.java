package com.okanetransfer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtpVerifyResponse {
    private boolean valid;
    private String  message;
}