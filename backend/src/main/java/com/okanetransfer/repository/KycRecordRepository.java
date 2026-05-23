package com.okanetransfer.repository;

import com.okanetransfer.entity.KycRecord;
import com.okanetransfer.entity.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycRecordRepository extends JpaRepository<KycRecord, Long> {
    Optional<KycRecord> findByTransferId(Long transferId);
    List<KycRecord> findAllByStatus(KycStatus status);
    List<KycRecord> findAllByWatchlistHitTrue();
    List<KycRecord> findAllBySuspicionDeclaredTrue();
}