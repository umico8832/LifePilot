import axios from 'axios'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('lifepilot.accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('lifepilot.accessToken')
      // Use dynamic import to avoid circular dependency with router
      const { default: router } = await import('@/router')
      const currentRoute = router.currentRoute.value
      if (currentRoute.name !== 'auth') {
        router.push({ name: 'auth', query: { redirect: currentRoute.fullPath } })
      }
    }
    return Promise.reject(error)
  },
)
