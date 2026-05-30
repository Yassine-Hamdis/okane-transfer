package com.okanetransfer.stub;

import com.okanetransfer.entity.enums.TransferType;
import com.okanetransfer.service.TransferService.FeeGridServicePort;
import com.okanetransfer.service.TransferService.FeeSimulationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * TEMPORARY STUB — Person 2 placeholder for FeeGridServicePort.
 *
 * This bean is ONLY active when no real FeeGridServicePort implementation
 * exists (i.e. while Person 2 hasn't delivered yet).
 *
 * Once Person 2 delivers their FeeGridService implementing FeeGridServicePort,
 * Spring will use theirs automatically and this stub will be ignored
 * because @ConditionalOnMissingBean deactivates it.
 *
 * HOW THE STUB CALCULATES (hardcoded for dev/testing only):
 *   fee        = 2% of sent amount
 *   received   = (sentAmount - fee) * 100  (exchange rate = 100 MAD → XOF)
 *   receivedCurrencyId = 2L  (XOF — adjust if needed)
 *
 * DO NOT use in production.
 */
@Service
@ConditionalOnMissingBean(
        value = FeeGridServicePort.class,
        ignored = FeeGridServiceStub.class
)
public class FeeGridServiceStub implements FeeGridServicePort {

    private static final Logger log = LoggerFactory.getLogger(FeeGridServiceStub.class);

    // Hardcoded stub values — replace with real service when Person 2 delivers
    private static final BigDecimal STUB_FEE_PERCENTAGE  = new BigDecimal("2.00");
    private static final BigDecimal STUB_EXCHANGE_RATE   = new BigDecimal("100.00");
    private static final Long       STUB_RECEIVED_CCY_ID = 2L;  // XOF

    @Override
    public void assertCorridorActive(Long corridorId) {
        log.warn("[STUB] assertCorridorActive called — stub always passes. corridorId={}", corridorId);
        // Stub: always active — real service will validate against DB
    }

    @Override
    public FeeSimulationResult simulateFee(Long corridorId,
                                            BigDecimal amount,
                                            Long currencyId,
                                            TransferType type) {
        log.warn("[STUB] simulateFee called — using hardcoded 2% fee + rate 100. " +
                 "corridorId={}, amount={}, type={}", corridorId, amount, type);

        // fee = 2% of sent amount
        BigDecimal feePercentage = STUB_FEE_PERCENTAGE;
        BigDecimal feeFixed      = BigDecimal.ZERO;
        BigDecimal feeAmount     = amount
                .multiply(feePercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // received = (amount - fee) * exchangeRate
        BigDecimal netAmount     = amount.subtract(feeAmount);
        BigDecimal received      = netAmount.multiply(STUB_EXCHANGE_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        return new FeeSimulationResult(
                feeAmount,
                feeFixed,
                feePercentage,
                received,
                STUB_RECEIVED_CCY_ID,
                STUB_EXCHANGE_RATE
        );
    }
}
