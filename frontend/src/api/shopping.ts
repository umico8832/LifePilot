import { http } from './http'

export interface ShoppingItemResponse {
  id: number
  shoppingListId: number
  name: string
  quantity: number
  unit: string | null
  estimatedPrice: number | null
  purchased: boolean
  inventoryItemId: number | null
  createdAt: string
  updatedAt: string
}

export interface ShoppingListResponse {
  id: number
  householdId: number
  name: string
  status: string
  estimatedBudget: number | null
  createdBy: number
  createdAt: string
  updatedAt: string
  items: ShoppingItemResponse[]
}

export interface CreateShoppingListPayload {
  name: string
  estimatedBudget?: number
}

export interface UpdateShoppingListPayload {
  name?: string
  status?: string
  estimatedBudget?: number
}

export interface CreateShoppingItemPayload {
  name: string
  quantity?: number
  unit?: string
  estimatedPrice?: number
}

export interface UpdateShoppingItemPayload {
  name?: string
  quantity?: number
  unit?: string
  estimatedPrice?: number
  purchased?: boolean
}

// ---- Shopping List APIs ----

export async function listShoppingLists(spaceId: number): Promise<ShoppingListResponse[]> {
  const res = await http.get(`/api/spaces/${spaceId}/shopping-lists`)
  return res.data.data
}

export async function getShoppingList(spaceId: number, id: number): Promise<ShoppingListResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/shopping-lists/${id}`)
  return res.data.data
}

export async function createShoppingList(spaceId: number, payload: CreateShoppingListPayload): Promise<ShoppingListResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/shopping-lists`, payload)
  return res.data.data
}

export async function updateShoppingList(spaceId: number, id: number, payload: UpdateShoppingListPayload): Promise<ShoppingListResponse> {
  const res = await http.patch(`/api/spaces/${spaceId}/shopping-lists/${id}`, payload)
  return res.data.data
}

export async function deleteShoppingList(spaceId: number, id: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/shopping-lists/${id}`)
}

// ---- Shopping Item APIs ----

export async function addShoppingItem(spaceId: number, listId: number, payload: CreateShoppingItemPayload): Promise<ShoppingItemResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/shopping-lists/${listId}/items`, payload)
  return res.data.data
}

export async function updateShoppingItem(spaceId: number, listId: number, itemId: number, payload: UpdateShoppingItemPayload): Promise<ShoppingItemResponse> {
  const res = await http.patch(`/api/spaces/${spaceId}/shopping-lists/${listId}/items/${itemId}`, payload)
  return res.data.data
}

export async function deleteShoppingItem(spaceId: number, listId: number, itemId: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/shopping-lists/${listId}/items/${itemId}`)
}