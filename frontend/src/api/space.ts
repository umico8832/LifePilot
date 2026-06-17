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