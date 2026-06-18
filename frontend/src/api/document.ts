import { http } from './http'

export interface DocumentResponse {
  id: number
  householdId: number
  title: string
  type: string
  issuer: string | null
  documentDate: string | null
  expireAt: string | null
  expiringSoon: boolean
  storageLocation: string | null
  metadataJson: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateDocumentPayload {
  title: string
  type: string
  issuer?: string
  documentDate?: string
  expireAt?: string
  storageLocation?: string
  metadataJson?: string
}

export interface UpdateDocumentPayload {
  title?: string
  type?: string
  issuer?: string
  documentDate?: string
  expireAt?: string
  storageLocation?: string
  metadataJson?: string
}

export async function listDocuments(spaceId: number, type?: string): Promise<DocumentResponse[]> {
  const params = type ? { type } : undefined
  const res = await http.get(`/api/spaces/${spaceId}/documents`, { params })
  return res.data.data
}

export async function getDocument(spaceId: number, id: number): Promise<DocumentResponse> {
  const res = await http.get(`/api/spaces/${spaceId}/documents/${id}`)
  return res.data.data
}

export async function createDocument(spaceId: number, payload: CreateDocumentPayload): Promise<DocumentResponse> {
  const res = await http.post(`/api/spaces/${spaceId}/documents`, payload)
  return res.data.data
}

export async function updateDocument(spaceId: number, id: number, payload: UpdateDocumentPayload): Promise<DocumentResponse> {
  const res = await http.patch(`/api/spaces/${spaceId}/documents/${id}`, payload)
  return res.data.data
}

export async function deleteDocument(spaceId: number, id: number): Promise<void> {
  await http.delete(`/api/spaces/${spaceId}/documents/${id}`)
}