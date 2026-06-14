export interface ExchangeRate {
    id: number;
    corridorId: number;
    corridorLabel: string;  // e.g. "MA → SN"
    rate: number;
    source: string;         // e.g. "MANUAL", "API"
    current: boolean;
    updatedById: number | null;
    updatedByEmail: string | null;
    recordedAt: string;
}

export interface CreateExchangeRateRequest {
    rate: number;
}