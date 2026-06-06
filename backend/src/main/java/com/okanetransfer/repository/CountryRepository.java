package com.okanetransfer.repository;

import com.okanetransfer.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCode(String code);
    Optional<Country> findByName(String name);
    List<Country> findAllByActiveTrue();
    List<Country> findAllByAllowsSendingTrue();
    List<Country> findAllByAllowsReceivingTrue();
    boolean existsByCode(String code);
}