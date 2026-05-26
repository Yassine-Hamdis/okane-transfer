package com.okanetransfer.service;

import com.okanetransfer.dto.request.ChangePasswordRequest;
import com.okanetransfer.dto.request.UpdateProfileRequest;
import com.okanetransfer.dto.response.ClientProfileResponse;
import com.okanetransfer.dto.response.TransferSummaryResponse;
import com.okanetransfer.dto.response.TransferTrackResponse;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.TransferStatus;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditService auditService;

    // ─────────────────────────────────────────────────────
    //  PROFILE
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ClientProfileResponse getMyProfile(Long userId) {
        User user = findUser(userId);
        return toProfileResponse(user);
    }

    @Transactional
    public ClientProfileResponse updateProfile(Long userId,
                                               UpdateProfileRequest request) {
        User user = findUser(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        auditService.log(userId, "CLIENT_PROFILE_UPDATED", "User", userId, null);
        return toProfileResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUser(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException(
                    "New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        auditService.log(userId, "PASSWORD_CHANGED", "User", userId, null);
    }

    @Transactional
    public void toggleTwoFactor(Long userId) {
        User user = findUser(userId);
        boolean newValue = !user.isTwoFactorEnabled();
        user.setTwoFactorEnabled(newValue);
        userRepository.save(user);

        String action = newValue ? "2FA_ENABLED" : "2FA_DISABLED";
        auditService.log(userId, action, "User", userId, null);
    }

    // ─────────────────────────────────────────────────────
    //  TRANSFERS
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TransferSummaryResponse> getMyTransfers(Long userId) {
        return transferRepository.findAllByClientId(userId)
                .stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransferSummaryResponse getMyTransferById(Long userId, Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", transferId));

        // Security: client can only see their own transfers
        if (transfer.getClient() == null ||
                !transfer.getClient().getId().equals(userId)) {
            throw new RuntimeException("Access denied to this transfer");
        }

        return toSummaryResponse(transfer);
    }

    @Transactional(readOnly = true)
    public TransferTrackResponse trackTransfer(String withdrawalCode) {
        Transfer transfer = transferRepository
                .findByWithdrawalCode(withdrawalCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transfer not found with code: " + withdrawalCode));

        return toTrackResponse(transfer);
    }

    // ─────────────────────────────────────────────────────
    //  MAPPERS
    // ─────────────────────────────────────────────────────

    private ClientProfileResponse toProfileResponse(User user) {
        return ClientProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    private TransferSummaryResponse toSummaryResponse(Transfer t) {
        return TransferSummaryResponse.builder()
                .id(t.getId())
                .withdrawalCode(t.getWithdrawalCode())
                .status(t.getStatus())
                .transferType(t.getTransferType())
                .recipientFullName(
                        t.getRecipientFirstName() + " " + t.getRecipientLastName())
                .sentAmount(t.getSentAmount())
                .sentCurrency(t.getSentCurrency().getCode())
                .receivedAmount(t.getReceivedAmount())
                .receivedCurrency(t.getReceivedCurrency().getCode())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private TransferTrackResponse toTrackResponse(Transfer t) {
        return TransferTrackResponse.builder()
                .withdrawalCode(t.getWithdrawalCode())
                .status(t.getStatus())
                .transferType(t.getTransferType())
                .recipientFullName(
                        t.getRecipientFirstName() + " " + t.getRecipientLastName())
                .receivedAmount(t.getReceivedAmount())
                .receivedCurrency(t.getReceivedCurrency().getCode())
                .sendingAgency(t.getSendingAgency().getName())
                .createdAt(t.getCreatedAt())
                .expiresAt(t.getExpiresAt())
                .paidAt(t.getPaidAt())
                .build();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}