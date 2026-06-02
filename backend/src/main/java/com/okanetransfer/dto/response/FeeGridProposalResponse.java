package com.okanetransfer.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class FeeGridProposalResponse {

    // simulation result
    private FeeSimulationResponse simulation;

    // validation status
    private boolean valid;

    private List<String> warnings;
}
