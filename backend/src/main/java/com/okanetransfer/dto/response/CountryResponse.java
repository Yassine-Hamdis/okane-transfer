package com.okanetransfer.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryResponse {
    private Long id;
    private String name;
    private String code;
    private boolean allowsSending;
    private boolean allowsReceiving;
    private boolean active;
}