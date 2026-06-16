import { http } from './http'
import type { ApiResponse, AuthResponse, UserProfile } from '@/types/auth'

export interface RegisterPayload {
  email: string
  password: string
  displayName: string
}

export interface LoginPayload {
  email: string
  password: string
}

export async function register(payload: RegisterPayload) {
  const response = await http.post<ApiResponse<AuthResponse>>('/api/auth/register', payload)
  return response.data.data
}

export async function login(payload: LoginPayload) {
  const response = await http.post<ApiResponse<AuthResponse>>('/api/auth/login', payload)
  return response.data.data
}

export async function fetchCurrentUser() {
  const response = await http.get<ApiResponse<UserProfile>>('/api/users/me')
  return response.data.data
}

