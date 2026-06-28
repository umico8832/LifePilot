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
  acceptInvitation,
  createInvitation,
  listMembers,
  listInvitations,
  updateMemberRole,
  removeMember,
  revokeInvitation,
} from '../space'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)
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

  it('createInvitation calls POST with invitation payload', async () => {
    const invitation = {
      id: 9,
      householdId: 1,
      invitedBy: 3,
      targetEmail: 'guest@example.com',
      role: 'member',
      status: 'pending',
      expiresAt: '2026-07-05T20:00:00',
      acceptedAt: null,
      acceptedBy: null,
      createdAt: '2026-06-28T20:00:00',
      token: 'token-value',
    }
    mockedPost.mockResolvedValue({ data: { data: invitation } })

    const result = await createInvitation(1, { targetEmail: 'guest@example.com', role: 'member' })

    expect(mockedPost).toHaveBeenCalledWith('/api/spaces/1/invitations', {
      targetEmail: 'guest@example.com',
      role: 'member',
    })
    expect(result).toEqual(invitation)
  })

  it('listInvitations calls GET with invitation path', async () => {
    mockedGet.mockResolvedValue({ data: { data: [] } })

    const result = await listInvitations(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/invitations')
    expect(result).toEqual([])
  })

  it('revokeInvitation calls DELETE with invitation path', async () => {
    mockedDelete.mockResolvedValue({})

    await revokeInvitation(1, 9)

    expect(mockedDelete).toHaveBeenCalledWith('/api/spaces/1/invitations/9')
  })

  it('acceptInvitation calls POST with token payload', async () => {
    const accepted = {
      id: 9,
      householdId: 1,
      invitedBy: 3,
      targetEmail: null,
      role: 'viewer',
      status: 'accepted',
      expiresAt: '2026-07-05T20:00:00',
      acceptedAt: '2026-06-28T20:05:00',
      acceptedBy: 4,
      createdAt: '2026-06-28T20:00:00',
      token: null,
    }
    mockedPost.mockResolvedValue({ data: { data: accepted } })

    const result = await acceptInvitation('abc123')

    expect(mockedPost).toHaveBeenCalledWith('/api/spaces/invitations/accept', { token: 'abc123' })
    expect(result).toEqual(accepted)
  })
})
