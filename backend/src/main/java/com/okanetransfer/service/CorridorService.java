package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateCorridorRequest;
import com.okanetransfer.dto.response.CorridorResponse;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.Country;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.CountryRepository;
import com.okanetransfer.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CorridorService {

    @Autowired
    private CorridorRepository corridorRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private AuditService auditService;

    @Transactional(readOnly = true)
    public List<CorridorResponse> getAll() {
        return corridorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CorridorResponse> getActive() {
        return corridorRepository.findAllByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CorridorResponse getById(Long id) {
        return toResponse(findCorridor(id));
    }

    @Transactional(readOnly = true)
    public List<CorridorResponse> getBySourceCountry(Long countryId) {
        return corridorRepository.findAllBySourceCountryId(countryId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CorridorResponse> getByDestinationCountry(Long countryId) {
        return corridorRepository.findAllByDestinationCountryId(countryId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CorridorResponse create(CreateCorridorRequest request, Long adminId) {
        if (request.getSourceCountryId().equals(request.getDestinationCountryId())) {
            throw new IllegalArgumentException("Source and destination countries must be different");
        }

        Country sourceCountry = countryRepository.findById(request.getSourceCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", request.getSourceCountryId()));

        Country destinationCountry = countryRepository.findById(request.getDestinationCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", request.getDestinationCountryId()));

        Currency sourceCurrency = currencyRepository.findById(request.getSourceCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getSourceCurrencyId()));

        Currency destinationCurrency = currencyRepository.findById(request.getDestinationCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getDestinationCurrencyId()));

        if (!sourceCountry.isActive() || !sourceCountry.isAllowsSending()) {
            throw new IllegalArgumentException("Source country is not allowed for sending");
        }

        if (!destinationCountry.isActive() || !destinationCountry.isAllowsReceiving()) {
            throw new IllegalArgumentException("Destination country is not allowed for receiving");
        }

        if (!sourceCurrency.isActive()) {
            throw new IllegalArgumentException("Source currency is inactive");
        }

        if (!destinationCurrency.isActive()) {
            throw new IllegalArgumentException("Destination currency is inactive");
        }

        boolean exists = corridorRepository
                .existsBySourceCountryIdAndDestinationCountryIdAndSourceCurrencyIdAndDestinationCurrencyId(
                        sourceCountry.getId(),
                        destinationCountry.getId(),
                        sourceCurrency.getId(),
                        destinationCurrency.getId()
                );

        if (exists) {
            throw new IllegalArgumentException("This corridor already exists");
        }

        Corridor corridor = Corridor.builder()
                .sourceCountry(sourceCountry)
                .destinationCountry(destinationCountry)
                .sourceCurrency(sourceCurrency)
                .destinationCurrency(destinationCurrency)
                .active(true)
                .build();

        Corridor saved = corridorRepository.save(corridor);

        auditService.log(adminId, "CORRIDOR_CREATED", "Corridor", saved.getId(),
                "{\"source\":\"" + sourceCountry.getCode() +
                        "\",\"destination\":\"" + destinationCountry.getCode() + "\"}");

        return toResponse(saved);
    }

    @Transactional
    public CorridorResponse toggleActive(Long id, Long adminId) {
        Corridor corridor = findCorridor(id);
        corridor.setActive(!corridor.isActive());

        Corridor saved = corridorRepository.save(corridor);

        auditService.log(adminId,
                saved.isActive() ? "CORRIDOR_ACTIVATED" : "CORRIDOR_DEACTIVATED",
                "Corridor",
                id,
                null);

        return toResponse(saved);
    }

    private Corridor findCorridor(Long id) {
        return corridorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Corridor", id));
    }

    private CorridorResponse toResponse(Corridor c) {
        return CorridorResponse.builder()
                .id(c.getId())

                .sourceCountryId(c.getSourceCountry().getId())
                .sourceCountryName(c.getSourceCountry().getName())
                .sourceCountryCode(c.getSourceCountry().getCode())

                .destinationCountryId(c.getDestinationCountry().getId())
                .destinationCountryName(c.getDestinationCountry().getName())
                .destinationCountryCode(c.getDestinationCountry().getCode())

                .sourceCurrencyId(c.getSourceCurrency().getId())
                .sourceCurrencyCode(c.getSourceCurrency().getCode())

                .destinationCurrencyId(c.getDestinationCurrency().getId())
                .destinationCurrencyCode(c.getDestinationCurrency().getCode())

                .active(c.isActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}