import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

import { http } from '../http'
import {
  listInventoryItems,
  getInventoryItem,
  createInventoryItem,
  updateInventoryItem,
  deleteInventoryItem,
  listInventoryAlerts,
} from '../inventory'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)
const mockedPatch = vi.mocked(http.patch)
const mockedDelete = vi.mocked(http.delete)

describe('inventory API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const mockItem = {
    id: 1,
    householdId: 1,
    name: '大米',
    category: '食品',
    quantity: 5,
    unit: 'kg',
    location: '厨房',
    expireAt: '2026-12-01',
    lowStockThreshold: 2,
    lowStock: false,
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
  }

  it('listInventoryItems calls GET with correct path', async () => {
    mockedGet.mockResolvedValue({ data: { data: [mockItem] } })

    const result = await listInventoryItems(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/inventory-items')
    expect(result).toEqual([mockItem])
  })

  it('getInventoryItem calls GET with correct path', async () => {
    mockedGet.mockResolvedValue({ data: { data: mockItem } })

    const result = await getInventoryItem(1, 1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/inventory-items/1')
    expect(result).toEqual(mockItem)
  })

  it('createInventoryItem calls POST with payload', async () => {
    mockedPost.mockResolvedValue({ data: { data: mockItem } })

    const payload = { name: '大米', category: '食品', quantity: 5, unit: 'kg', location: '厨房' }
    const result = await createInventoryItem(1, payload)

    expect(mockedPost).toHaveBeenCalledWith('/api/spaces/1/inventory-items', payload)
    expect(result).toEqual(mockItem)
  })

  it('updateInventoryItem calls PATCH with payload', async () => {
    const updated = { ...mockItem, quantity: 3 }
    mockedPatch.mockResolvedValue({ data: { data: updated } })

    const result = await updateInventoryItem(1, 1, { quantity: 3 })

    expect(mockedPatch).toHaveBeenCalledWith('/api/spaces/1/inventory-items/1', { quantity: 3 })
    expect(result).toEqual(updated)
  })

  it('deleteInventoryItem calls DELETE', async () => {
    mockedDelete.mockResolvedValue({})

    await deleteInventoryItem(1, 1)

    expect(mockedDelete).toHaveBeenCalledWith('/api/spaces/1/inventory-items/1')
  })

  it('listInventoryAlerts calls alerts endpoint', async () => {
    const alertItem = { ...mockItem, lowStock: true, quantity: 1 }
    mockedGet.mockResolvedValue({ data: { data: [alertItem] } })

    const result = await listInventoryAlerts(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/inventory-items/alerts')
    expect(result).toEqual([alertItem])
  })

  it('propagates errors from http calls', async () => {
    mockedGet.mockRejectedValue(new Error('Network error'))

    await expect(listInventoryItems(1)).rejects.toThrow('Network error')
  })
})