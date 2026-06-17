import { http } from './http'

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