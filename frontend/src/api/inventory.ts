import { http } from './http'

export interface InventoryItemResponse {
  id: number
  householdId: number
  name: string
  category: string | null
  quantity: number
  unit: string | null
  location: string | null
  expireAt: string | null
  lowStockThreshold: number | null
  lowStock: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateInventoryItemPayload {
  name: string
  category?: string
  quantity?: number
  unit?: string
  location?: string
  expireAt?: string
  lowStockThreshold?: number
}

export interface UpdateInventoryItemPayload {
  name?: string
  category?: string
  quantity?: number
  unit?: string
  location?: string
  expireAt?: string
  lowStockThreshold?: number
}

export async function listInventoryItems(spaceId: number): Promise<InventoryItemResponse[]> {
  const res = await http.get(`/api/spaces/${spaceId}/inventory-items`)
  return res.data.data
}

export async function getInventoryItem(spaceId: number, id: number): Promise<InventoryItemResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/inventory-items/${id}`)
  return res.data.data
}

export async function createInventoryItem(spaceId: number, payload: CreateInventoryItemPayload): Promise<InventoryItemResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/inventory-items`, payload)
  return res.data.data
}

export async function updateInventoryItem(spaceId: number, id: number, payload: UpdateInventoryItemPayload): Promise<InventoryItemResponse> {
  const res = await http.patch(`/api/spaces/${spaceId}/inventory-items/${id}`, payload)
  return res.data.data
}

export async function deleteInventoryItem(spaceId: number, id: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/inventory-items/${id}`)
}

export async function listInventoryAlerts(spaceId: number): Promise<InventoryItemResponse[]> {
  const res = await http.get(`/api/spaces/${spaceId}/inventory-items/alerts`)
  return res.data.data
}