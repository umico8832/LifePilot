import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ref } from 'vue'

const mockCurrentSpace = ref<{ id: number; name: string } | null>(null)
const mockSpaces = ref<{ id: number; name: string }[]>([])
const mockFetchSpaces = vi.fn()
const mockSetCurrentSpace = vi.fn()

vi.mock('@/stores/space', () => ({
  useSpaceStore: () => ({
    currentSpace: mockCurrentSpace.value,
    spaces: mockSpaces.value,
    fetchSpaces: mockFetchSpaces,
    setCurrentSpace: mockSetCurrentSpace,
  }),
}))

const mockListAiCallLogs = vi.fn()
vi.mock('@/api/ai', () => ({
  listAiCallLogs: (...args: unknown[]) => mockListAiCallLogs(...args),
}))

vi.mock('@/layouts/AppShell.vue', () => ({
  default: { name: 'AppShell', template: '<div class="app-shell-stub"><slot /></div>' },
}))

vi.mock('@lucide/vue', () => ({
  AlertCircle: { name: 'AlertCircle', template: '<span />' },
  Bot: { name: 'Bot', template: '<span />' },
  Clock3: { name: 'Clock3', template: '<span />' },
  RefreshCw: { name: 'RefreshCw', template: '<span />' },
  ShieldCheck: { name: 'ShieldCheck', template: '<span />' },
}))

import AiLogView from '../ai/AiLogView.vue'

const sampleLogs = [
  {
    id: 1,
    userId: 7,
    spaceId: 3,
    provider: 'mock',
    scenario: 'parse_todo',
    promptHash: 'abc123',
    requestJson: '{"inputLength":12}',
    responseJson: '{"taskCount":1}',
    status: 'success',
    durationMs: 18,
    errorMessage: null,
    createdAt: '2026-06-28T19:00:00',
  },
  {
    id: 2,
    userId: 7,
    spaceId: 3,
    provider: 'openai',
    scenario: 'monthly_report',
    promptHash: null,
    requestJson: '{"year":2026,"month":6}',
    responseJson: null,
    status: 'failed',
    durationMs: 240,
    errorMessage: 'provider timeout',
    createdAt: '2026-06-28T19:05:00',
  },
]

function mountAiLogView() {
  return mount(AiLogView, {
    global: {
      stubs: {
        ElButton: { template: '<button><slot /></button>', props: ['type', 'loading', 'size'] },
        ElSelect: { template: '<select><slot /></select>', props: ['modelValue', 'clearable', 'placeholder', 'size'] },
        ElOption: { template: '<option />', props: ['label', 'value'] },
        ElInputNumber: { template: '<input type="number" />', props: ['modelValue', 'min', 'max', 'step', 'size'] },
        ElTag: { template: '<span class="el-tag"><slot /></span>', props: ['type', 'size'] },
      },
    },
  })
}

describe('AiLogView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCurrentSpace.value = { id: 3, name: '家庭空间' }
    mockSpaces.value = [{ id: 3, name: '家庭空间' }]
    mockFetchSpaces.mockResolvedValue(undefined)
    mockListAiCallLogs.mockResolvedValue(sampleLogs)
  })

  it('loads AI call logs for the current space', async () => {
    const wrapper = mountAiLogView()
    await flushPromises()

    expect(mockFetchSpaces).toHaveBeenCalled()
    expect(mockListAiCallLogs).toHaveBeenCalledWith(3, {
      scenario: undefined,
      status: undefined,
      limit: 50,
    })
    expect(wrapper.text()).toContain('AI 调用日志')
    expect(wrapper.text()).toContain('待办草稿')
    expect(wrapper.text()).toContain('月度报告')
    expect(wrapper.text()).toContain('provider timeout')
    expect(wrapper.text()).toContain('abc123')
  })

  it('shows no-space state without loading logs', async () => {
    mockCurrentSpace.value = null
    mockSpaces.value = []
    const wrapper = mountAiLogView()
    await flushPromises()

    expect(mockListAiCallLogs).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('请先选择一个空间')
  })

  it('shows empty state when no logs exist', async () => {
    mockListAiCallLogs.mockResolvedValue([])
    const wrapper = mountAiLogView()
    await flushPromises()

    expect(wrapper.text()).toContain('暂无 AI 调用日志')
  })

  it('shows error state when loading fails', async () => {
    mockListAiCallLogs.mockRejectedValue(new Error('network error'))
    const wrapper = mountAiLogView()
    await flushPromises()

    expect(wrapper.text()).toContain('日志加载失败')
    expect(wrapper.text()).toContain('重新加载')
  })
})
