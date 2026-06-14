export interface Agency {
  id: number;
  name: string;
  address: string;
  city: string;
  countryName: string;
  countryCode: string;
  managerId: number | null;
  managerName: string | null;
  dailyLimit: number;
  active: boolean;
  createdAt: string;
}

export interface CreateAgencyRequest {
  name: string;
  address: string;
  city: string;
  countryId: number;
  dailyLimit: number;
}

export interface UpdateAgencyRequest {
  name: string;
  address: string;
  city: string;
  countryId: number;
  dailyLimit: number;
}

export interface AssignManagerRequest {
  managerId: number;
}
