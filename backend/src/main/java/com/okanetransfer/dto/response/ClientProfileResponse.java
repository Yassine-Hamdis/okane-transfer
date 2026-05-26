package com.okanetransfer.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProfileResponse {
    private Long    id;
    private String  firstName;
    private String  lastName;
    private String  email;
    private String  phone;
    private boolean twoFactorEnabled;
    private boolean mustChangePassword;
}