import { http } from './http'

export interface TodoResponse {
  id: number
  householdId: number
  title: string
  description: string | null
  status: string
  priority: string
  dueAt: string | null
  repeatRule: string | null
  assignedTo: number | null
  overdue: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateTodoPayload {
  title: string
  description?: string
  priority?: string
  dueAt?: string
  repeatRule?: string
  assignedTo?: number
}

export interface UpdateTodoPayload {
  title?: string
  description?: string
  status?: string
  priority?: string
  dueAt?: string
  repeatRule?: string
  assignedTo?: number
}

export async function listTodoTasks(spaceId: number, status?: string): Promise<TodoResponse[]> {
  const params = status ? { status } : undefined
  const res = await http.get(`/api/spaces/${spaceId}/todo-tasks`, { params })
  return res.data.data
}

export async function getTodoTask(spaceId: number, id: number): Promise<TodoResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/todo-tasks/${id}`)
  return res.data.data
}

export async function createTodoTask(spaceId: number, payload: CreateTodoPayload): Promise<TodoResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/todo-tasks`, payload)
  return res.data.data
}

export async function updateTodoTask(spaceId: number, id: number, payload: UpdateTodoPayload): Promise<TodoResponse> {
  const res = await http.patch(`/api/spaces/${spaceId}/todo-tasks/${id}`, payload)
  return res.data.data
}

export async function deleteTodoTask(spaceId: number, id: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/todo-tasks/${id}`)
}