package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CloseCashRequest;
import com.okanetransfer.dto.request.DiscrepancyRequest;
import com.okanetransfer.dto.response.CashBalanceResponse;
import com.okanetransfer.dto.response.CashOperationResponse;
import com.okanetransfer.dto.response.CashRegisterResponse;
import com.okanetransfer.entity.User;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.security.CustomUserDetails;
import com.okanetransfer.service.CashService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent/cash")
@PreAuthorize("hasRole('ROLE_AGENT')")
@Tag(name = "Cash Register", description = "Agent cash register — balances, operations and end-of-day close")
public class CashController {

    private final CashService    cashService;
    private final UserRepository userRepository;

    public CashController(CashService cashService, UserRepository userRepository) {
        this.cashService    = cashService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get my cash register",
            description = "Returns the cash register for the agent's agency, including all currency balances.")
    @GetMapping("/my-register")
    public ResponseEntity<CashRegisterResponse> getRegister(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(cashService.getRegisterByAgency(principal.getAgencyId()));
    }

    @Operation(summary = "Get current balances by currency")
    @GetMapping("/my-register/balances")
    public ResponseEntity<List<CashBalanceResponse>> getBalances(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(cashService.getBalances(principal.getAgencyId()));
    }

    @Operation(summary = "Get today's cash operations",
            description = "Returns all ENVOI, RETRAIT and ANNULATION operations recorded since midnight.")
    @GetMapping("/my-register/operations/today")
    public ResponseEntity<List<CashOperationResponse>> getTodayOps(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(cashService.getTodayOperations(principal.getAgencyId()));
    }

    @Operation(summary = "Close the cash register",
            description = "Records a CLOTURE_CAISSE operation and stamps lastClosedAt. Run at end of day.")
    @ApiResponse(responseCode = "204", description = "Cash register closed successfully")
    @PostMapping("/my-register/close")
    public ResponseEntity<Void> close(
            @RequestBody CloseCashRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        cashService.closeCashRegister(principal.getAgencyId(), resolveUser(principal.getId()), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Report a cash discrepancy",
            description = "Use when the physical cash count does not match the system balance.")
    @ApiResponse(responseCode = "204", description = "Discrepancy recorded")
    @PostMapping("/my-register/discrepancy")
    public ResponseEntity<Void> discrepancy(
            @Valid @RequestBody DiscrepancyRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        cashService.reportDiscrepancy(principal.getAgencyId(), resolveUser(principal.getId()), request);
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
