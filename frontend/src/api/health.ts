import { http } from './http'
import type { ApiResponse } from '@/types/auth'

export interface HealthData {
  status: string
  service: string
  timestamp: string
}

export async function fetchHealth() {
  const response = await http.get<ApiResponse<HealthData>>('/api/health')
  return response.data
}

