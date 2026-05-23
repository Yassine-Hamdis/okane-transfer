package com.okanetransfer.repository;

import com.okanetransfer.entity.MobileMoney;
import com.okanetransfer.entity.enums.MobileMoneyStatus;
import com.okanetransfer.entity.enums.MobileOperator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MobileMoneyRepository extends JpaRepository<MobileMoney, Long> {
    Optional<MobileMoney> findByTransferId(Long transferId);
    List<MobileMoney> findAllByStatus(MobileMoneyStatus status);
    List<MobileMoney> findAllByOperator(MobileOperator operator);
    List<MobileMoney> findAllByWalletPhone(String walletPhone);
}