package com.okanetransfer.dto.request;

import com.okanetransfer.entity.enums.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KycReviewRequest {

    @NotNull
    private KycStatus status;

    private String notes;
}