package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.KycStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycRecordResponse {
    private Long          id;
    private Long          transferId;
    private String        withdrawalCode;
    private KycStatus     status;
    private boolean       watchlistHit;
    private boolean       suspicionDeclared;
    private Integer       riskScore;
    private String        notes;
    private Long          checkedById;
    private String        checkedByEmail;
    private LocalDateTime checkedAt;
}