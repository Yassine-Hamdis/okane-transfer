package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateExchangeRateProviderRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 500)
    private String baseUrl;

    @NotBlank
    @Size(max = 500)
    private String apiKey;

    @Size(max = 1000)
    private String description;
}
