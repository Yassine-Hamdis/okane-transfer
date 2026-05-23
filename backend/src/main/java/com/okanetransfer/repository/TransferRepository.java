package com.okanetransfer.repository;

import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findByWithdrawalCode(String withdrawalCode);
    List<Transfer> findAllByStatus(TransferStatus status);
    List<Transfer> findAllBySendingAgencyId(Long agencyId);
    List<Transfer> findAllByReceivingAgencyId(Long agencyId);
    List<Transfer> findAllByClientId(Long clientId);
    List<Transfer> findAllBySendingAgentId(Long agentId);
    List<Transfer> findAllByCorridorId(Long corridorId);
    List<Transfer> findAllByStatusAndExpiresAtBefore(
            TransferStatus status, LocalDateTime dateTime);
    Optional<Transfer> findByWithdrawalCodeAndStatus(
            String withdrawalCode, TransferStatus status);
    List<Transfer> findAllByRecipientPhone(String recipientPhone);
    // Add this method — WithdrawalCodeGenerator needs it
    boolean existsByWithdrawalCode(String withdrawalCode);
}