package com.okanetransfer.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyResponse {
    private Long id;
    private String code;
    private String name;
    private String symbol;
    private boolean active;
}