package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.Role;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long          id;
    private String        firstName;
    private String        lastName;
    private String        email;
    private String        phone;
    private Role          role;
    private boolean       active;
    private boolean       twoFactorEnabled;
    private boolean       mustChangePassword;
    private Long          agencyId;
    private String        agencyName;
    private LocalDateTime createdAt;
}