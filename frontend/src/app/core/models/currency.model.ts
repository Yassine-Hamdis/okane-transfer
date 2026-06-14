export interface Currency {
    id: number;
    code: string;    // e.g. "MAD", "EUR"
    name: string;    // e.g. "Moroccan Dirham"
    symbol: string;  // e.g. "د.م."
    active: boolean;
}

export interface CreateCurrencyRequest {
    code: string;
    name: string;
    symbol: string;
}

export interface UpdateCurrencyRequest {
    code: string;
    name: string;
    symbol: string;
}