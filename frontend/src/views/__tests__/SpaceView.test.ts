import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

const mockSpaceStore = {
  spaces: [] as any[],
  currentSpace: null as any,
  members: [] as any[],
  invitations: [] as any[],
  fetchSpaces: vi.fn(),
  fetchSpace: vi.fn(),
  fetchMembers: vi.fn(),
  fetchInvitations: vi.fn(),
  createNewSpace: vi.fn(),
  renameSpace: vi.fn(),
  createSpaceInvitation: vi.fn(),
  revokeSpaceInvitation: vi.fn(),
  changeMemberRole: vi.fn(),
  deleteMember: vi.fn(),
}

const mockAuthStore = {
  user: { id: 1 },
}

vi.mock('@/stores/space', () => ({
  useSpaceStore: () => mockSpaceStore,
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => mockAuthStore,
}))

vi.mock('@/layouts/AppShell.vue', () => ({
  default: { name: 'AppShell', template: '<div class="app-shell-stub"><slot /></div>' },
}))

vi.mock('@lucide/vue', () => ({
  Copy: { name: 'Copy', template: '<span />' },
  Home: { name: 'Home', template: '<span />' },
  Link2: { name: 'Link2', template: '<span />' },
  RefreshCw: { name: 'RefreshCw', template: '<span />' },
}))

import SpaceView from '../space/SpaceView.vue'

function mountSpaceView() {
  return mount(SpaceView, {
    global: {
      stubs: {
        ElButton: { template: '<button><slot /></button>', props: ['type', 'size', 'disabled', 'loading', 'plain'] },
        ElDialog: { template: '<div><slot /><slot name="footer" /></div>', props: ['modelValue', 'title', 'width'] },
        ElForm: { template: '<form><slot /></form>', props: ['labelWidth'] },
        ElFormItem: { template: '<label><slot /></label>', props: ['label'] },
        ElInput: { template: '<input />', props: ['modelValue', 'placeholder', 'size', 'clearable'] },
        ElInputNumber: { template: '<input type="number" />', props: ['modelValue', 'min', 'max', 'controlsPosition'] },
        ElSelect: { template: '<select><slot /></select>', props: ['modelValue', 'size'] },
        ElOption: { template: '<option />', props: ['label', 'value'] },
        ElTag: { template: '<span><slot /></span>', props: ['type', 'size'] },
        ElTable: { template: '<div><slot /></div>', props: ['data', 'stripe'] },
        ElTableColumn: { template: '<div><slot name="default" :row="{}" /></div>', props: ['prop', 'label', 'minWidth', 'width', 'fixed'] },
      },
    },
  })
}

describe('SpaceView invitations', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockSpaceStore.spaces = [{ id: 2, name: 'Family', type: 'family', memberRole: 'admin' }]
    mockSpaceStore.currentSpace = { id: 2, name: 'Family', type: 'family', memberRole: 'admin' }
    mockSpaceStore.members = [{ id: 1, userId: 1, email: 'owner@example.com', displayName: 'Owner', role: 'admin', status: 'active' }]
    mockSpaceStore.invitations = []
    mockSpaceStore.fetchSpaces.mockResolvedValue(undefined)
    mockSpaceStore.fetchSpace.mockResolvedValue(undefined)
    mockSpaceStore.fetchMembers.mockResolvedValue(undefined)
    mockSpaceStore.fetchInvitations.mockResolvedValue(undefined)
  })

  it('shows invitation management for admins', async () => {
    const wrapper = mountSpaceView()
    await flushPromises()

    expect(wrapper.text()).toContain('生成邀请')
    expect(wrapper.text()).toContain('邀请链接')
    expect(mockSpaceStore.fetchInvitations).toHaveBeenCalledWith(2)
  })

  it('keeps ordinary members in read-only mode without invitation actions', async () => {
    mockSpaceStore.spaces = [{ id: 2, name: 'Family', type: 'family', memberRole: 'member' }]
    mockSpaceStore.currentSpace = { id: 2, name: 'Family', type: 'family', memberRole: 'member' }

    const wrapper = mountSpaceView()
    await flushPromises()

    expect(wrapper.text()).not.toContain('生成邀请')
    expect(wrapper.text()).not.toContain('邀请链接')
    expect(mockSpaceStore.fetchInvitations).not.toHaveBeenCalled()
  })
})
