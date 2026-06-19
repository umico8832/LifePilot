import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

// Mock space store
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

// Mock transaction API
const mockListTransactions = vi.fn()
const mockCreateTransaction = vi.fn()
const mockUpdateTransaction = vi.fn()
const mockDeleteTransaction = vi.fn()
const mockListCategories = vi.fn()
const mockCreateCategory = vi.fn()
const mockDeleteCategory = vi.fn()
vi.mock('@/api/transaction', () => ({
  listTransactions: (...args: unknown[]) => mockListTransactions(...args),
  createTransaction: (...args: unknown[]) => mockCreateTransaction(...args),
  updateTransaction: (...args: unknown[]) => mockUpdateTransaction(...args),
  deleteTransaction: (...args: unknown[]) => mockDeleteTransaction(...args),
  listCategories: (...args: unknown[]) => mockListCategories(...args),
  createCategory: (...args: unknown[]) => mockCreateCategory(...args),
  deleteCategory: (...args: unknown[]) => mockDeleteCategory(...args),
}))

// Mock AI API
const mockParseTransaction = vi.fn()
vi.mock('@/api/ai', () => ({
  parseTransaction: (...args: unknown[]) => mockParseTransaction(...args),
}))

// Stub AppShell
vi.mock('@/layouts/AppShell.vue', () => ({
  default: { name: 'AppShell', template: '<div class="app-shell-stub"><slot /></div>' },
}))

// Stub Lucide icons
vi.mock('@lucide/vue', () => ({
  RefreshCw: { name: 'RefreshCw', template: '<span />' },
  FileText: { name: 'FileText', template: '<span />' },
  AlertCircle: { name: 'AlertCircle', template: '<span />' },
  Plus: { name: 'Plus', template: '<span />' },
  Trash2: { name: 'Trash2', template: '<span />' },
  Tags: { name: 'Tags', template: '<span />' },
}))

import FinanceView from '../finance/FinanceView.vue'

const sampleTransactions = [
  { id: 1, type: 'expense', amount: 50, merchant: '超市', note: '买菜', categoryId: 1, occurredAt: '2026-06-01' },
  { id: 2, type: 'income', amount: 100, merchant: '公司', note: '工资', categoryId: 2, occurredAt: '2026-06-02' },
]

const sampleCategories = [
  { id: 1, name: '餐饮', type: 'expense', icon: '🍜', color: '#f56c6c' },
  { id: 2, name: '工资', type: 'income', icon: '💰', color: '#67c23a' },
]

function mountFinance() {
  return mount(FinanceView, {
    global: {
      stubs: {
        ElButton: { template: '<button><slot /></button>' },
        // ElTable / ElTableColumn: use opaque stubs that don't invoke column slots
        // to avoid Element Plus internal slot forwarding issues
        ElTable: {
          props: ['data'],
          template: '<div class="el-table-stub" :data-count="(data||[]).length"></div>',
        },
        ElTableColumn: { template: '' },
        ElTag: { template: '<span class="el-tag"><slot /></span>', props: ['type', 'size'] },
        ElDialog: {
          template: '<div class="el-dialog-stub" v-if="modelValue"><slot /><slot name="footer" /></div>',
          props: ['modelValue', 'title', 'width'],
        },
        ElForm: { template: '<form><slot /></form>' },
        ElFormItem: { template: '<div><slot /></div>' },
        ElRadioGroup: { template: '<div><slot /></div>' },
        ElRadio: { template: '<label><slot /></label>', props: ['value'] },
        ElInputNumber: { template: '<input type="number" />', props: ['modelValue'] },
        ElInput: { template: '<input />', props: ['modelValue'] },
        ElSelect: { template: '<select><slot /></select>', props: ['modelValue'] },
        ElOption: { template: '<option />', props: ['value', 'label'] },
        ElMessageBox: { confirm: vi.fn().mockResolvedValue('confirm') },
        ElAlert: { template: '<div><slot /></div>', props: ['type'] },
        ElDivider: { template: '<hr />' },
      },
    },
  })
}

