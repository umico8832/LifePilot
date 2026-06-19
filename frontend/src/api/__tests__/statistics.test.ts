import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../http', () => ({
  http: {
    get: vi.fn(),
  },
}))

import { http } from '../http'
import {
  getOverview,
  getFinanceMonthly,
  getFinanceCategories,
  getInventoryStats,
  getTodoStats,
  getShoppingStats,
} from '../statistics'

const mockedGet = vi.mocked(http.get)

describe('statistics API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getOverview calls correct endpoint and returns data', async () => {
    const mockData = {
      totalIncome: 1000,
      totalExpense: 500,
      netBalance: 500,
      transactionCount: 10,
      inventoryItemCount: 20,
      shoppingListCount: 3,
      inventoryAlertCount: 2,
    }
    mockedGet.mockResolvedValue({ data: { data: mockData } })

    const result = await getOverview(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/statistics/overview')
    expect(result).toEqual(mockData)
  })

  it('getFinanceMonthly calls correct endpoint with params', async () => {
    const mockData = {
      year: 2026,
      month: 6,
      totalIncome: 5000,
      totalExpense: 3000,
      netBalance: 2000,
      categories: [{ categoryId: 1, categoryName: '餐饮', amount: 500, count: 5 }],
    }
    mockedGet.mockResolvedValue({ data: { data: mockData } })

    const result = await getFinanceMonthly(1, 2026, 6)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/statistics/finance/monthly', {
      params: { year: 2026, month: 6 },
    })
    expect(result).toEqual(mockData)
  })

  it('getFinanceCategories calls correct endpoint with params', async () => {
    const mockData = {
      year: 2026,
      month: 6,
      totalIncome: 5000,
      totalExpense: 3000,
      expenseCategories: [{ categoryId: 1, categoryName: '餐饮', amount: 500, count: 5 }],
      incomeCategories: [{ categoryId: 2, categoryName: '工资', amount: 5000, count: 1 }],
    }
    mockedGet.mockResolvedValue({ data: { data: mockData } })

    const result = await getFinanceCategories(1, 2026, 6)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/statistics/finance/categories', {
      params: { year: 2026, month: 6 },
    })
    expect(result).toEqual(mockData)
  })

  it('getInventoryStats calls correct endpoint', async () => {
    const mockData = {
      totalItems: 30,
      lowStockCount: 5,
      byCategory: [{ category: '食品', count: 10 }],
    }
    mockedGet.mockResolvedValue({ data: { data: mockData } })

    const result = await getInventoryStats(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/statistics/inventory')
    expect(result).toEqual(mockData)
  })

  it('getTodoStats calls correct endpoint', async () => {
    const mockData = {
      totalCount: 20,
      pendingCount: 5,
      inProgressCount: 3,
      completedCount: 10,
      cancelledCount: 2,
      overdueCount: 1,
    }
    mockedGet.mockResolvedValue({ data: { data: mockData } })

    const result = await getTodoStats(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/statistics/todos')
    expect(result).toEqual(mockData)
  })

  it('getShoppingStats calls correct endpoint', async () => {
    const mockData = {
      totalLists: 8,
      activeLists: 3,
      completedLists: 5,
      totalItems: 25,
      purchasedItems: 18,
      recent30Days: [{ date: '2026-06-01', count: 2 }],
    }
    mockedGet.mockResolvedValue({ data: { data: mockData } })

    const result = await getShoppingStats(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/statistics/shopping')
    expect(result).toEqual(mockData)
  })

  it('propagates errors from http.get', async () => {
    mockedGet.mockRejectedValue(new Error('Network error'))

    await expect(getOverview(1)).rejects.toThrow('Network error')
  })
})