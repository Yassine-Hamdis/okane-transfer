package com.okanetransfer.service;

import com.okanetransfer.dto.request.UpdateRateRequest;
import com.okanetransfer.dto.response.ExchangeRateResponse;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.ExchangeRate;
import com.okanetransfer.entity.User;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.ExchangeRateRepository;
import com.okanetransfer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class ExchangeRateService {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private CorridorRepository corridorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Transactional(readOnly = true)
    public ExchangeRateResponse getCurrentRate(Long corridorId) {
        ExchangeRate rate = exchangeRateRepository
                .findByCorridorIdAndIsCurrentTrue(corridorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Current exchange rate not found for corridor: " + corridorId));

        return toResponse(rate);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateResponse> getHistory(Long corridorId) {
        return exchangeRateRepository.findAllByCorridorIdOrderByRecordedAtDesc(corridorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExchangeRateResponse updateManually(Long corridorId,
                                               UpdateRateRequest request,
                                               Long adminId) {
        Corridor corridor = corridorRepository.findById(corridorId)
                .orElseThrow(() -> new ResourceNotFoundException("Corridor", corridorId));

        if (!corridor.isActive()) {
            throw new IllegalArgumentException("Cannot update rate for inactive corridor");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));

        exchangeRateRepository.deactivateAllByCorridorId(corridorId);

        ExchangeRate newRate = ExchangeRate.builder()
                .corridor(corridor)
                .rate(request.getRate())
                .source("MANUAL")
                .updatedBy(admin)
                .isCurrent(true)
                .build();

        ExchangeRate saved = exchangeRateRepository.save(newRate);

        auditService.log(adminId, "RATE_UPDATED", "ExchangeRate", saved.getId(),
                "{\"corridorId\":" + corridorId + ",\"rate\":\"" + request.getRate() + "\"}");

        return toResponse(saved);
    }

    private ExchangeRateResponse toResponse(ExchangeRate r) {
        Corridor c = r.getCorridor();

        String label = format("%s → %s (%s → %s)",
                c.getSourceCountry().getCode(),
                c.getDestinationCountry().getCode(),
                c.getSourceCurrency().getCode(),
                c.getDestinationCurrency().getCode());

        return ExchangeRateResponse.builder()
                .id(r.getId())
                .corridorId(c.getId())
                .corridorLabel(label)
                .rate(r.getRate())
                .source(r.getSource())
                .current(r.isCurrent())
                .updatedById(r.getUpdatedBy() != null ? r.getUpdatedBy().getId() : null)
                .updatedByEmail(r.getUpdatedBy() != null ? r.getUpdatedBy().getEmail() : null)
                .recordedAt(r.getRecordedAt())
                .build();
    }
}