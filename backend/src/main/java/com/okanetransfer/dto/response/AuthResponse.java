package com.okanetransfer.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;          // null si 2FA requise
    private String role;
    private String fullName;
    private boolean twoFactorRequired;
}