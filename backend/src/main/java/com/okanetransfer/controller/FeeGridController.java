package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateFeeGridRequest;
import com.okanetransfer.dto.request.FeeGridProposalRequest;
import com.okanetransfer.dto.response.*;
import com.okanetransfer.entity.FeeGrid;
import com.okanetransfer.entity.enums.TransferType;
import com.okanetransfer.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/fee-grids")
@RequiredArgsConstructor
public class FeeGridController {

    private final FeeService feeGridService;

    @PostMapping("/admin/fee-grids/propose")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeGridProposalResponse>> propose(
            @RequestBody FeeGridProposalRequest request) {

        FeeGridProposalResponse response =
                feeGridService.propose(request);

        return ResponseEntity.ok(
                ApiResponse.<FeeGridProposalResponse>builder()
                        .success(true)
                        .message("Fee grid simulation completed successfully")
                        .data(response)
                        .build()
        );
    }


    @PostMapping("/admin/fee-grids/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeGrid>> create(
            @RequestBody CreateFeeGridRequest request) {

        FeeGrid created = feeGridService.createFeeGrid(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<FeeGrid>builder()
                        .success(true)
                        .message("Fee grid created successfully")
                        .data(created)
                        .build()
                );
    }

    @GetMapping("/admin/fee-grids/preview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeGridPreviewResponse> preview(
            @RequestParam Long corridorId,
            @RequestParam TransferType transferType
    ) {

        return ResponseEntity.ok(
                feeGridService.getFeePreview(corridorId, transferType)
        );
    }

    @GetMapping("/admin/fee-grids/export/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam Long corridorId,
            @RequestParam TransferType transferType
    ) {
        byte[] data = feeGridService.exportCsv(corridorId, transferType);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fee-grid.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(data);
    }

    @GetMapping("/admin/fee-grids/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam Long corridorId,
            @RequestParam TransferType transferType
    ) {
        byte[] data = feeGridService.exportPdf(corridorId, transferType);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fee-grid.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
    @GetMapping("/agent/fee-grids/applicable")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AgentFeeGridDto>> getApplicableFeeGrid(
            @RequestParam Long corridorId,
            @RequestParam TransferType transferType,
            @RequestParam BigDecimal amount) {

        AgentFeeGridDto response =
                feeGridService.getApplicableFeeGrid(
                        corridorId,
                        transferType,
                        amount
                );

        return ResponseEntity.ok(
                ApiResponse.<AgentFeeGridDto>builder()
                        .success(true)
                        .message("Applicable fee grid retrieved successfully")
                        .data(response)
                        .build()
        );
    }
}