import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/auth',
    name: 'auth',
    component: () => import('@/views/auth/AuthView.vue'),
    meta: { guestOnly: true },
  },
  {
    path: '/spaces',
    name: 'spaces',
    component: () => import('@/views/space/SpaceView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/spaces/invitations/accept',
    name: 'acceptInvitation',
    component: () => import('@/views/space/AcceptInvitationView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/finance',
    name: 'finance',
    component: () => import('@/views/finance/FinanceView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/shopping',
    name: 'shopping',
    component: () => import('@/views/shopping/ShoppingView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/inventory',
    name: 'inventory',
    component: () => import('@/views/inventory/InventoryView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/todo',
    name: 'todo',
    component: () => import('@/views/todo/TodoView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/recipe',
    name: 'recipe',
    component: () => import('@/views/recipe/RecipeView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/mealplan',
    name: 'mealplan',
    component: () => import('@/views/mealplan/MealPlanView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/document',
    name: 'document',
    component: () => import('@/views/document/DocumentView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/ai-logs',
    name: 'aiLogs',
    component: () => import('@/views/ai/AiLogView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/profile/ProfileView.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  // On first load with a token, validate it by fetching the current user
  if (authStore.token && !authStore.user) {
    await authStore.loadCurrentUser()
  }

  const isAuthenticated = authStore.isAuthenticated

  if (to.meta.requiresAuth && !isAuthenticated) {
    // Redirect unauthenticated users to login
    next({ name: 'auth', query: { redirect: to.fullPath } })
  } else if (to.meta.guestOnly && isAuthenticated) {
    // Redirect authenticated users away from auth page
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router
