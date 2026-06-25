import { http } from './http'

export interface TransactionDraft {
  type: string | null
  amount: number | null
  currency: string | null
  occurredAt: string | null
  merchant: string | null
  categoryName: string | null
  note: string | null
  needsReview: boolean
  rawInput: string | null
  validationMessage: string | null
}

export interface ShoppingDraftItem {
  name: string
  quantity: number
  unit: string | null
  estimatedPrice: number | null
}

export interface ShoppingDraft {
  listName: string
  estimatedBudget: number | null
  items: ShoppingDraftItem[]
  needsReview: boolean
  rawInput: string | null
  validationMessage: string | null
}

export async function parseTransaction(spaceId: number, text: string): Promise<TransactionDraft> {
  const res = await http.post(`/api/ai/spaces/${spaceId}/parse-transaction`, { text })
  return res.data.data
}

export interface TodoDraftItem {
  title: string
  description: string | null
  priority: string | null
  dueAt: string | null
}

export interface TodoDraft {
  items: TodoDraftItem[]
  needsReview: boolean
  rawInput: string | null
  validationMessage: string | null
}

export async function parseShoppingList(spaceId: number, text: string): Promise<ShoppingDraft> {
  const res = await http.post(`/api/ai/spaces/${spaceId}/parse-shopping`, { text })
  return res.data.data
}

export interface CategoryItem {
  name: string
  amount: number
  count: number
}

export interface FinanceSummary {
  totalIncome: number
  totalExpense: number
  balance: number
  transactionCount: number
  topExpenseCategories: CategoryItem[]
}

export interface InventorySummary {
  totalItems: number
  lowStockCount: number
}

export interface ShoppingSummary {
  listCount: number
}

export interface TodoSummary {
  totalCount: number
  pendingCount: number
  completedCount: number
  overdueCount: number
}

export interface MonthlyReport {
  year: number
  month: number
  finance: FinanceSummary
  inventory: InventorySummary
  shopping: ShoppingSummary
  todo: TodoSummary
  highlights: string[]
  suggestions: string[]
  reportText: string
}

export async function parseTodo(spaceId: number, text: string): Promise<TodoDraft> {
  const res = await http.post(`/api/ai/spaces/${spaceId}/parse-todo`, { text })
  return res.data.data
}

export async function generateMonthlyReport(spaceId: number, year: number, month: number): Promise<MonthlyReport> {
  const res = await http.get(`/api/ai/spaces/${spaceId}/monthly-report`, { params: { year, month } })
  return res.data.data
}

export interface RecommendedRecipe {
  recipeId: number
  recipeName: string
  matchedIngredients: string[]
  missingIngredients: string[]
  matchScore: number
  reason: string
}

export interface RecipeRecommendation {
  recipes: RecommendedRecipe[]
}

export async function recommendRecipes(spaceId: number): Promise<RecipeRecommendation> {
  const res = await http.get(`/api/ai/spaces/${spaceId}/recommend-recipes`)
  return res.data.data
}

export async function draftShoppingListFromMealPlan(
  spaceId: number,
  startDate?: string,
  endDate?: string,
): Promise<ShoppingDraft> {
  const res = await http.get(`/api/ai/spaces/${spaceId}/meal-plan-shopping-draft`, {
    params: { startDate, endDate },
  })
  return res.data.data
}
