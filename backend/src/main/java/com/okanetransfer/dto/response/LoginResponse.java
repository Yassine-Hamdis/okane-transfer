package com.okanetransfer.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String  accessToken;
    private String  role;
    private String  fullName;
    private Long    userId;
    private boolean mustChangePassword;
    private boolean requiresTwoFactor;
}