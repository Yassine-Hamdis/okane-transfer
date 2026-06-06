package com.okanetransfer.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgencyResponse {
    private Long          id;
    private String        name;
    private String        address;
    private String        city;
    private String        countryName;
    private String        countryCode;
    private Long          managerId;
    private String        managerName;
    private BigDecimal    dailyLimit;
    private boolean       active;
    private LocalDateTime createdAt;
}