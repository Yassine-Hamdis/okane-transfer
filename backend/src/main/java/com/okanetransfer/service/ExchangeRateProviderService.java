package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateExchangeRateProviderRequest;
import com.okanetransfer.dto.request.UpdateExchangeRateProviderRequest;
import com.okanetransfer.dto.response.ExchangeRateProviderResponseDto;
import com.okanetransfer.entity.ExchangeRateProvider;
import com.okanetransfer.mapper.ExchangeRateProviderMapper;
import com.okanetransfer.repository.ExchangeRateProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeRateProviderService {

    private final ExchangeRateProviderRepository repository;

    @Transactional
    public ExchangeRateProviderResponseDto create(
            CreateExchangeRateProviderRequest request) {

        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("Provider name already exists");
        }

        ExchangeRateProvider provider =
                ExchangeRateProvider.builder()
                        .name(request.getName())
                        .baseUrl(request.getBaseUrl())
                        .apiKey(request.getApiKey())
                        .description(request.getDescription())
                        .build();

        return ExchangeRateProviderMapper.toDto(
                repository.save(provider)
        );
    }

    @Transactional
    public ExchangeRateProviderResponseDto update(
            Long id,
            UpdateExchangeRateProviderRequest request) {

        ExchangeRateProvider provider =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Provider not found"));

        if (repository.existsByNameAndIdNot(
                request.getName(),
                id)) {
            throw new RuntimeException("Provider name already exists");
        }
        if (provider.isActive()) {
            throw new RuntimeException(
                    "Active provider cannot be modified. Activate another provider first."
            );
        }

        provider.setName(request.getName());
        provider.setBaseUrl(request.getBaseUrl());
        provider.setApiKey(request.getApiKey());
        provider.setDescription(request.getDescription());

        return ExchangeRateProviderMapper.toDto(provider);
    }

    @Transactional
    public ExchangeRateProviderResponseDto activate(Long id) {

        ExchangeRateProvider provider =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Provider not found"));

        repository.deactivateAll();

        provider.setActive(true);

        return ExchangeRateProviderMapper.toDto(provider);
    }



    @Transactional(readOnly = true)
    public List<ExchangeRateProviderResponseDto> findAll() {

        return repository.findAll()
                .stream()
                .map(ExchangeRateProviderMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExchangeRateProviderResponseDto getActiveProvider() {

        return repository.findByActiveTrue()
                .map(ExchangeRateProviderMapper::toDto)
                .orElseThrow(() ->
                        new RuntimeException("No active provider found"));
    }
}
