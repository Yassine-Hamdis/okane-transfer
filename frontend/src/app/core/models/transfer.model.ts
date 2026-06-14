import { TransferType } from './fee-grid.model';

export type TransferStatus = 'EN_ATTENTE' | 'PAYE' | 'ANNULE' | 'EXPIRE';

export interface Transfer {
  id: number;
  withdrawalCode: string;
  senderFullName: string;
  senderPhone: string;
  senderCountry: string;
  recipientFullName: string;
  recipientPhone: string;
  recipientCountry: string;
  sentAmount: number;
  sentCurrency: string;
  feeAmount: number;
  receivedAmount: number;
  receivedCurrency: string;
  exchangeRate: number;
  feeFixed: number;
  feePercentage: number;
  transferType: TransferType;
  status: TransferStatus;
  requiresAdminApproval: boolean;
  blockedReason: string | null;
  cancellationReason: string | null;
  notes: string | null;
  sendingAgencyId: number;
  sendingAgencyName: string;
  receivingAgencyId: number | null;
  receivingAgencyName: string | null;
  sendingAgentId: number;
  sendingAgentName: string;
  receivingAgentId: number | null;
  receivingAgentName: string | null;
  clientId: number | null;
  corridorId: number;
  corridorLabel: string;
  createdAt: string;
  paidAt: string | null;
  expiresAt: string;
}

// ── Create transfer (agent sends money) ───────────────────────────────────────

export interface CreateTransferRequest {
  senderFirstName: string;
  senderLastName: string;
  senderPhone: string;
  senderIdNumber: string;
  senderEmail?: string;
  senderCountryId: number;
  recipientFirstName: string;
  recipientLastName: string;
  recipientPhone: string;
  recipientCountryId: number;
  sentAmount: number;
  sentCurrencyId: number;
  corridorId: number;
  transferType: TransferType;
  notes?: string;
}

// ── Payout ────────────────────────────────────────────────────────────────────

export interface PayoutRequest {
  withdrawalCode: string;
  recipientIdNumber: string;
}

// ── Cancel ────────────────────────────────────────────────────────────────────

export interface CancelTransferRequest {
  reason: string;
}

// ── Client summary (lighter object for list views) ────────────────────────────

export interface TransferSummary {
  id: number;
  withdrawalCode: string;
  status: TransferStatus;
  transferType: TransferType;
  recipientFullName: string;
  sentAmount: number;
  sentCurrency: string;
  receivedAmount: number;
  receivedCurrency: string;
  createdAt: string;
}

// ── Public tracking ───────────────────────────────────────────────────────────

export interface TransferTrack {
  withdrawalCode: string;
  status: TransferStatus;
  transferType: TransferType;
  recipientFullName: string;
  receivedAmount: number;
  receivedCurrency: string;
  sendingAgency: string;
  createdAt: string;
  expiresAt: string;
  paidAt: string | null;
}
