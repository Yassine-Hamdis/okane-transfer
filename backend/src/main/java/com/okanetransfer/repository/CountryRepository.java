package com.okanetransfer.repository;

import com.okanetransfer.entity.Country;
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
public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCode(String code);
    Optional<Country> findByName(String name);
    List<Country> findAllByActiveTrue();
    List<Country> findAllByAllowsSendingTrue();

    List<Country> findAllByAllowsReceivingTrue();

    boolean existsByCode(String code);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByCodeAndIdNot(String code, Long id);
    @Query("""
    SELECT c FROM Country c
    WHERE (:active IS NULL OR c.active = :active)
    AND (
        :keyword IS NULL OR :keyword = '' 
        OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
""")
    Page<Country> searchCountries(
            @Param("active") Boolean active,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
    SELECT c
    FROM Country c
    WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ORDER BY c.name ASC
""")
    List<Country> searchByKeyword(@Param("keyword") String keyword);
    List<Country> findByDefaultCurrency_Id(Long currencyId);

    @Modifying
    @Query("""
    UPDATE Country c
    SET c.active = false
    WHERE c.defaultCurrency.id = :currencyId
""")
    void disableByCurrency(@Param("currencyId") Long currencyId);
}