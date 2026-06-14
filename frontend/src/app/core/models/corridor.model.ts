export interface Corridor {
    id: number;
    sourceCountryId: number;
    sourceCountryName: string;
    sourceCountryCode: string;
    destinationCountryId: number;
    destinationCountryName: string;
    destinationCountryCode: string;
    sourceCurrencyId: number;
    sourceCurrencyCode: string;
    destinationCurrencyId: number;
    destinationCurrencyCode: string;
    active: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface CreateCorridorRequest {
    sourceCountryId: number;
    destinationCountryId: number;
    sourceCurrencyId: number;
    destinationCurrencyId: number;
}