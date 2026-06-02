package com.okanetransfer.repository;

import com.okanetransfer.entity.Country;
import com.okanetransfer.entity.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCode(String code);
    List<Currency> findAllByActiveTrue();
    boolean existsByCode(String code);
    boolean existsBySymbol(String symbol);
    boolean existsByName(String name);
    boolean existsByCodeAndIdNot(String code, Long id);
    boolean existsBySymbolAndIdNot(String symbol, Long id);
    @Query("""
    SELECT c FROM Currency c
    WHERE (:status IS NULL OR c.active = :status)
    AND (
        LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(c.symbol) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
""")
    Page<Currency> searchCurrencies(
            @Param("status") Boolean status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}