import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ref } from 'vue'

const mockSpaces = ref([{ id: 2, name: 'Family' }])
const mockAcceptSpaceInvitation = vi.fn()
const mockSetCurrentSpace = vi.fn()
const mockPush = vi.fn()
let mockRouteQuery: Record<string, string> = {}

vi.mock('@/stores/space', () => ({
  useSpaceStore: () => ({
    spaces: mockSpaces.value,
    acceptSpaceInvitation: mockAcceptSpaceInvitation,
    setCurrentSpace: mockSetCurrentSpace,
  }),
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: mockRouteQuery }),
  useRouter: () => ({ push: mockPush }),
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
  },
}))

vi.mock('@/layouts/AppShell.vue', () => ({
  default: { name: 'AppShell', template: '<div class="app-shell-stub"><slot /></div>' },
}))

vi.mock('@lucide/vue', () => ({
  Link2: { name: 'Link2', template: '<span />' },
  ShieldCheck: { name: 'ShieldCheck', template: '<span />' },
}))

import AcceptInvitationView from '../space/AcceptInvitationView.vue'

function mountAcceptInvitationView() {
  return mount(AcceptInvitationView, {
    global: {
      stubs: {
        ElInput: {
          props: ['modelValue'],
          emits: ['update:modelValue'],
          template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
        },
        ElButton: {
          props: ['type', 'loading', 'disabled', 'nativeType'],
          template: '<button :type="nativeType || \'button\'" :disabled="disabled"><slot /></button>',
        },
      },
    },
  })
}

describe('AcceptInvitationView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockRouteQuery = {}
    mockSpaces.value = [{ id: 2, name: 'Family' }]
  })

  it('prefills token from route query and accepts invitation', async () => {
    mockRouteQuery = { token: 'query-token' }
    mockAcceptSpaceInvitation.mockResolvedValue({
      householdId: 2,
      status: 'accepted',
    })

    const wrapper = mountAcceptInvitationView()
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(mockAcceptSpaceInvitation).toHaveBeenCalledWith('query-token')
    expect(wrapper.text()).toContain('邀请已接受')

    await wrapper.find('button').trigger('click')
    expect(mockSetCurrentSpace).toHaveBeenCalledWith({ id: 2, name: 'Family' })
    expect(mockPush).toHaveBeenCalledWith('/spaces')
  })

  it('shows readable email mismatch error', async () => {
    mockAcceptSpaceInvitation.mockRejectedValue({
      response: { data: { code: 'FORBIDDEN', message: 'Invitation target email does not match current user' } },
    })

    const wrapper = mountAcceptInvitationView()
    await wrapper.find('input').setValue('bad-token')
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(mockAcceptSpaceInvitation).toHaveBeenCalledWith('bad-token')
    expect(wrapper.text()).toContain('邀请邮箱与当前登录账号不匹配')
  })
})
