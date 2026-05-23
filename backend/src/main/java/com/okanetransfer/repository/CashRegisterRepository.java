package com.okanetransfer.repository;

import com.okanetransfer.entity.CashRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {
    Optional<CashRegister> findByAgencyId(Long agencyId);
}