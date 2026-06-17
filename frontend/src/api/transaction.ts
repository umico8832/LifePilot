import { http } from './http'

export interface TransactionResponse {
  id: number
  householdId: number
  userId: number
  categoryId: number | null
  type: string
  amount: number
  currency: string
  occurredAt: string
  merchant: string | null
  note: string | null
  source: string
  createdAt: string
  updatedAt: string
}

export interface CreateTransactionPayload {
  amount: number
  type?: string
  currency?: string
  categoryId?: number
  occurredAt?: string
  merchant?: string
  note?: string
}

export interface UpdateTransactionPayload {
  amount?: number
  type?: string
  categoryId?: number
  occurredAt?: string
  merchant?: string
  note?: string
}

export async function listTransactions(spaceId: number): Promise<TransactionResponse[]> {
  const res = await http.get(`/api/spaces/${spaceId}/transactions`)
  return res.data.data
}

export async function getTransaction(spaceId: number, id: number): Promise<TransactionResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/transactions/${id}`)
  return res.data.data
}

export async function createTransaction(spaceId: number, payload: CreateTransactionPayload): Promise<TransactionResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/transactions`, payload)
  return res.data.data
}

export async function updateTransaction(spaceId: number, id: number, payload: UpdateTransactionPayload): Promise<TransactionResponse> {
  const res = await http.patch(`/api/spaces/${spaceId}/transactions/${id}`, payload)
  return res.data.data
}

export async function deleteTransaction(spaceId: number, id: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/transactions/${id}`)
}

// ---- Category APIs ----

export interface CategoryResponse {
  id: number
  householdId: number
  name: string
  type: string
  icon: string | null
  color: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateCategoryPayload {
  name: string
  type?: string
  icon?: string
  color?: string
}

export async function listCategories(spaceId: number): Promise<CategoryResponse[]> {
  const res = await http.get(`/api/spaces/${spaceId}/transaction-categories`)
  return res.data.data
}

export async function createCategory(spaceId: number, payload: CreateCategoryPayload): Promise<CategoryResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/transaction-categories`, payload)
  return res.data.data
}

export async function deleteCategory(spaceId: number, categoryId: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/transaction-categories/${categoryId}`)
}