describe('FinanceView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    mockCurrentSpace.value = { id: 1, name: 'Test Space' }
    mockSpaces.value = [{ id: 1, name: 'Test Space' }]
    mockListTransactions.mockResolvedValue(sampleTransactions)
    mockListCategories.mockResolvedValue(sampleCategories)
    mockFetchSpaces.mockResolvedValue(undefined)
  })

  it('shows no-space state when currentSpace is null', async () => {
    mockCurrentSpace.value = null
    mockSpaces.value = []
    const wrapper = mountFinance()
    await flushPromises()

    expect(wrapper.text()).toContain('请先选择一个空间')
  })

  it('renders page title and description', async () => {
    const wrapper = mountFinance()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('记账')
    expect(wrapper.text()).toContain('管理你的收入和支出记录')
  })

  it('loads transactions and categories when space is selected', async () => {
    const wrapper = mountFinance()
    await flushPromises()

    expect(mockListTransactions).toHaveBeenCalledWith(1)
    expect(mockListCategories).toHaveBeenCalledWith(1)
    // ElTable stub should be rendered
    expect(wrapper.find('.el-table-stub').exists()).toBe(true)
  })

  it('shows error state when loading transactions fails', async () => {
    mockListTransactions.mockRejectedValue(new Error('network error'))
    const wrapper = mountFinance()
    await flushPromises()

    expect(wrapper.text()).toContain('数据加载失败')
    expect(wrapper.text()).toContain('无法加载记账记录')
    expect(wrapper.text()).toContain('重新加载')
  })

  it('shows empty state when no transactions exist', async () => {
    mockListTransactions.mockResolvedValue([])
    const wrapper = mountFinance()
    await flushPromises()

    expect(wrapper.text()).toContain('暂无记账记录')
    expect(wrapper.text()).toContain('点击「记一笔」')
  })

  it('displays summary with correct totals', async () => {
    const wrapper = mountFinance()
    await flushPromises()

    const text = wrapper.text()
    // totalExpense = 50, totalIncome = 100, count = 2
    expect(text).toContain('支出合计')
    expect(text).toContain('收入合计')
    expect(text).toContain('记录数')
    expect(text).toContain('50.00')
    expect(text).toContain('100.00')
  })

  it('shows toolbar buttons', async () => {
    const wrapper = mountFinance()
    await flushPromises()

    const text = wrapper.text()
    expect(text).toContain('AI 记账')
    expect(text).toContain('分类管理')
    expect(text).toContain('记一笔')
  })

  it('does not load transactions when no space is selected', async () => {
    mockCurrentSpace.value = null
    mockSpaces.value = []
    mountFinance()
    await flushPromises()

    expect(mockListTransactions).not.toHaveBeenCalled()
    expect(mockListCategories).not.toHaveBeenCalled()
  })

  it('shows 0 summary values when transactions list is empty', async () => {
    mockListTransactions.mockResolvedValue([])
    mockListCategories.mockResolvedValue([])
    const wrapper = mountFinance()
    await flushPromises()

    const text = wrapper.text()
    expect(text).toContain('0.00')
  })

  it('loads categories on mount', async () => {
    mountFinance()
    await flushPromises()

    expect(mockListCategories).toHaveBeenCalledWith(1)
  })

  it('renders space selector when spaces exist', async () => {
    mockSpaces.value = [
      { id: 1, name: 'Space A' },
      { id: 2, name: 'Space B' },
    ]
    const wrapper = mountFinance()
    await flushPromises()

    // el-select stub should be rendered
    expect(wrapper.find('select').exists()).toBe(true)
  })

  it('does not render space selector when no spaces exist', async () => {
    mockCurrentSpace.value = null
    mockSpaces.value = []
    const wrapper = mountFinance()
    await flushPromises()

    expect(wrapper.find('select').exists()).toBe(false)
  })
})