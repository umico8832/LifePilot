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
  listShoppingLists,
  getShoppingList,
  createShoppingList,
  updateShoppingList,
  deleteShoppingList,
  addShoppingItem,
  updateShoppingItem,
  deleteShoppingItem,
} from '../shopping'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)
const mockedPatch = vi.mocked(http.patch)
const mockedDelete = vi.mocked(http.delete)

describe('shopping API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const mockList = {
    id: 1,
    householdId: 1,
    name: '本周采购',
    status: 'active',
    estimatedBudget: 500,
    createdBy: 1,
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    items: [],
  }

  const mockItem = {
    id: 1,
    shoppingListId: 1,
    name: '牛奶',
    quantity: 2,
    unit: '盒',
    estimatedPrice: 15,
    purchased: false,
    inventoryItemId: null,
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
  }

  it('listShoppingLists calls GET with correct path', async () => {
    mockedGet.mockResolvedValue({ data: { data: [mockList] } })

    const result = await listShoppingLists(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/shopping-lists')
    expect(result).toEqual([mockList])
  })

  it('getShoppingList calls GET with correct path', async () => {
    mockedGet.mockResolvedValue({ data: { data: mockList } })

    const result = await getShoppingList(1, 1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/shopping-lists/1')
    expect(result).toEqual(mockList)
  })

  it('createShoppingList calls POST with payload', async () => {
    mockedPost.mockResolvedValue({ data: { data: mockList } })

    const result = await createShoppingList(1, { name: '本周采购', estimatedBudget: 500 })

    expect(mockedPost).toHaveBeenCalledWith('/api/spaces/1/shopping-lists', {
      name: '本周采购',
      estimatedBudget: 500,
    })
    expect(result).toEqual(mockList)
  })

  it('updateShoppingList calls PATCH with payload', async () => {
    const updated = { ...mockList, status: 'completed' }
    mockedPatch.mockResolvedValue({ data: { data: updated } })

    const result = await updateShoppingList(1, 1, { status: 'completed' })

    expect(mockedPatch).toHaveBeenCalledWith('/api/spaces/1/shopping-lists/1', { status: 'completed' })
    expect(result).toEqual(updated)
  })

  it('deleteShoppingList calls DELETE', async () => {
    mockedDelete.mockResolvedValue({})

    await deleteShoppingList(1, 1)

    expect(mockedDelete).toHaveBeenCalledWith('/api/spaces/1/shopping-lists/1')
  })

  it('addShoppingItem calls POST with correct path and payload', async () => {
    mockedPost.mockResolvedValue({ data: { data: mockItem } })

    const result = await addShoppingItem(1, 1, { name: '牛奶', quantity: 2, unit: '盒', estimatedPrice: 15 })

    expect(mockedPost).toHaveBeenCalledWith('/api/spaces/1/shopping-lists/1/items', {
      name: '牛奶',
      quantity: 2,
      unit: '盒',
      estimatedPrice: 15,
    })
    expect(result).toEqual(mockItem)
  })

  it('updateShoppingItem calls PATCH with correct path and payload', async () => {
    const updated = { ...mockItem, purchased: true }
    mockedPatch.mockResolvedValue({ data: { data: updated } })

    const result = await updateShoppingItem(1, 1, 1, { purchased: true })

    expect(mockedPatch).toHaveBeenCalledWith('/api/spaces/1/shopping-lists/1/items/1', { purchased: true })
    expect(result).toEqual(updated)
  })

  it('deleteShoppingItem calls DELETE with correct path', async () => {
    mockedDelete.mockResolvedValue({})

    await deleteShoppingItem(1, 1, 1)

    expect(mockedDelete).toHaveBeenCalledWith('/api/spaces/1/shopping-lists/1/items/1')
  })

  it('propagates errors from http calls', async () => {
    mockedGet.mockRejectedValue(new Error('Network error'))

    await expect(listShoppingLists(1)).rejects.toThrow('Network error')
  })
})