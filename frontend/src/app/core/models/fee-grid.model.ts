export type TransferType = 'STANDARD' | 'EXPRESS' | 'MOBILE_MONEY';

export interface FeeGrid {
    id: number;
    corridorId: number;
    corridorLabel: string;
    minAmount: number;
    maxAmount: number;
    feeFixed: number;
    feePercentage: number;
    transferType: TransferType;
    agencySharePercentage: number;
    centralSharePercentage: number;
    active: boolean;
}

export interface CreateFeeGridRequest {
    corridorId: number;
    minAmount: number;
    maxAmount: number;
    feeFixed: number;
    feePercentage: number;
    transferType: TransferType;
    agencySharePercentage: number;
    centralSharePercentage: number;
}

export interface UpdateFeeGridRequest {
    minAmount: number;
    maxAmount: number;
    feeFixed: number;
    feePercentage: number;
    transferType: TransferType;
    agencySharePercentage: number;
    centralSharePercentage: number;
}

// ── Fee simulation ────────────────────────────────────────────────────────────

export interface FeeSimulateRequest {
    corridorId: number;
    currencyId: number;
    amount: number;
    transferType: TransferType;
}

export interface FeeSimulationResponse {
    feeGridId: number;
    sentAmount: number;
    sentCurrency: string;
    feeFixedAmount: number;
    feePercentage: number;
    feeAmount: number;
    amountAfterFee: number;
    exchangeRate: number;
    receivedAmount: number;
    receivedCurrency: string;
    agencyShare: number;
    centralShare: number;
    transferType: string;
}