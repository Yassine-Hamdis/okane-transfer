package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCountryRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(min = 2, max = 3)
    private String code;

    private Boolean allowsSending = true;

    private Boolean allowsReceiving = true;
}
