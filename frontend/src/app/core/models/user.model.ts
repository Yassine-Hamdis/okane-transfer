export type UserRole =
  | 'ROLE_ADMIN'
  | 'ROLE_MANAGER'
  | 'ROLE_AGENT'
  | 'ROLE_CLIENT';

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: UserRole;
  active: boolean;
  twoFactorEnabled: boolean;
  mustChangePassword: boolean;
  agencyId: number | null;
  agencyName: string | null;
  createdAt: string; // ISO-8601 string from backend
}

// ── Auth ──────────────────────────────────────────────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string | null;       // null/absent if 2FA required
  role: UserRole;
  fullName: string;
  userId: number;
  mustChangePassword: boolean;
  requiresTwoFactor: boolean;
}

export interface TwoFactorRequest {
  email: string;
  otpCode: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone: string;
}

// ── Admin user management ─────────────────────────────────────────────────────

export interface CreateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone: string;
  role: UserRole;
  agencyId?: number | null;
}

export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: UserRole;
  agencyId?: number | null;
}

export interface ResetPasswordRequest {
  newPassword: string;
}

// ── Client self-service ───────────────────────────────────────────────────────

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phone: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

// ── localStorage session ──────────────────────────────────────────────────────

export interface SessionData {
  token: string;
  role: UserRole;
  userId: number;
  fullName: string;
}
