package com.okanetransfer.dto.request;

import com.okanetransfer.entity.enums.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KycReviewRequest {

    @NotNull
    private KycStatus status;

    private String notes;
}