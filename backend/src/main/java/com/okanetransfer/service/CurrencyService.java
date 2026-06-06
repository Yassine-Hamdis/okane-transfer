package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateCurrencyRequest;
import com.okanetransfer.dto.request.UpdateCurrencyRequest;
import com.okanetransfer.dto.response.CurrencyResponse;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private AuditService auditService;

    @Transactional(readOnly = true)
    public List<CurrencyResponse> getAll() {
        return currencyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CurrencyResponse> getActive() {
        return currencyRepository.findAllByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CurrencyResponse getById(Long id) {
        return toResponse(findCurrency(id));
    }

    @Transactional
    public CurrencyResponse create(CreateCurrencyRequest request, Long adminId) {
        String code = normalizeCode(request.getCode());

        if (currencyRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Currency code already exists: " + code);
        }

        Currency currency = Currency.builder()
                .code(code)
                .name(request.getName())
                .symbol(request.getSymbol())
                .active(true)
                .build();

        Currency saved = currencyRepository.save(currency);

        auditService.log(adminId, "CURRENCY_CREATED", "Currency", saved.getId(),
                "{\"code\":\"" + code + "\"}");

        return toResponse(saved);
    }

    @Transactional
    public CurrencyResponse update(Long id, UpdateCurrencyRequest request, Long adminId) {
        Currency currency = findCurrency(id);
        String code = normalizeCode(request.getCode());

        currencyRepository.findByCode(code).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Currency code already exists: " + code);
            }
        });

        currency.setCode(code);
        currency.setName(request.getName());
        currency.setSymbol(request.getSymbol());

        Currency saved = currencyRepository.save(currency);

        auditService.log(adminId, "CURRENCY_UPDATED", "Currency", id, null);

        return toResponse(saved);
    }

    @Transactional
    public CurrencyResponse toggleActive(Long id, Long adminId) {
        Currency currency = findCurrency(id);
        currency.setActive(!currency.isActive());

        Currency saved = currencyRepository.save(currency);

        auditService.log(adminId,
                saved.isActive() ? "CURRENCY_ACTIVATED" : "CURRENCY_DEACTIVATED",
                "Currency",
                id,
                null);

        return toResponse(saved);
    }

    private Currency findCurrency(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", id));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private CurrencyResponse toResponse(Currency c) {
        return CurrencyResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .symbol(c.getSymbol())
                .active(c.isActive())
                .build();
    }
}