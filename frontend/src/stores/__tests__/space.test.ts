import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('@/api/space', () => ({
  listSpaces: vi.fn(),
  getSpace: vi.fn(),
  createSpace: vi.fn(),
  updateSpace: vi.fn(),
  listMembers: vi.fn(),
  addMember: vi.fn(),
  updateMemberRole: vi.fn(),
  removeMember: vi.fn(),
}))

import { useSpaceStore } from '../space'
import {
  listSpaces,
  getSpace,
  createSpace,
  updateSpace,
  listMembers,
  addMember,
  updateMemberRole,
  removeMember,
} from '@/api/space'

const mockedListSpaces = vi.mocked(listSpaces)
const mockedGetSpace = vi.mocked(getSpace)
const mockedCreateSpace = vi.mocked(createSpace)
const mockedUpdateSpace = vi.mocked(updateSpace)
const mockedListMembers = vi.mocked(listMembers)
const mockedAddMember = vi.mocked(addMember)
const mockedUpdateMemberRole = vi.mocked(updateMemberRole)
const mockedRemoveMember = vi.mocked(removeMember)

const mockSpaces = [
  { id: 1, name: 'Home', type: 'personal', ownerId: 1 },
  { id: 2, name: 'Family', type: 'family', ownerId: 1 },
]

const mockMembers = [
  { id: 1, userId: 1, email: 'a@b.com', displayName: 'Alice', role: 'owner' },
]

describe('useSpaceStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts with empty state', () => {
    const store = useSpaceStore()
    expect(store.spaces).toEqual([])
    expect(store.currentSpace).toBeNull()
    expect(store.members).toEqual([])
    expect(store.loading).toBe(false)
  })

  it('fetchSpaces loads list and sets first as current', async () => {
    mockedListSpaces.mockResolvedValue(mockSpaces as any)
    const store = useSpaceStore()

    await store.fetchSpaces()

    expect(store.spaces).toEqual(mockSpaces)
    expect(store.currentSpace).toEqual(mockSpaces[0])
    expect(store.loading).toBe(false)
  })

  it('fetchSpaces does not override existing currentSpace', async () => {
    mockedListSpaces.mockResolvedValue(mockSpaces as any)
    const store = useSpaceStore()
    store.setCurrentSpace(mockSpaces[1] as any)

    await store.fetchSpaces()

    expect(store.currentSpace).toEqual(mockSpaces[1])
  })

  it('fetchSpace sets current space', async () => {
    mockedGetSpace.mockResolvedValue(mockSpaces[0] as any)
    const store = useSpaceStore()

    await store.fetchSpace(1)

    expect(store.currentSpace).toEqual(mockSpaces[0])
  })

  it('createNewSpace adds to list and sets current', async () => {
    const newSpace = { id: 3, name: 'Office', type: 'personal', ownerId: 1 }
    mockedCreateSpace.mockResolvedValue(newSpace as any)
    const store = useSpaceStore()

    const result = await store.createNewSpace('Office', 'personal')

    expect(result).toEqual(newSpace)
    expect(store.spaces).toContainEqual(newSpace)
    expect(store.currentSpace).toEqual(newSpace)
  })

  it('renameSpace updates space in list and current', async () => {
    const updated = { id: 1, name: 'Renamed', type: 'personal', ownerId: 1 }
    mockedUpdateSpace.mockResolvedValue(updated as any)
    const store = useSpaceStore()
    store.spaces = [...mockSpaces] as any
    store.currentSpace = mockSpaces[0] as any

    const result = await store.renameSpace(1, 'Renamed')

    expect(result).toEqual(updated)
    expect(store.spaces[0]).toEqual(updated)
    expect(store.currentSpace).toEqual(updated)
  })

  it('fetchMembers loads members', async () => {
    mockedListMembers.mockResolvedValue(mockMembers as any)
    const store = useSpaceStore()

    await store.fetchMembers(1)

    expect(store.members).toEqual(mockMembers)
  })

  it('inviteMember adds to members list', async () => {
    const newMember = { id: 2, userId: 2, email: 'b@c.com', displayName: 'Bob', role: 'member' }
    mockedAddMember.mockResolvedValue(newMember as any)
    const store = useSpaceStore()

    const result = await store.inviteMember(1, 'b@c.com', 'member')

    expect(result).toEqual(newMember)
    expect(store.members).toContainEqual(newMember)
  })

  it('changeMemberRole updates member in list', async () => {
    const updated = { ...mockMembers[0], role: 'admin' }
    mockedUpdateMemberRole.mockResolvedValue(updated as any)
    const store = useSpaceStore()
    store.members = [...mockMembers] as any

    const result = await store.changeMemberRole(1, 1, 'admin')

    expect(mockedUpdateMemberRole).toHaveBeenCalledWith(1, 1, 'admin')
    expect(result).toEqual(updated)
    expect(store.members[0]).toEqual(updated)
  })

  it('deleteMember removes member from list', async () => {
    mockedRemoveMember.mockResolvedValue(undefined)
    const store = useSpaceStore()
    store.members = [...mockMembers, { id: 2, userId: 2, email: 'b@c.com', displayName: 'Bob', role: 'member' }] as any

    await store.deleteMember(1, 2)

    expect(mockedRemoveMember).toHaveBeenCalledWith(1, 2)
    expect(store.members).toEqual(mockMembers)
  })

  it('clear resets all state', () => {
    const store = useSpaceStore()
    store.spaces = mockSpaces as any
    store.currentSpace = mockSpaces[0] as any
    store.members = mockMembers as any

    store.clear()

    expect(store.spaces).toEqual([])
    expect(store.currentSpace).toBeNull()
    expect(store.members).toEqual([])
  })

  it('setCurrentSpace updates current space', () => {
    const store = useSpaceStore()
    store.setCurrentSpace(mockSpaces[1] as any)
    expect(store.currentSpace).toEqual(mockSpaces[1])
  })
})
