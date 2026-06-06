package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateCountryRequest;
import com.okanetransfer.dto.request.UpdateCountryRequest;
import com.okanetransfer.dto.response.CountryResponse;
import com.okanetransfer.entity.Country;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CountryService {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private AuditService auditService;

    @Transactional(readOnly = true)
    public List<CountryResponse> getAll() {
        return countryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> getActive() {
        return countryRepository.findAllByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> getSendingCountries() {
        return countryRepository.findAllByAllowsSendingTrue()
                .stream()
                .filter(Country::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> getReceivingCountries() {
        return countryRepository.findAllByAllowsReceivingTrue()
                .stream()
                .filter(Country::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CountryResponse getById(Long id) {
        return toResponse(findCountry(id));
    }

    @Transactional
    public CountryResponse create(CreateCountryRequest request, Long adminId) {
        String code = normalizeCode(request.getCode());

        if (countryRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Country code already exists: " + code);
        }

        if (countryRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Country name already exists: " + request.getName());
        }

        Country country = Country.builder()
                .name(request.getName())
                .code(code)
                .allowsSending(request.getAllowsSending() == null || request.getAllowsSending())
                .allowsReceiving(request.getAllowsReceiving() == null || request.getAllowsReceiving())
                .active(true)
                .build();

        Country saved = countryRepository.save(country);

        auditService.log(adminId, "COUNTRY_CREATED", "Country", saved.getId(),
                "{\"code\":\"" + code + "\"}");

        return toResponse(saved);
    }

    @Transactional
    public CountryResponse update(Long id, UpdateCountryRequest request, Long adminId) {
        Country country = findCountry(id);
        String code = normalizeCode(request.getCode());

        countryRepository.findByCode(code).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Country code already exists: " + code);
            }
        });

        countryRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Country name already exists: " + request.getName());
            }
        });

        country.setName(request.getName());
        country.setCode(code);
        country.setAllowsSending(request.getAllowsSending() == null || request.getAllowsSending());
        country.setAllowsReceiving(request.getAllowsReceiving() == null || request.getAllowsReceiving());

        Country saved = countryRepository.save(country);

        auditService.log(adminId, "COUNTRY_UPDATED", "Country", id, null);

        return toResponse(saved);
    }

    @Transactional
    public CountryResponse toggleActive(Long id, Long adminId) {
        Country country = findCountry(id);
        country.setActive(!country.isActive());
        Country saved = countryRepository.save(country);

        auditService.log(adminId,
                saved.isActive() ? "COUNTRY_ACTIVATED" : "COUNTRY_DEACTIVATED",
                "Country",
                id,
                null);

        return toResponse(saved);
    }

    private Country findCountry(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", id));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private CountryResponse toResponse(Country c) {
        return CountryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .code(c.getCode())
                .allowsSending(c.isAllowsSending())
                .allowsReceiving(c.isAllowsReceiving())
                .active(c.isActive())
                .build();
    }
}