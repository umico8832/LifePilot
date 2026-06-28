import { http } from './http'

export interface SpaceResponse {
  id: number
  name: string
  type: string
  ownerUserId: number
  memberRole: string
  createdAt: string
  updatedAt: string
}

export interface MemberResponse {
  id: number
  householdId: number
  userId: number
  email: string
  displayName: string
  role: string
  status: string
  createdAt: string
}

export interface InvitationResponse {
  id: number
  householdId: number
  invitedBy: number
  targetEmail: string | null
  role: string
  status: string
  expiresAt: string
  acceptedAt: string | null
  acceptedBy: number | null
  createdAt: string
  token: string | null
}

export async function listSpaces(): Promise<SpaceResponse[]> {
  const res = await http.get('/api/spaces')
  return res.data.data
}

export async function getSpace(spaceId: number): Promise<SpaceResponse> {
  const res = await http.get(`/api/spaces/${spaceId}`)
  return res.data.data
}

export async function createSpace(name: string, type?: string): Promise<SpaceResponse> {
  const res = await http.post('/api/spaces', { name, type })
  return res.data.data
}

export async function updateSpace(spaceId: number, name: string): Promise<SpaceResponse> {
  const res = await http.patch(`/api/spaces/${spaceId}`, { name })
  return res.data.data
}

export async function listMembers(spaceId: number): Promise<MemberResponse[]> {
  const res = await http.get(`/api/spaces/${spaceId}/members`)
  return res.data.data
}

export async function addMember(spaceId: number, email: string, role?: string): Promise<MemberResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/members`, { email, role })
  return res.data.data
}

export async function updateMemberRole(spaceId: number, memberId: number, role: string): Promise<MemberResponse> {
  const res = await http.patch(`/api/spaces/${spaceId}/members/${memberId}`, { role })
  return res.data.data
}

export async function removeMember(spaceId: number, memberId: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/members/${memberId}`)
}

export async function createInvitation(
  spaceId: number,
  payload: { targetEmail?: string; role?: string; expiresInDays?: number },
): Promise<InvitationResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/invitations`, payload)
  return res.data.data
}

export async function listInvitations(spaceId: number): Promise<InvitationResponse[]> {
  const res = await http.get(`/api/spaces/${spaceId}/invitations`)
  return res.data.data
}

export async function revokeInvitation(spaceId: number, invitationId: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/invitations/${invitationId}`)
}

export async function acceptInvitation(token: string): Promise<InvitationResponse> {
  const res = await http.post('/api/spaces/invitations/accept', { token })
  return res.data.data
}
