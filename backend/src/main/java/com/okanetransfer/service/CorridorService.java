package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateCorridorRequest;
import com.okanetransfer.dto.response.CorridorResponseDto;
import com.okanetransfer.dto.response.PaginationResponse;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.Country;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.mapper.CorridorMapper;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.CountryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CorridorService {

    private final CountryRepository countryRepository;
    private final CorridorRepository corridorRepository;

    public CorridorService(CountryRepository countryRepository, CorridorRepository corridorRepository) {
        this.countryRepository = countryRepository;
        this.corridorRepository = corridorRepository;
    }


    @Transactional
    public CorridorResponseDto create(CreateCorridorRequest request) {

        Country source = countryRepository.findById(request.getSourceCountryId())
                .orElseThrow(() -> new RuntimeException("Source country not found"));

        Country destination = countryRepository.findById(request.getDestinationCountryId())
                .orElseThrow(() -> new RuntimeException("Destination country not found"));

        if (source.getId().equals(destination.getId())) {
            throw new RuntimeException("Source and destination cannot be same");
        }

        if (corridorRepository.existsBySourceCountryIdAndDestinationCountryId(
                source.getId(),
                destination.getId()
        )) {
            throw new RuntimeException("Corridor already exists");
        }

        boolean active = canBeActive(source, destination);

        Corridor corridor = Corridor.builder()
                .sourceCountry(source)
                .destinationCountry(destination)
                .sourceCurrency(source.getDefaultCurrency())
                .destinationCurrency(destination.getDefaultCurrency())
                .active(active)
                .build();

        Corridor saved = corridorRepository.save(corridor);

        return CorridorMapper.toDto(saved);
    }

    @Transactional
    public CorridorResponseDto toggleActive(Long id) {

        Corridor corridor = corridorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corridor not found"));

        Country source = corridor.getSourceCountry();
        Country destination = corridor.getDestinationCountry();
        if (!corridor.isActive()) {

            if (!canBeActive(
                    corridor.getSourceCountry(),
                    corridor.getDestinationCountry())) {

                throw new RuntimeException(
                        "Corridor cannot be activated because countries configuration does not allow it"
                );
            }
        }

        // 🔁 bascule état
        corridor.setActive(!corridor.isActive());

        Corridor saved = corridorRepository.save(corridor);

        return CorridorMapper.toDto(saved);
    }

    public boolean canBeActive(Country source, Country destination) {

        return source.isActive()
                && source.isAllowsSending()
                && destination.isActive()
                && destination.isAllowsReceiving();
    }

    @Transactional
    public void refreshCorridorsByCountry(Long countryId) {

        List<Corridor> corridors =
                corridorRepository
                        .findBySourceCountryIdOrDestinationCountryId(
                                countryId,
                                countryId);

        for (Corridor corridor : corridors) {

            boolean active = canBeActive(
                    corridor.getSourceCountry(),
                    corridor.getDestinationCountry());

            corridor.setActive(active);
        }
    }

    @Transactional(readOnly = true)
    public PaginationResponse<CorridorResponseDto> search(
            Long sourceCountryId,
            Long destinationCountryId,
            Boolean active,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (page < 0) page = 0;
        if (size <= 0 || size > 50) size = 10;

        List<String> allowedSorts = List.of("id", "active", "createdAt", "updatedAt");

        if (sortBy == null || !allowedSorts.contains(sortBy)) {
            sortBy = "id";
        }

        if (direction == null ||
                (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))) {
            direction = "asc";
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Corridor> result = corridorRepository.search(
                sourceCountryId,
                destinationCountryId,
                active,
                pageable
        );

        List<CorridorResponseDto> content = result.getContent()
                .stream()
                .map(CorridorMapper::toDto)
                .toList();

        return PaginationResponse.<CorridorResponseDto>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public CorridorResponseDto getCorridor(
            Long sourceCountryId,
            Long destinationCountryId) {

        Corridor corridor = corridorRepository
                .findExactCorridor(sourceCountryId, destinationCountryId)
                .orElseThrow(() ->
                        new RuntimeException("Corridor not found for this route"));

        return CorridorMapper.toDto(corridor);
    }
}
