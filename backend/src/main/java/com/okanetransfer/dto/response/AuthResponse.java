// AuthResponse.java
package com.okanetransfer.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String message;
}