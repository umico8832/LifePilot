import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ref, computed } from 'vue'

// Mock vue-router
const mockPush = vi.fn()
const mockCurrentRoute = ref({ query: {} as Record<string, string> })
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush, currentRoute: computed(() => mockCurrentRoute.value) }),
}))

// Mock auth store
const mockLogin = vi.fn()
const mockRegister = vi.fn()
const mockLoading = ref(false)
vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    login: mockLogin,
    register: mockRegister,
    loading: mockLoading.value,
  }),
}))

// Stub AppShell to a simple pass-through
vi.mock('@/layouts/AppShell.vue', () => ({
  default: { name: 'AppShell', template: '<div class="app-shell-stub"><slot /></div>' },
}))

// Stub Lucide icons
vi.mock('@lucide/vue', () => ({
  ArrowLeft: { name: 'ArrowLeft', template: '<span />' },
  LockKeyhole: { name: 'LockKeyhole', template: '<span />' },
  UserPlus: { name: 'UserPlus', template: '<span />' },
}))

import AuthView from '../auth/AuthView.vue'

function mountAuth() {
  return mount(AuthView, {
    global: {
      stubs: {
        ElButton: { template: '<button><slot /></button>' },
      },
    },
  })
}

describe('AuthView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    mockCurrentRoute.value = { query: {} }
    mockLoading.value = false
  })

  it('renders login mode by default', () => {
    const wrapper = mountAuth()
    expect(wrapper.find('h1').text()).toBe('登录')
    // Login mode should not show displayName field
    expect(wrapper.find('label:has(input[autocomplete="name"])').exists()).toBe(false)
    // Email and password fields should exist
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })

  it('switches to register mode when clicking register tab', async () => {
    const wrapper = mountAuth()
    const tabs = wrapper.findAll('button[role="tab"]')
    expect(tabs.length).toBe(2)

    await tabs[1].trigger('click')

    expect(wrapper.find('h1').text()).toBe('注册')
    // Register mode should show displayName field
    expect(wrapper.find('input[autocomplete="name"]').exists()).toBe(true)
  })

  it('switches back to login mode', async () => {
    const wrapper = mountAuth()
    const tabs = wrapper.findAll('button[role="tab"]')

    // Switch to register
    await tabs[1].trigger('click')
    expect(wrapper.find('h1').text()).toBe('注册')

    // Switch back to login
    await tabs[0].trigger('click')
    expect(wrapper.find('h1').text()).toBe('登录')
    expect(wrapper.find('input[autocomplete="name"]').exists()).toBe(false)
  })

  it('calls authStore.login on form submit in login mode', async () => {
    mockLogin.mockResolvedValue(undefined)
    const wrapper = mountAuth()

    await wrapper.find('input[type="email"]').setValue('user@example.com')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit')

    await flushPromises()

    expect(mockLogin).toHaveBeenCalledWith({
      email: 'user@example.com',
      password: 'password123',
    })
    expect(mockPush).toHaveBeenCalledWith('/')
  })

  it('calls authStore.register on form submit in register mode', async () => {
    mockRegister.mockResolvedValue(undefined)
    const wrapper = mountAuth()

    // Switch to register
    await wrapper.findAll('button[role="tab"]')[1].trigger('click')

    await wrapper.find('input[type="email"]').setValue('new@example.com')
    await wrapper.find('input[autocomplete="name"]').setValue('New User')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit')

    await flushPromises()

    expect(mockRegister).toHaveBeenCalledWith({
      email: 'new@example.com',
      password: 'password123',
      displayName: 'New User',
    })
  })

  it('redirects to query param after login', async () => {
    mockLogin.mockResolvedValue(undefined)
    mockCurrentRoute.value = { query: { redirect: '/finance' } }
    const wrapper = mountAuth()

    await wrapper.find('input[type="email"]').setValue('user@example.com')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit')

    await flushPromises()

    expect(mockPush).toHaveBeenCalledWith('/finance')
  })

  it('displays error message when login fails', async () => {
    const error = {
      response: { data: { message: '用户名或密码错误' } },
    }
    mockLogin.mockRejectedValue(error)
    const wrapper = mountAuth()

    await wrapper.find('input[type="email"]').setValue('user@example.com')
    await wrapper.find('input[type="password"]').setValue('wrongpassword')
    await wrapper.find('form').trigger('submit')

    await flushPromises()

    expect(wrapper.find('.form-error').text()).toBe('用户名或密码错误')
  })

  it('displays generic error when error has no message', async () => {
    mockLogin.mockRejectedValue(new Error('network error'))
    const wrapper = mountAuth()

    await wrapper.find('input[type="email"]').setValue('user@example.com')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit')

    await flushPromises()

    expect(wrapper.find('.form-error').text()).toBe('请求失败')
  })

  it('clears error message on new submit', async () => {
    // First submit fails
    mockLogin.mockRejectedValueOnce({ response: { data: { message: 'Error' } } })
    const wrapper = mountAuth()

    await wrapper.find('input[type="email"]').setValue('user@example.com')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('.form-error').exists()).toBe(true)

    // Second submit succeeds
    mockLogin.mockResolvedValue(undefined)
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('.form-error').exists()).toBe(false)
  })

  it('has correct segmented control tabs', () => {
    const wrapper = mountAuth()
    const tabs = wrapper.findAll('button[role="tab"]')
    expect(tabs[0].text()).toBe('登录')
    expect(tabs[1].text()).toBe('注册')
  })

  it('login tab is active by default', () => {
    const wrapper = mountAuth()
    const tabs = wrapper.findAll('button[role="tab"]')
    expect(tabs[0].classes()).toContain('active')
    expect(tabs[1].classes()).not.toContain('active')
  })

  it('submit button text changes based on mode', async () => {
    const wrapper = mountAuth()
    const submitButton = wrapper.find('form button[type="submit"]')
    expect(submitButton.text()).toContain('登录')

    await wrapper.findAll('button[role="tab"]')[1].trigger('click')
    const submitButtonRegister = wrapper.find('form button[type="submit"]')
    expect(submitButtonRegister.text()).toContain('注册')
  })

  it('navigate back button calls router.push with /', async () => {
    const wrapper = mountAuth()
    const backButton = wrapper.find('.text-button')
    await backButton.trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/')
  })
})