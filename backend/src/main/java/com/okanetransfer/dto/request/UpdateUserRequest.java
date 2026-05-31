package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateUserRequest {

    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank private String phone;

    private Long agencyId;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Long getAgencyId() { return agencyId; }
    public void setAgencyId(Long agencyId) { this.agencyId = agencyId; }
}