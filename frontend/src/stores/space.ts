import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  listSpaces,
  getSpace,
  createSpace,
  updateSpace,
  listMembers,
  addMember,
  type SpaceResponse,
  type MemberResponse,
} from '@/api/space'

export const useSpaceStore = defineStore('space', () => {
  const spaces = ref<SpaceResponse[]>([])
  const currentSpace = ref<SpaceResponse | null>(null)
  const members = ref<MemberResponse[]>([])
  const loading = ref(false)

  async function fetchSpaces() {
    loading.value = true
    try {
      spaces.value = await listSpaces()
      if (!currentSpace.value && spaces.value.length > 0) {
        currentSpace.value = spaces.value[0]
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchSpace(spaceId: number) {
    currentSpace.value = await getSpace(spaceId)
  }

  async function createNewSpace(name: string, type?: string) {
    const space = await createSpace(name, type)
    spaces.value.push(space)
    currentSpace.value = space
    return space
  }

  async function renameSpace(spaceId: number, name: string) {
    const updated = await updateSpace(spaceId, name)
    const idx = spaces.value.findIndex((s) => s.id === spaceId)
    if (idx >= 0) {
      spaces.value[idx] = updated
    }
    if (currentSpace.value?.id === spaceId) {
      currentSpace.value = updated
    }
    return updated
  }

  async function fetchMembers(spaceId: number) {
    members.value = await listMembers(spaceId)
  }

  async function inviteMember(spaceId: number, email: string, role?: string) {
    const member = await addMember(spaceId, email, role)
    members.value.push(member)
    return member
  }

  function setCurrentSpace(space: SpaceResponse) {
    currentSpace.value = space
  }

  function clear() {
    spaces.value = []
    currentSpace.value = null
    members.value = []
  }

  return {
    spaces,
    currentSpace,
    members,
    loading,
    fetchSpaces,
    fetchSpace,
    createNewSpace,
    renameSpace,
    fetchMembers,
    inviteMember,
    setCurrentSpace,
    clear,
  }
})