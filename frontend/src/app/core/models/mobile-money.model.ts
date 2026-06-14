export type MobileMoneyOperator = 'ORANGE_MONEY' | 'WAVE' | 'M_PESA';

export type MobileMoneyStatus =
  | 'PENDING'
  | 'SENT'
  | 'RECONCILED'
  | 'FAILED';

export interface MobileMoneyResponse {
  id: number;
  transferId: number;
  withdrawalCode: string;
  operator: MobileMoneyOperator;
  walletPhone: string;
  status: MobileMoneyStatus;
  operatorReference: string | null;
  sentAt: string | null;
  reconciledAt: string | null;
}

export interface CreateMobileMoneyRequest {
  transferId: number;
  operator: MobileMoneyOperator;
  walletPhone: string;
}
