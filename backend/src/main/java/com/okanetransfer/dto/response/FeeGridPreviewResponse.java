package com.okanetransfer.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class FeeGridPreviewResponse {

    private Long corridorId;
    private String corridorLabel;

    private String transferType;

    private List<FeeGridPreviewItemDto> tiers;
}
