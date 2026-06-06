package com.okanetransfer.repository;

import com.okanetransfer.entity.ExchangeRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    // Get the active current rate for a corridor
    Optional<ExchangeRate> findByCorridorIdAndIsCurrentTrue(Long corridorId);

    // Full rate history for a corridor
    List<ExchangeRate> findAllByCorridorIdOrderByRecordedAtDesc(Long corridorId);

    // Mark all rates for a corridor as not current (before inserting new one)
    @Modifying
    @Query("UPDATE ExchangeRate e SET e.isCurrent = false WHERE e.corridor.id = :corridorId")
    void deactivateAllByCorridorId(Long corridorId);

    Optional<ExchangeRate> findTopByCorridorIdOrderByRecordedAtDesc(Long corridorId);

    @Modifying
    @Query("""
            update ExchangeRate e
            set e.isCurrent = false
            where e.corridor.id = :corridorId
            and e.isCurrent = true
            """)
    void clearCurrentRate(Long corridorId);

    Page<ExchangeRate> findByCorridorIdOrderByRecordedAtDesc(
            Long corridorId,
            Pageable pageable
    );

    @Query("SELECT er FROM ExchangeRate er " +
            "WHERE er.corridor.sourceCurrency.code = :sourceCode " +
            "AND er.corridor.destinationCurrency.code = :destCode " +
            "AND er.isCurrent = true")
    Optional<ExchangeRate> findCurrentRateByCurrencyCodes(
            @Param("sourceCode") String sourceCode,
            @Param("destCode") String destCode
    );

}