package com.okanetransfer.repository;

import com.okanetransfer.entity.ExchangeRateProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateProviderRepository extends JpaRepository<ExchangeRateProvider, Long> {

        boolean existsByName(String name);

        boolean existsByNameAndIdNot(String name, Long id);

        Optional<ExchangeRateProvider> findByActiveTrue();

        @Modifying
        @Query("""
            update ExchangeRateProvider p
            set p.active = false
            """)
        void deactivateAll();


}
