import { http } from './http'

export interface RecipeResponse {
  id: number
  householdId: number
  name: string
  description: string | null
  ingredientsJson: string | null
  stepsJson: string | null
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface CreateRecipePayload {
  name: string
  description?: string
  ingredientsJson?: string
  stepsJson?: string
}

export interface UpdateRecipePayload {
  name?: string
  description?: string
  ingredientsJson?: string
  stepsJson?: string
}

export async function listRecipes(spaceId: number): Promise<RecipeResponse[]> {
  const res = await http.get(`/api/spaces/${spaceId}/recipes`)
  return res.data.data
}

export async function getRecipe(spaceId: number, id: number): Promise<RecipeResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/recipes/${id}`)
  return res.data.data
}

export async function createRecipe(spaceId: number, payload: CreateRecipePayload): Promise<RecipeResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/recipes`, payload)
  return res.data.data
}

export async function updateRecipe(spaceId: number, id: number, payload: UpdateRecipePayload): Promise<RecipeResponse> {
  const res = await http.patch(`/api/spaces/${spaceId}/recipes/${id}`, payload)
  return res.data.data
}

export async function deleteRecipe(spaceId: number, id: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/recipes/${id}`)
}