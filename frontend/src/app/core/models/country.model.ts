export interface Country {
    id: number;
    name: string;
    code: string;           // ISO-2 e.g. "MA", "FR"
    allowsSending: boolean;
    allowsReceiving: boolean;
    active: boolean;
}

export interface CreateCountryRequest {
    name: string;
    code: string;
    allowsSending: boolean;
    allowsReceiving: boolean;
}

export interface UpdateCountryRequest {
    name: string;
    code: string;
    allowsSending: boolean;
    allowsReceiving: boolean;
}