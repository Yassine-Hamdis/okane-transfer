export type CashOperationType =
  | 'ENVOI'
  | 'RETRAIT'
  | 'ANNULATION'
  | 'CLOTURE_CAISSE';

export interface CashBalance {
  currencyId: number;
  currencyCode: string;
  currencySymbol: string;
  currentBalance: number;
  updatedAt: string | null;
}

export interface CashRegisterResponse {
  id: number;
  agencyId: number;
  agencyName: string;
  balances: CashBalance[];
  lastClosedAt: string | null;
}

export interface CashOperationResponse {
  id: number;
  type: CashOperationType;
  amount: number;
  currencyCode: string;
  balanceAfter: number;
  agentId: number;
  agentName: string;
  transferId: number | null;
  withdrawalCode: string | null;
  note: string | null;
  createdAt: string;
}

export interface CloseRegisterRequest {
  note: string | null;
}

export interface DiscrepancyRequest {
  currencyId: number;
  amount: number;
  note: string;
}
