export type KycStatus = 'PASSED' | 'FLAGGED' | 'BLOCKED';

export interface KycRecord {
  id: number;
  transferId: number;
  withdrawalCode: string;
  status: KycStatus;
  watchlistHit: boolean;
  suspicionDeclared: boolean;
  riskScore: number;           // 0–100
  notes: string | null;
  checkedById: number | null;
  checkedByEmail: string | null;
  checkedAt: string;
}

export interface KycReviewRequest {
  status: KycStatus;
  notes: string;
}
