import { defineStore } from 'pinia'

import { fetchCurrentUser, login, register } from '@/api/auth'
import type { LoginPayload, RegisterPayload } from '@/api/auth'
import type { UserProfile } from '@/types/auth'

const tokenStorageKey = 'lifepilot.accessToken'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(tokenStorageKey),
    user: null as UserProfile | null,
    loading: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    async register(payload: RegisterPayload) {
      this.loading = true
      try {
        const result = await register(payload)
        this.setSession(result.accessToken, result.user)
      } finally {
        this.loading = false
      }
    },
    async login(payload: LoginPayload) {
      this.loading = true
      try {
        const result = await login(payload)
        this.setSession(result.accessToken, result.user)
      } finally {
        this.loading = false
      }
    },
    async loadCurrentUser() {
      if (!this.token) {
        return
      }

      try {
        this.user = await fetchCurrentUser()
      } catch {
        this.logout()
      }
    },
    setSession(token: string, user: UserProfile) {
      this.token = token
      this.user = user
      localStorage.setItem(tokenStorageKey, token)
    },
    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem(tokenStorageKey)
    },
  },
})

