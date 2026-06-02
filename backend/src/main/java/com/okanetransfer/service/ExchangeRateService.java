package com.okanetransfer.service;

import com.okanetransfer.dto.response.ExchangeRateHistoryDto;
import com.okanetransfer.dto.response.ExchangeRateResponseDto;
import com.okanetransfer.dto.response.PaginationResponse;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.ExchangeRate;
import com.okanetransfer.entity.ExchangeRateProvider;
import com.okanetransfer.mapper.ExchangeRateMapper;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.ExchangeRateProviderRepository;
import com.okanetransfer.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final CorridorRepository corridorRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateProviderRepository providerRepository;
    private final ExchangeRateClientRouter router;



    @Transactional
    public ExchangeRateResponseDto refresh(Long corridorId) {

        Corridor corridor = corridorRepository.findById(corridorId)
                .orElseThrow(() -> new RuntimeException("Corridor not found"));

        ExchangeRateProvider provider = providerRepository.findByActiveTrue()
                .orElseThrow(() -> new RuntimeException("No active provider"));

        BigDecimal newRate = router.getRate(
                corridor.getSourceCurrency().getCode(),
                corridor.getDestinationCurrency().getCode(),
                provider
        );

        ExchangeRate last = exchangeRateRepository
                .findByCorridorIdAndIsCurrentTrue(corridorId)
                .orElse(null);

        if (last != null && last.getRate().compareTo(newRate) == 0) {
            return ExchangeRateMapper.toDto(last);
        }

        if (last != null) {
            last.setCurrent(false);
        }

        ExchangeRate rate = ExchangeRate.builder()
                .corridor(corridor)
                .provider(provider)
                .rate(newRate)
                .isCurrent(true)
                .build();

        ExchangeRate saved = exchangeRateRepository.save(rate);

        return ExchangeRateMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public ExchangeRateResponseDto getCurrentRate(Long corridorId) {

        ExchangeRate exchangeRate =
                exchangeRateRepository
                        .findByCorridorIdAndIsCurrentTrue(corridorId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No current exchange rate found for corridor "
                                                + corridorId));

        return ExchangeRateMapper.toDto(exchangeRate);
    }

    @Transactional(readOnly = true)
    public PaginationResponse<ExchangeRateHistoryDto> getHistory(
            Long corridorId,
            int page,
            int size) {

        if (page < 0) page = 0;
        if (size <= 0 || size > 50) size = 10;

        Pageable pageable = PageRequest.of(page, size);

        Page<ExchangeRate> result =
                exchangeRateRepository
                        .findByCorridorIdOrderByRecordedAtDesc(
                                corridorId,
                                pageable
                        );

        List<ExchangeRateHistoryDto> content =
                result.getContent()
                        .stream()
                        .map(ExchangeRateMapper::toHistoryDto)
                        .toList();

        return PaginationResponse.<ExchangeRateHistoryDto>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

}
