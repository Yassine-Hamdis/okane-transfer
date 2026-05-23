package com.okanetransfer.repository;

import com.okanetransfer.entity.CashOperation;
import com.okanetransfer.entity.enums.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {
    List<CashOperation> findAllByCashRegisterIdOrderByCreatedAtDesc(Long cashRegisterId);
    List<CashOperation> findAllByAgentId(Long agentId);
    List<CashOperation> findAllByCashRegisterIdAndCreatedAtBetween(
            Long cashRegisterId, LocalDateTime from, LocalDateTime to);
    List<CashOperation> findAllByType(OperationType type);
    List<CashOperation> findAllByTransferId(Long transferId);
}