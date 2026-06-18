<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Home, Receipt, ShoppingCart, Package, LogOut, LayoutGrid, ListTodo, ChefHat } from '@lucide/vue'

import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const displayName = computed(() => authStore.user?.displayName || authStore.user?.email || '')

interface NavItem {
  label: string
  routeName: string
  icon: typeof Home
}

const navItems: NavItem[] = [
  { label: '总览', routeName: 'home', icon: Home },
  { label: '空间', routeName: 'spaces', icon: LayoutGrid },
  { label: '记账', routeName: 'finance', icon: Receipt },
  { label: '购物', routeName: 'shopping', icon: ShoppingCart },
  { label: '库存', routeName: 'inventory', icon: Package },
  { label: '待办', routeName: 'todo', icon: ListTodo },
  { label: '菜谱', routeName: 'recipe', icon: ChefHat },
]

function isActive(name: string) {
  return route.name === name
}

function navigateTo(name: string) {
  router.push({ name })
}

function handleLogout() {
  authStore.logout()
  router.push({ name: 'auth' })
}
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">LP</span>
        <span class="brand-name">LifePilot</span>
      </div>
      <nav class="nav-list" aria-label="Primary">
        <button
          v-for="item in navItems"
          :key="item.routeName"
          class="nav-item"
          :class="{ active: isActive(item.routeName) }"
          @click="navigateTo(item.routeName)"
        >
          <component :is="item.icon" :size="18" />
          <span class="nav-label">{{ item.label }}</span>
        </button>
      </nav>
      <div v-if="isAuthenticated" class="sidebar-footer">
        <span class="user-label" :title="displayName">{{ displayName }}</span>
        <button class="logout-button" title="退出登录" @click="handleLogout">
          <LogOut :size="16" />
        </button>
      </div>
    </aside>
    <main class="content">
      <slot />
    </main>
  </div>
</template>