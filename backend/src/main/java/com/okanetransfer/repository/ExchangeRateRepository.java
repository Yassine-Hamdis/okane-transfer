package com.okanetransfer.repository;

import com.okanetransfer.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByCorridorIdAndIsCurrentTrue(Long corridorId);

    List<ExchangeRate> findAllByCorridorIdOrderByRecordedAtDesc(Long corridorId);

    @Modifying
    @Query("UPDATE ExchangeRate e SET e.isCurrent = false WHERE e.corridor.id = :corridorId")
    void deactivateAllByCorridorId(@Param("corridorId") Long corridorId);
}