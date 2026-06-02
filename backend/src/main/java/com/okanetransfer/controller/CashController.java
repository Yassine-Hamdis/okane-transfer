package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CloseCashRequest;
import com.okanetransfer.dto.request.DiscrepancyRequest;
import com.okanetransfer.dto.response.ApiResponse;
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

@RestController
@RequestMapping("/api/agent/cash")
@PreAuthorize("hasAnyAuthority('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
@Tag(name = "Cash Management", description = "Agent cash register management")
public class CashController {

    @Autowired private CashService    cashService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/my-register")
    @Operation(summary = "Get my agency cash register with balances")
    public ResponseEntity<ApiResponse<CashRegisterResponse>> getMyRegister(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Cash register retrieved successfully",
                        cashService.getRegisterByAgency(resolveAgencyId(userDetails))));
    }

    @GetMapping("/my-register/operations/today")
    @Operation(summary = "Get today's cash operations")
    public ResponseEntity<ApiResponse<List<CashOperationResponse>>> getTodayOps(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Today's operations retrieved successfully",
                        cashService.getTodayOperations(resolveAgencyId(userDetails))));
    }

    @GetMapping("/my-register/operations")
    @Operation(summary = "Get all cash operations history")
    public ResponseEntity<ApiResponse<List<CashOperationResponse>>> getAllOps(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Operations retrieved successfully",
                        cashService.getAllOperations(resolveAgencyId(userDetails))));
    }

    @PostMapping("/my-register/close")
    @Operation(summary = "Close cash register end of day")
    public ResponseEntity<ApiResponse<CashRegisterResponse>> close(
            @RequestBody CloseCashRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Cash register closed successfully",
                        cashService.closeCashRegister(
                                resolveAgencyId(userDetails),
                                resolveUserId(userDetails),
                                request)));
    }

    @PostMapping("/my-register/discrepancy")
    @Operation(summary = "Report a cash discrepancy")
    public ResponseEntity<ApiResponse<Void>> discrepancy(
            @Valid @RequestBody DiscrepancyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        cashService.reportDiscrepancy(
                resolveAgencyId(userDetails),
                resolveUserId(userDetails),
                request);
        return ResponseEntity.ok(
                ApiResponse.success("Discrepancy reported successfully"));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return resolveUser(userDetails).getId();
    }

    private Long resolveAgencyId(UserDetails userDetails) {
        User user = resolveUser(userDetails);
        if (user.getAgency() == null) {
            throw new IllegalStateException("No agency assigned to this user");
        }
        return user.getAgency().getId();
    }

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}