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
  listMembers,
  updateMemberRole,
  removeMember,
} from '../space'

const mockedGet = vi.mocked(http.get)
const mockedPatch = vi.mocked(http.patch)
const mockedDelete = vi.mocked(http.delete)

describe('space API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const member = {
    id: 2,
    householdId: 1,
    userId: 8,
    email: 'member@example.com',
    displayName: 'Member',
    role: 'member',
    status: 'active',
    createdAt: '2026-06-28T20:00:00',
  }

  it('listMembers calls GET with space path', async () => {
    mockedGet.mockResolvedValue({ data: { data: [member] } })

    const result = await listMembers(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/members')
    expect(result).toEqual([member])
  })

  it('updateMemberRole calls PATCH with role payload', async () => {
    const updated = { ...member, role: 'admin' }
    mockedPatch.mockResolvedValue({ data: { data: updated } })

    const result = await updateMemberRole(1, 2, 'admin')

    expect(mockedPatch).toHaveBeenCalledWith('/api/spaces/1/members/2', { role: 'admin' })
    expect(result).toEqual(updated)
  })

  it('removeMember calls DELETE with member path', async () => {
    mockedDelete.mockResolvedValue({})

    await removeMember(1, 2)

    expect(mockedDelete).toHaveBeenCalledWith('/api/spaces/1/members/2')
  })
})
