package com.okanetransfer.repository;

import com.okanetransfer.entity.Corridor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorridorRepository extends JpaRepository<Corridor, Long> {

    List<Corridor> findAllByActiveTrue();

    Optional<Corridor> findBySourceCountryIdAndDestinationCountryId(
            Long sourceCountryId,
            Long destinationCountryId
    );

    List<Corridor> findAllBySourceCountryId(Long sourceCountryId);

    List<Corridor> findAllByDestinationCountryId(Long destinationCountryId);

    boolean existsBySourceCountryIdAndDestinationCountryIdAndSourceCurrencyIdAndDestinationCurrencyId(
            Long sourceCountryId,
            Long destinationCountryId,
            Long sourceCurrencyId,
            Long destinationCurrencyId
    );
}