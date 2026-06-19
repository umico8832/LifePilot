import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

import { http } from '../http'
import {
  listTodoTasks,
  getTodoTask,
  createTodoTask,
  updateTodoTask,
  deleteTodoTask,
} from '../todo'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)
const mockedPatch = vi.mocked(http.patch)
const mockedDelete = vi.mocked(http.delete)

describe('todo API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const mockTask = {
    id: 1,
    householdId: 1,
    title: '买菜',
    description: '去超市买一周的菜',
    status: 'pending',
    priority: 'high',
    dueAt: '2026-06-20T18:00:00',
    repeatRule: null,
    assignedTo: null,
    overdue: false,
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
  }

  it('listTodoTasks calls GET without status param when no status', async () => {
    mockedGet.mockResolvedValue({ data: { data: [mockTask] } })

    const result = await listTodoTasks(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/todo-tasks', { params: undefined })
    expect(result).toEqual([mockTask])
  })

  it('listTodoTasks calls GET with status param when status provided', async () => {
    mockedGet.mockResolvedValue({ data: { data: [mockTask] } })

    const result = await listTodoTasks(1, 'pending')

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/todo-tasks', { params: { status: 'pending' } })
    expect(result).toEqual([mockTask])
  })

  it('getTodoTask calls GET with correct path', async () => {
    mockedGet.mockResolvedValue({ data: { data: mockTask } })

    const result = await getTodoTask(1, 1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/todo-tasks/1')
    expect(result).toEqual(mockTask)
  })

  it('createTodoTask calls POST with payload', async () => {
    mockedPost.mockResolvedValue({ data: { data: mockTask } })

    const payload = { title: '买菜', description: '去超市买一周的菜', priority: 'high' }
    const result = await createTodoTask(1, payload)

    expect(mockedPost).toHaveBeenCalledWith('/api/spaces/1/todo-tasks', payload)
    expect(result).toEqual(mockTask)
  })

  it('updateTodoTask calls PATCH with payload', async () => {
    const updated = { ...mockTask, status: 'completed' }
    mockedPatch.mockResolvedValue({ data: { data: updated } })

    const result = await updateTodoTask(1, 1, { status: 'completed' })

    expect(mockedPatch).toHaveBeenCalledWith('/api/spaces/1/todo-tasks/1', { status: 'completed' })
    expect(result).toEqual(updated)
  })

  it('deleteTodoTask calls DELETE', async () => {
    mockedDelete.mockResolvedValue({})

    await deleteTodoTask(1, 1)

    expect(mockedDelete).toHaveBeenCalledWith('/api/spaces/1/todo-tasks/1')
  })

  it('propagates errors from http calls', async () => {
    mockedGet.mockRejectedValue(new Error('Network error'))

    await expect(listTodoTasks(1)).rejects.toThrow('Network error')
  })
})