package com.okanetransfer.repository;

import com.okanetransfer.entity.FeeGrid;
import com.okanetransfer.entity.enums.TransferType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeGridRepository extends JpaRepository<FeeGrid, Long> {

    List<FeeGrid> findAllByCorridorIdAndActiveTrue(Long corridorId);

    // Find the matching fee grid for a given amount, corridor and transfer type
    @Query("""
            SELECT f FROM FeeGrid f
            WHERE f.corridor.id = :corridorId
              AND f.transferType = :transferType
              AND f.active = true
              AND f.minAmount <= :amount
              AND f.maxAmount >= :amount
            """)
    Optional<FeeGrid> findApplicableGrid(
            Long corridorId,
            BigDecimal amount,
            TransferType transferType
    );

    List<FeeGrid> findAllByCorridorIdAndTransferTypeAndActiveTrue(
            Long corridorId, TransferType transferType);

    List<FeeGrid> findByCorridorIdAndTransferTypeAndActiveTrueOrderByMinAmountAsc(
            Long corridorId,
            TransferType transferType
    );

    @Query("""
    SELECT f FROM FeeGrid f
    WHERE f.corridor.id = :corridorId
    AND f.transferType = :transferType
    AND f.active = true
    AND :amount >= f.minAmount
    AND :amount < f.maxAmount
""")
    Optional<FeeGrid> findApplicableFeeGrid(
            Long corridorId,
            TransferType transferType,
            BigDecimal amount
    );
}