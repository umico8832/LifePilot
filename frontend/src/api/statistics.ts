import { http } from './http'
import type { ApiResponse } from '@/types/auth'

export interface OverviewResponse {
  totalIncome: number
  totalExpense: number
  netBalance: number
  transactionCount: number
  inventoryItemCount: number
  shoppingListCount: number
  inventoryAlertCount: number
}

export interface CategoryBreakdown {
  categoryId: number | null
  categoryName: string
  amount: number
  count: number
}

export interface FinanceMonthlyResponse {
  year: number
  month: number
  totalIncome: number
  totalExpense: number
  netBalance: number
  categories: CategoryBreakdown[]
}

export async function getOverview(spaceId: number): Promise<OverviewResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/statistics/overview`)
  return res.data.data
}

export interface CategoryCount {
  category: string
  count: number
}

export interface InventoryStatsResponse {
  totalItems: number
  lowStockCount: number
  byCategory: CategoryCount[]
}

export interface InventoryAlertItem {
  id: number
  name: string
  category: string | null
  quantity: number
  unit: string | null
  location: string | null
  expireAt: string | null
  lowStockThreshold: number | null
  alertType: 'expiring' | 'low_stock'
}

export interface InventoryAlertsResponse {
  expiringItems: InventoryAlertItem[]
  lowStockItems: InventoryAlertItem[]
  totalAlerts: number
}

export interface TodoDailyTrend {
  date: string
  count: number
}

export interface TodoStatsResponse {
  totalCount: number
  pendingCount: number
  inProgressCount: number
  completedCount: number
  cancelledCount: number
  overdueCount: number
  completionRate: number
  recent30Days: TodoDailyTrend[]
}

export async function getFinanceMonthly(
  spaceId: number,
  year: number,
  month: number,
): Promise<FinanceMonthlyResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/statistics/finance/monthly`, {
    params: { year, month },
  })
  return res.data.data
}

export interface CategoryDetail {
  categoryId: number | null
  categoryName: string
  amount: number
  count: number
}

export interface FinanceCategoriesResponse {
  year: number
  month: number
  totalIncome: number
  totalExpense: number
  expenseCategories: CategoryDetail[]
  incomeCategories: CategoryDetail[]
}

export async function getFinanceCategories(
  spaceId: number,
  year: number,
  month: number,
): Promise<FinanceCategoriesResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/statistics/finance/categories`, {
    params: { year, month },
  })
  return res.data.data
}

export async function getInventoryStats(spaceId: number): Promise<InventoryStatsResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/statistics/inventory`)
  return res.data.data
}

export interface ShoppingStatsResponse {
  totalLists: number
  activeLists: number
  completedLists: number
  totalItems: number
  purchasedItems: number
  recent30Days: Array<{ date: string; count: number }>
}

export async function getShoppingStats(spaceId: number): Promise<ShoppingStatsResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/statistics/shopping`)
  return res.data.data
}

export async function getTodoStats(spaceId: number): Promise<TodoStatsResponse> {
  const res = await http.get<ApiResponse<TodoStatsResponse>>(`/api/spaces/${spaceId}/statistics/todos`)
  return res.data.data
}

export async function getInventoryAlerts(spaceId: number): Promise<InventoryAlertsResponse> {
  const res = await http.get<ApiResponse<InventoryAlertsResponse>>(`/api/spaces/${spaceId}/statistics/inventory/alerts`)
  return res.data.data
}
