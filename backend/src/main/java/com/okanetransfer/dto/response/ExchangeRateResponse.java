package com.okanetransfer.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateResponse {
    private Long id;
    private Long corridorId;
    private String corridorLabel;
    private BigDecimal rate;
    private String source;
    private boolean current;
    private Long updatedById;
    private String updatedByEmail;
    private LocalDateTime recordedAt;
}
