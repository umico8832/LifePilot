import { http } from './http'

export interface MealPlanResponse {
  id: number
  householdId: number
  recipeId: number
  recipeName: string
  plannedDate: string
  mealType: string
  note: string | null
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface CreateMealPlanPayload {
  recipeId: number
  plannedDate: string
  mealType: string
  note?: string
}

export interface UpdateMealPlanPayload {
  recipeId?: number
  plannedDate?: string
  mealType?: string
  note?: string
}

export async function listMealPlans(spaceId: number, startDate?: string, endDate?: string): Promise<MealPlanResponse[]> {
  const params: Record<string, string> = {}
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  const res = await http.get(`/api/spaces/${spaceId}/meal-plans`, { params })
  return res.data.data
}

export async function getMealPlan(spaceId: number, id: number): Promise<MealPlanResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/meal-plans/${id}`)
  return res.data.data
}

export async function createMealPlan(spaceId: number, payload: CreateMealPlanPayload): Promise<MealPlanResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/meal-plans`, payload)
  return res.data.data
}

export async function updateMealPlan(spaceId: number, id: number, payload: UpdateMealPlanPayload): Promise<MealPlanResponse> {
  const res = await http.patch(`/api/spaces/${spaceId}/meal-plans/${id}`, payload)
  return res.data.data
}

export async function deleteMealPlan(spaceId: number, id: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/meal-plans/${id}`)
}