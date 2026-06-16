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

// ── What the BACKEND actually expects (matches backend DTO field names) ────────
export interface CreateFeeGridRequest {
  corridorId:          number;
  currencyId:          number;    // ← required by backend
  minAmount:           number;
  maxAmount:           number;
  feeFixed:            number;
  feePercentage:       number;
  transferType:        TransferType;
  agencySharePercent:  number;    // ← backend name (no 'age')
  centralSharePercent: number;    // ← backend name (no 'age')
}

export interface UpdateFeeGridRequest {
  currencyId:          number;    // ← required by backend
  minAmount:           number;
  maxAmount:           number;
  feeFixed:            number;
  feePercentage:       number;
  transferType:        TransferType;
  agencySharePercent:  number;    // ← backend name
  centralSharePercent: number;    // ← backend name
}

// ── Fee simulation ─────────────────────────────────────────────────────────────
export interface FeeSimulateRequest {
  corridorId:   number;
  currencyId:   number;
  amount:       number;
  transferType: TransferType;
}

export interface FeeSimulationResponse {
  feeGridId:        number;
  sentAmount:       number;
  sentCurrency:     string;
  feeFixedAmount:   number;
  feePercentage:    number;
  feeAmount:        number;
  amountAfterFee:   number;
  exchangeRate:     number;
  receivedAmount:   number;
  receivedCurrency: string;
  agencyShare:      number;
  centralShare:     number;
  transferType:     string;
}
