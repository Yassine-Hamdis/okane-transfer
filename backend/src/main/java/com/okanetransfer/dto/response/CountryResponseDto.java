package com.okanetransfer.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CountryResponseDto {

    private Long id;

    private String name;

    private String code;

    private boolean allowsSending;

    private boolean allowsReceiving;

    private boolean active;

    private Long defaultCurrencyId;

    private String defaultCurrencyCode;

    private String defaultCurrencyName;
}
