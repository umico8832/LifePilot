import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

import { http } from '../http'
import {
  parseTransaction,
  parseShoppingList,
  parseTodo,
  generateMonthlyReport,
  draftShoppingListFromMealPlan,
} from '../ai'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)

describe('ai API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('parseTransaction calls POST with text payload', async () => {
    const mockDraft = {
      type: 'expense',
      amount: 35.5,
      currency: 'CNY',
      occurredAt: '2026-06-19',
      merchant: '星巴克',
      categoryName: '餐饮',
      note: null,
      needsReview: false,
      rawInput: '星巴克咖啡35.5',
      validationMessage: null,
    }
    mockedPost.mockResolvedValue({ data: { data: mockDraft } })

    const result = await parseTransaction(1, '星巴克咖啡35.5')

    expect(mockedPost).toHaveBeenCalledWith('/api/ai/spaces/1/parse-transaction', { text: '星巴克咖啡35.5' })
    expect(result).toEqual(mockDraft)
  })

  it('parseShoppingList calls POST with text payload', async () => {
    const mockDraft = {
      listName: '本周采购',
      estimatedBudget: null,
      items: [
        { name: '牛奶', quantity: 2, unit: '盒', estimatedPrice: 15 },
        { name: '面包', quantity: 1, unit: '袋', estimatedPrice: 8 },
      ],
      needsReview: false,
      rawInput: '买2盒牛奶和1袋面包',
      validationMessage: null,
    }
    mockedPost.mockResolvedValue({ data: { data: mockDraft } })

    const result = await parseShoppingList(1, '买2盒牛奶和1袋面包')

    expect(mockedPost).toHaveBeenCalledWith('/api/ai/spaces/1/parse-shopping', { text: '买2盒牛奶和1袋面包' })
    expect(result).toEqual(mockDraft)
  })

  it('parseTodo calls POST with text payload', async () => {
    const mockDraft = {
      items: [
        { title: '交电费', description: null, priority: 'high', dueAt: '2026-06-20' },
      ],
      needsReview: false,
      rawInput: '明天之前交电费',
      validationMessage: null,
    }
    mockedPost.mockResolvedValue({ data: { data: mockDraft } })

    const result = await parseTodo(1, '明天之前交电费')

    expect(mockedPost).toHaveBeenCalledWith('/api/ai/spaces/1/parse-todo', { text: '明天之前交电费' })
    expect(result).toEqual(mockDraft)
  })

  it('generateMonthlyReport calls GET with year and month params', async () => {
    const mockReport = {
      year: 2026,
      month: 6,
      finance: {
        totalIncome: 10000,
        totalExpense: 5000,
        balance: 5000,
        transactionCount: 30,
        topExpenseCategories: [{ name: '餐饮', amount: 2000, count: 15 }],
      },
      inventory: { totalItems: 20, lowStockCount: 3 },
      shopping: { listCount: 5 },
      todo: { totalCount: 10, pendingCount: 3, completedCount: 6, overdueCount: 1 },
      highlights: ['本月储蓄率 50%'],
      suggestions: ['建议减少餐饮开支'],
      reportText: '6月生活报告...',
    }
    mockedGet.mockResolvedValue({ data: { data: mockReport } })

    const result = await generateMonthlyReport(1, 2026, 6)

    expect(mockedGet).toHaveBeenCalledWith('/api/ai/spaces/1/monthly-report', {
      params: { year: 2026, month: 6 },
    })
    expect(result).toEqual(mockReport)
  })

  it('draftShoppingListFromMealPlan calls GET with date range params', async () => {
    const mockDraft = {
      listName: '饮食计划采购清单',
      estimatedBudget: null,
      items: [
        { name: '番茄', quantity: 2, unit: '个', estimatedPrice: null },
      ],
      needsReview: true,
      rawInput: '2026-06-22 lunch 番茄炒蛋',
      validationMessage: '请确认数量和单位后创建。',
    }
    mockedGet.mockResolvedValue({ data: { data: mockDraft } })

    const result = await draftShoppingListFromMealPlan(1, '2026-06-22', '2026-06-28')

    expect(mockedGet).toHaveBeenCalledWith('/api/ai/spaces/1/meal-plan-shopping-draft', {
      params: { startDate: '2026-06-22', endDate: '2026-06-28' },
    })
    expect(result).toEqual(mockDraft)
  })

  it('propagates errors from http calls', async () => {
    mockedPost.mockRejectedValue(new Error('Network error'))

    await expect(parseTransaction(1, 'test')).rejects.toThrow('Network error')
  })
})
