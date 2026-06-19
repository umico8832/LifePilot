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
  listDocuments,
  getDocument,
  createDocument,
  updateDocument,
  deleteDocument,
} from '../document'

const mockedGet = vi.mocked(http.get)
const mockedPost = vi.mocked(http.post)
const mockedPatch = vi.mocked(http.patch)
const mockedDelete = vi.mocked(http.delete)

describe('document API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const mockDoc = {
    id: 1,
    householdId: 1,
    title: '购房合同',
    type: 'contract',
    issuer: '开发商',
    documentDate: '2025-01-15',
    expireAt: '2035-01-15',
    expiringSoon: false,
    storageLocation: '保险柜',
    metadataJson: null,
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
  }

  it('listDocuments calls GET without type param when no type', async () => {
    mockedGet.mockResolvedValue({ data: { data: [mockDoc] } })

    const result = await listDocuments(1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/documents', { params: undefined })
    expect(result).toEqual([mockDoc])
  })

  it('listDocuments calls GET with type param when type provided', async () => {
    mockedGet.mockResolvedValue({ data: { data: [mockDoc] } })

    const result = await listDocuments(1, 'contract')

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/documents', { params: { type: 'contract' } })
    expect(result).toEqual([mockDoc])
  })

  it('getDocument calls GET with correct path', async () => {
    mockedGet.mockResolvedValue({ data: { data: mockDoc } })

    const result = await getDocument(1, 1)

    expect(mockedGet).toHaveBeenCalledWith('/api/spaces/1/documents/1')
    expect(result).toEqual(mockDoc)
  })

  it('createDocument calls POST with payload', async () => {
    mockedPost.mockResolvedValue({ data: { data: mockDoc } })

    const payload = { title: '购房合同', type: 'contract', issuer: '开发商' }
    const result = await createDocument(1, payload)

    expect(mockedPost).toHaveBeenCalledWith('/api/spaces/1/documents', payload)
    expect(result).toEqual(mockDoc)
  })

  it('updateDocument calls PATCH with payload', async () => {
    const updated = { ...mockDoc, storageLocation: '银行保险箱' }
    mockedPatch.mockResolvedValue({ data: { data: updated } })

    const result = await updateDocument(1, 1, { storageLocation: '银行保险箱' })

    expect(mockedPatch).toHaveBeenCalledWith('/api/spaces/1/documents/1', { storageLocation: '银行保险箱' })
    expect(result).toEqual(updated)
  })

  it('deleteDocument calls DELETE', async () => {
    mockedDelete.mockResolvedValue({})

    await deleteDocument(1, 1)

    expect(mockedDelete).toHaveBeenCalledWith('/api/spaces/1/documents/1')
  })

  it('propagates errors from http calls', async () => {
    mockedGet.mockRejectedValue(new Error('Network error'))

    await expect(listDocuments(1)).rejects.toThrow('Network error')
  })
})