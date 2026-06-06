package com.okanetransfer.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorridorResponse {
    private Long id;

    private Long sourceCountryId;
    private String sourceCountryName;
    private String sourceCountryCode;

    private Long destinationCountryId;
    private String destinationCountryName;
    private String destinationCountryCode;

    private Long sourceCurrencyId;
    private String sourceCurrencyCode;

    private Long destinationCurrencyId;
    private String destinationCurrencyCode;

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
