package com.okanetransfer.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrencyResponseDto {

    private Long id;

    private String code;

    private String name;

    private String symbol;

    private boolean active;
}
