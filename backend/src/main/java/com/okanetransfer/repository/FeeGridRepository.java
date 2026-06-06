package com.okanetransfer.repository;

import com.okanetransfer.entity.FeeGrid;
import com.okanetransfer.entity.enums.TransferType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FeeGridRepository extends JpaRepository<FeeGrid, Long> {

    List<FeeGrid> findAllByCorridorId(Long corridorId);

    List<FeeGrid> findAllByCorridorIdAndActiveTrue(Long corridorId);

    List<FeeGrid> findAllByCorridorIdAndCurrencyIdAndTransferTypeAndActiveTrue(
            Long corridorId,
            Long currencyId,
            TransferType transferType
    );

    @Query("""
            SELECT f FROM FeeGrid f
            WHERE f.corridor.id = :corridorId
              AND f.currency.id = :currencyId
              AND f.transferType = :transferType
              AND f.active = true
              AND f.minAmount <= :amount
              AND f.maxAmount >= :amount
            """)
    Optional<FeeGrid> findApplicableGrid(
            @Param("corridorId") Long corridorId,
            @Param("currencyId") Long currencyId,
            @Param("amount") BigDecimal amount,
            @Param("transferType") TransferType transferType
    );
}