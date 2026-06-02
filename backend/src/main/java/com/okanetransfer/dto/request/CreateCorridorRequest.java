package com.okanetransfer.dto.request;

import lombok.Data;

@Data
public class CreateCorridorRequest {

    private Long sourceCountryId;

    private Long destinationCountryId;
}
