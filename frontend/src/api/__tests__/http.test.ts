import { describe, it, expect, vi, beforeEach } from 'vitest'

// We need to mock the router module before importing http
vi.mock('@/router', () => ({
  default: {
    currentRoute: { value: { name: 'home', fullPath: '/' } },
    push: vi.fn(),
  },
}))

import { http } from '../http'

describe('http interceptors', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('request interceptor adds Authorization header when token exists', async () => {
    localStorage.setItem('lifepilot.accessToken', 'test-token')

    const result = http.interceptors.request.handlers?.[0]?.fulfilled?.({ headers: {} } as any)
    const config = result instanceof Promise ? await result : result
    expect(config?.headers?.Authorization).toBe('Bearer test-token')
  })

  it('request interceptor does not add header when no token', async () => {
    const result = http.interceptors.request.handlers?.[0]?.fulfilled?.({ headers: {} } as any)
    const config = result instanceof Promise ? await result : result
    expect(config?.headers?.Authorization).toBeUndefined()
  })

  it('response interceptor passes through successful responses', async () => {
    const handler = http.interceptors.response.handlers?.[0]?.fulfilled
    expect(handler).toBeDefined()

    const mockResponse = { status: 200, data: { success: true } }
    const result = handler?.(mockResponse as any)
    expect(result).toEqual(mockResponse)
  })

  it('response interceptor rejects errors', async () => {
    const handler = http.interceptors.response.handlers?.[0]?.rejected
    expect(handler).toBeDefined()

    const error = { response: { status: 500 } }
    await expect(handler?.(error as any)).rejects.toBe(error)
  })

  it('has correct base configuration', () => {
    expect(http.defaults.timeout).toBe(10000)
  })
})