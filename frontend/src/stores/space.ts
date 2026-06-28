import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  listSpaces,
  getSpace,
  createSpace,
  updateSpace,
  listMembers,
  addMember,
  updateMemberRole,
  removeMember,
  createInvitation,
  listInvitations,
  revokeInvitation,
  acceptInvitation,
  type SpaceResponse,
  type MemberResponse,
  type InvitationResponse,
} from '@/api/space'

export const useSpaceStore = defineStore('space', () => {
  const spaces = ref<SpaceResponse[]>([])
  const currentSpace = ref<SpaceResponse | null>(null)
  const members = ref<MemberResponse[]>([])
  const invitations = ref<InvitationResponse[]>([])
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

  async function fetchInvitations(spaceId: number) {
    invitations.value = await listInvitations(spaceId)
  }

  async function inviteMember(spaceId: number, email: string, role?: string) {
    const member = await addMember(spaceId, email, role)
    members.value.push(member)
    return member
  }

  async function createSpaceInvitation(
    spaceId: number,
    payload: { targetEmail?: string; role?: string; expiresInDays?: number },
  ) {
    const invitation = await createInvitation(spaceId, payload)
    invitations.value = [invitation, ...invitations.value]
    return invitation
  }

  async function revokeSpaceInvitation(spaceId: number, invitationId: number) {
    await revokeInvitation(spaceId, invitationId)
    const idx = invitations.value.findIndex((invitation) => invitation.id === invitationId)
    if (idx >= 0) {
      invitations.value[idx] = {
        ...invitations.value[idx],
        status: 'revoked',
      }
    }
  }

  async function acceptSpaceInvitation(token: string) {
    const invitation = await acceptInvitation(token)
    await fetchSpaces()
    return invitation
  }

  async function changeMemberRole(spaceId: number, memberId: number, role: string) {
    const member = await updateMemberRole(spaceId, memberId, role)
    const idx = members.value.findIndex((m) => m.id === memberId)
    if (idx >= 0) {
      members.value[idx] = member
    }
    return member
  }

  async function deleteMember(spaceId: number, memberId: number) {
    await removeMember(spaceId, memberId)
    members.value = members.value.filter((m) => m.id !== memberId)
  }

  function setCurrentSpace(space: SpaceResponse) {
    currentSpace.value = space
  }

  function clear() {
    spaces.value = []
    currentSpace.value = null
    members.value = []
    invitations.value = []
  }

  return {
    spaces,
    currentSpace,
    members,
    invitations,
    loading,
    fetchSpaces,
    fetchSpace,
    createNewSpace,
    renameSpace,
    fetchMembers,
    fetchInvitations,
    inviteMember,
    createSpaceInvitation,
    revokeSpaceInvitation,
    acceptSpaceInvitation,
    changeMemberRole,
    deleteMember,
    setCurrentSpace,
    clear,
  }
})
