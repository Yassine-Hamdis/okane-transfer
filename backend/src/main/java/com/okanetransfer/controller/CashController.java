package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CloseCashRequest;
import com.okanetransfer.dto.request.DiscrepancyRequest;
import com.okanetransfer.dto.response.CashOperationResponse;
import com.okanetransfer.dto.response.CashRegisterResponse;
import com.okanetransfer.entity.User;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.CashService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent/cash")
@PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
@Tag(name = "Cash Management", description = "Agent cash register management")
public class CashController {

    @Autowired private CashService    cashService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/my-register")
    @Operation(summary = "Get my agency's cash register")
    public ResponseEntity<CashRegisterResponse> getMyRegister(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long agencyId = resolveAgencyId(userDetails);
        return ResponseEntity.ok(cashService.getRegisterByAgency(agencyId));
    }

    @GetMapping("/my-register/operations/today")
    @Operation(summary = "Get today's cash operations")
    public ResponseEntity<List<CashOperationResponse>> getTodayOperations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long agencyId = resolveAgencyId(userDetails);
        return ResponseEntity.ok(cashService.getTodayOperations(agencyId));
    }

    @GetMapping("/my-register/operations")
    @Operation(summary = "Get all cash operations history")
    public ResponseEntity<List<CashOperationResponse>> getAllOperations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long agencyId = resolveAgencyId(userDetails);
        return ResponseEntity.ok(cashService.getAllOperations(agencyId));
    }

    @PostMapping("/my-register/close")
    @Operation(summary = "Close cash register end of day")
    public ResponseEntity<CashRegisterResponse> close(
            @RequestBody CloseCashRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long agentId  = resolveUserId(userDetails);
        Long agencyId = resolveAgencyId(userDetails);
        return ResponseEntity.ok(
                cashService.closeCashRegister(agencyId, agentId, request));
    }

    @PostMapping("/my-register/discrepancy")
    @Operation(summary = "Report a cash discrepancy")
    public ResponseEntity<Map<String, String>> reportDiscrepancy(
            @Valid @RequestBody DiscrepancyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long agentId  = resolveUserId(userDetails);
        Long agencyId = resolveAgencyId(userDetails);
        cashService.reportDiscrepancy(agencyId, agentId, request);
        return ResponseEntity.ok(Map.of("message", "Discrepancy reported successfully"));
    }

    // ── Helpers ────────────────────────────────────────

    private Long resolveUserId(UserDetails userDetails) {
        return resolveUser(userDetails).getId();
    }

    private Long resolveAgencyId(UserDetails userDetails) {
        User user = resolveUser(userDetails);
        if (user.getAgency() == null) {
            throw new IllegalStateException("User has no agency assigned");
        }
        return user.getAgency().getId();
    }

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
