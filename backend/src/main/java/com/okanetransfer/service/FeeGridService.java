package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateFeeGridRequest;
import com.okanetransfer.dto.request.FeeSimulationRequest;
import com.okanetransfer.dto.request.UpdateFeeGridRequest;
import com.okanetransfer.dto.response.FeeGridResponse;
import com.okanetransfer.dto.response.FeeSimulationResponse;
import com.okanetransfer.entity.Corridor;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.entity.ExchangeRate;
import com.okanetransfer.entity.FeeGrid;
import com.okanetransfer.entity.enums.TransferType;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.CorridorRepository;
import com.okanetransfer.repository.CurrencyRepository;
import com.okanetransfer.repository.ExchangeRateRepository;
import com.okanetransfer.repository.FeeGridRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeeGridService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Autowired
    private FeeGridRepository feeGridRepository;

    @Autowired
    private CorridorRepository corridorRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private AuditService auditService;

    @Transactional(readOnly = true)
    public List<FeeGridResponse> getAll() {
        return feeGridRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeeGridResponse getById(Long id) {
        return toResponse(findFeeGrid(id));
    }

    @Transactional(readOnly = true)
    public List<FeeGridResponse> getByCorridor(Long corridorId) {
        return feeGridRepository.findAllByCorridorId(corridorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeeGridResponse create(CreateFeeGridRequest request, Long adminId) {
        Corridor corridor = findCorridor(request.getCorridorId());
        Currency currency = findCurrency(request.getCurrencyId());

        validateCorridorAndCurrency(corridor, currency);
        validateFeeGridValues(
                request.getMinAmount(),
                request.getMaxAmount(),
                request.getFeeFixedAmount(),
                request.getFeePercentage(),
                request.getAgencySharePercent(),
                request.getCentralSharePercent()
        );

        validateNoOverlap(
                corridor.getId(),
                currency.getId(),
                request.getTransferType(),
                request.getMinAmount(),
                request.getMaxAmount(),
                null
        );

        FeeGrid feeGrid = FeeGrid.builder()
                .corridor(corridor)
                .currency(currency)
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .feeFixedAmount(request.getFeeFixedAmount())
                .feePercentage(request.getFeePercentage())
                .agencySharePercent(request.getAgencySharePercent())
                .centralSharePercent(request.getCentralSharePercent())
                .transferType(request.getTransferType())
                .active(true)
                .build();

        FeeGrid saved = feeGridRepository.save(feeGrid);

        auditService.log(adminId, "FEE_GRID_CREATED", "FeeGrid", saved.getId(),
                "{\"corridorId\":" + corridor.getId() +
                        ",\"transferType\":\"" + request.getTransferType() + "\"}");

        return toResponse(saved);
    }

    @Transactional
    public FeeGridResponse update(Long id, UpdateFeeGridRequest request, Long adminId) {
        FeeGrid feeGrid = findFeeGrid(id);

        Corridor corridor = findCorridor(request.getCorridorId());
        Currency currency = findCurrency(request.getCurrencyId());

        validateCorridorAndCurrency(corridor, currency);
        validateFeeGridValues(
                request.getMinAmount(),
                request.getMaxAmount(),
                request.getFeeFixedAmount(),
                request.getFeePercentage(),
                request.getAgencySharePercent(),
                request.getCentralSharePercent()
        );

        if (feeGrid.isActive()) {
            validateNoOverlap(
                    corridor.getId(),
                    currency.getId(),
                    request.getTransferType(),
                    request.getMinAmount(),
                    request.getMaxAmount(),
                    id
            );
        }

        feeGrid.setCorridor(corridor);
        feeGrid.setCurrency(currency);
        feeGrid.setMinAmount(request.getMinAmount());
        feeGrid.setMaxAmount(request.getMaxAmount());
        feeGrid.setFeeFixedAmount(request.getFeeFixedAmount());
        feeGrid.setFeePercentage(request.getFeePercentage());
        feeGrid.setAgencySharePercent(request.getAgencySharePercent());
        feeGrid.setCentralSharePercent(request.getCentralSharePercent());
        feeGrid.setTransferType(request.getTransferType());

        FeeGrid saved = feeGridRepository.save(feeGrid);

        auditService.log(adminId, "FEE_GRID_UPDATED", "FeeGrid", id, null);

        return toResponse(saved);
    }

    @Transactional
    public FeeGridResponse toggleActive(Long id, Long adminId) {
        FeeGrid feeGrid = findFeeGrid(id);

        if (!feeGrid.isActive()) {
            validateNoOverlap(
                    feeGrid.getCorridor().getId(),
                    feeGrid.getCurrency().getId(),
                    feeGrid.getTransferType(),
                    feeGrid.getMinAmount(),
                    feeGrid.getMaxAmount(),
                    feeGrid.getId()
            );
        }

        feeGrid.setActive(!feeGrid.isActive());

        FeeGrid saved = feeGridRepository.save(feeGrid);

        auditService.log(adminId,
                saved.isActive() ? "FEE_GRID_ACTIVATED" : "FEE_GRID_DEACTIVATED",
                "FeeGrid",
                id,
                null);

        return toResponse(saved);
    }

    /**
     * This method is important for Person 3.
     * TransferService can call it before creating a transfer.
     */
    @Transactional(readOnly = true)
    public FeeSimulationResponse simulateFee(FeeSimulationRequest request) {
        Corridor corridor = findCorridor(request.getCorridorId());
        Currency currency = findCurrency(request.getCurrencyId());

        validateCorridorAndCurrency(corridor, currency);

        TransferType transferType = request.getTransferType() != null
                ? request.getTransferType()
                : TransferType.STANDARD;

        ExchangeRate currentRate = exchangeRateRepository
                .findByCorridorIdAndIsCurrentTrue(corridor.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Current exchange rate not found for corridor: " + corridor.getId()));

        FeeGrid feeGrid = feeGridRepository
                .findApplicableGrid(
                        corridor.getId(),
                        currency.getId(),
                        request.getAmount(),
                        transferType
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active fee grid found for this amount/corridor/type"));

        BigDecimal fixed = safe(feeGrid.getFeeFixedAmount());
        BigDecimal percentage = safe(feeGrid.getFeePercentage());

        BigDecimal percentageFee = request.getAmount()
                .multiply(percentage)
                .divide(HUNDRED, 6, RoundingMode.HALF_UP);

        BigDecimal feeAmount = fixed.add(percentageFee)
                .setScale(2, RoundingMode.HALF_UP);

        if (feeAmount.compareTo(request.getAmount()) >= 0) {
            throw new IllegalArgumentException("Fee amount cannot be greater than or equal to sent amount");
        }

        BigDecimal amountAfterFee = request.getAmount()
                .subtract(feeAmount)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal receivedAmount = amountAfterFee
                .multiply(currentRate.getRate())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal agencyShare = feeAmount
                .multiply(BigDecimal.valueOf(feeGrid.getAgencySharePercent()))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        BigDecimal centralShare = feeAmount.subtract(agencyShare)
                .setScale(2, RoundingMode.HALF_UP);

        return FeeSimulationResponse.builder()
                .feeGridId(feeGrid.getId())
                .sentAmount(request.getAmount())
                .sentCurrency(currency.getCode())
                .feeFixedAmount(fixed)
                .feePercentage(percentage)
                .feeAmount(feeAmount)
                .amountAfterFee(amountAfterFee)
                .exchangeRate(currentRate.getRate())
                .receivedAmount(receivedAmount)
                .receivedCurrency(corridor.getDestinationCurrency().getCode())
                .agencyShare(agencyShare)
                .centralShare(centralShare)
                .transferType(transferType)
                .build();
    }

    private FeeGrid findFeeGrid(Long id) {
        return feeGridRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeGrid", id));
    }

    private Corridor findCorridor(Long id) {
        return corridorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Corridor", id));
    }

    private Currency findCurrency(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", id));
    }

    private void validateCorridorAndCurrency(Corridor corridor, Currency currency) {
        if (!corridor.isActive()) {
            throw new IllegalArgumentException("Corridor is inactive");
        }

        if (!currency.isActive()) {
            throw new IllegalArgumentException("Currency is inactive");
        }

        if (!corridor.getSourceCurrency().getId().equals(currency.getId())) {
            throw new IllegalArgumentException(
                    "Fee currency must match corridor source currency: "
                            + corridor.getSourceCurrency().getCode());
        }
    }

    private void validateFeeGridValues(BigDecimal minAmount,
                                       BigDecimal maxAmount,
                                       BigDecimal feeFixed,
                                       BigDecimal feePercentage,
                                       Integer agencyShare,
                                       Integer centralShare) {
        if (minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("minAmount cannot be greater than maxAmount");
        }

        BigDecimal fixed = safe(feeFixed);
        BigDecimal percentage = safe(feePercentage);

        if (fixed.compareTo(BigDecimal.ZERO) == 0 &&
                percentage.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("At least one fee value is required");
        }

        if (agencyShare == null || centralShare == null ||
                agencyShare + centralShare != 100) {
            throw new IllegalArgumentException("Agency share + central share must equal 100");
        }
    }

    private void validateNoOverlap(Long corridorId,
                                   Long currencyId,
                                   TransferType transferType,
                                   BigDecimal minAmount,
                                   BigDecimal maxAmount,
                                   Long excludedId) {
        List<FeeGrid> existingGrids =
                feeGridRepository.findAllByCorridorIdAndCurrencyIdAndTransferTypeAndActiveTrue(
                        corridorId,
                        currencyId,
                        transferType
                );

        for (FeeGrid existing : existingGrids) {
            if (excludedId != null && existing.getId().equals(excludedId)) {
                continue;
            }

            boolean overlaps =
                    minAmount.compareTo(existing.getMaxAmount()) <= 0 &&
                            maxAmount.compareTo(existing.getMinAmount()) >= 0;

            if (overlaps) {
                throw new IllegalArgumentException(
                        "Fee grid range overlaps with existing active grid id=" + existing.getId());
            }
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private FeeGridResponse toResponse(FeeGrid f) {
        Corridor c = f.getCorridor();

        String corridorLabel = c.getSourceCountry().getCode()
                + " → "
                + c.getDestinationCountry().getCode()
                + " ("
                + c.getSourceCurrency().getCode()
                + " → "
                + c.getDestinationCurrency().getCode()
                + ")";

        return FeeGridResponse.builder()
                .id(f.getId())
                .corridorId(c.getId())
                .corridorLabel(corridorLabel)
                .currencyId(f.getCurrency().getId())
                .currencyCode(f.getCurrency().getCode())
                .minAmount(f.getMinAmount())
                .maxAmount(f.getMaxAmount())
                .feeFixedAmount(f.getFeeFixedAmount())
                .feePercentage(f.getFeePercentage())
                .agencySharePercent(f.getAgencySharePercent())
                .centralSharePercent(f.getCentralSharePercent())
                .transferType(f.getTransferType())
                .active(f.isActive())
                .build();
    }
}
