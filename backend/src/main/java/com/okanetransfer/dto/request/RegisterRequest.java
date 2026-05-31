// RegisterRequest.java
package com.okanetransfer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password; // Mot de passe en clair (sera haché par le backend)

    @NotBlank
    private String phone;

    // Pièce d'identité en clair (sera chiffrée en AES-256 par le backend)
    private String idNumber;
}