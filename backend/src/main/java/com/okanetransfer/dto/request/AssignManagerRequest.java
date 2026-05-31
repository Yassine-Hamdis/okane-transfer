package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotNull;

public class AssignManagerRequest {
    @NotNull
    private Long userId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}