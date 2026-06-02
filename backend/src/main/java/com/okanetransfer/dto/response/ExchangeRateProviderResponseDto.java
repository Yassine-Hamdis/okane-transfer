package com.okanetransfer.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateProviderResponseDto {

    private Long id;

    private String name;

    private String baseUrl;

    private boolean active;

    private boolean enabled;

    private String description;
}
