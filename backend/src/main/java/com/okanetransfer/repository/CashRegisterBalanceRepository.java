package com.okanetransfer.repository;

import com.okanetransfer.entity.CashRegisterBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CashRegisterBalanceRepository extends JpaRepository<CashRegisterBalance, Long> {
    Optional<CashRegisterBalance> findByCashRegisterIdAndCurrencyId(
            Long cashRegisterId, Long currencyId);
    List<CashRegisterBalance> findAllByCashRegisterId(Long cashRegisterId);
}