export interface AuditLog {
  id: number;
  userId: number | null;
  userEmail: string | null;
  action: string;
  entityType: string | null;
  entityId: number | null;
  details: string | null;
  ipAddress: string | null;
  createdAt: string;
}

export interface AuditRangeParams {
  from: string; // ISO-8601 datetime string
  to: string;
}
