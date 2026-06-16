export interface UserProfile {
  id: number
  email: string
  displayName: string
  avatarUrl?: string
}

export interface AuthResponse {
  tokenType: 'Bearer'
  accessToken: string
  user: UserProfile
}

export interface ApiResponse<T> {
  success: boolean
  code: string
  message: string
  data: T
}

