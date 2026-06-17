<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { CircleCheck, CircleX, LogIn, LogOut, RefreshCw } from '@lucide/vue'
import { useRouter } from 'vue-router'

import { fetchHealth } from '@/api/health'
import AppShell from '@/layouts/AppShell.vue'
import { useAuthStore } from '@/stores/auth'

const healthStatus = ref('checking')
const healthMessage = ref('正在检查后端服务')
const router = useRouter()
const authStore = useAuthStore()

async function loadHealth() {
  healthStatus.value = 'checking'
  healthMessage.value = '正在检查后端服务'

  try {
    const result = await fetchHealth()
    healthStatus.value = result.data.status
    healthMessage.value = `${result.data.service} ${result.data.status}`
  } catch {
    healthStatus.value = 'offline'
    healthMessage.value = '后端暂未连接'
  }
}

function logout() {
  authStore.logout()
}

onMounted(async () => {
  await Promise.all([loadHealth(), authStore.loadCurrentUser()])
})
</script>

<template>
  <AppShell>
    <section class="dashboard-header">
      <div>
        <p class="eyebrow">AI 个人生活管家平台</p>
        <h1>LifePilot</h1>
        <p class="summary">
          把记账、购物、库存、待办和生活分析放进同一个可持续演进的工作台。
        </p>
      </div>
      <div class="header-actions">
        <button
          v-if="authStore.isAuthenticated"
          class="text-button"
          type="button"
          title="退出登录"
          @click="logout"
        >
          <LogOut :size="18" />
          {{ authStore.user?.displayName || '已登录' }}
        </button>
        <button v-else class="text-button" type="button" title="登录" @click="router.push('/auth')">
          <LogIn :size="18" />
          登录
        </button>
        <button class="icon-button" type="button" title="刷新服务状态" @click="loadHealth">
          <RefreshCw :size="18" />
        </button>
      </div>
    </section>

    <section class="status-band">
      <div class="status-card">
        <component
          :is="healthStatus === 'UP' ? CircleCheck : CircleX"
          :class="healthStatus === 'UP' ? 'status-good' : 'status-bad'"
          :size="24"
        />
        <div>
          <p class="status-label">后端服务</p>
          <p class="status-value">{{ healthMessage }}</p>
        </div>
      </div>
      <div class="status-card">
        <span class="metric">Phase 6</span>
        <div>
          <p class="status-label">当前阶段</p>
          <p class="status-value">家庭库存</p>
        </div>
      </div>
      <div class="status-card">
        <span class="metric">{{ authStore.isAuthenticated ? 'JWT' : 'P0-006' }}</span>
        <div>
          <p class="status-label">{{ authStore.isAuthenticated ? '当前用户' : '下一任务' }}</p>
          <p class="status-value">
            {{ authStore.isAuthenticated ? authStore.user?.email : '生活空间模型' }}
          </p>
        </div>
      </div>
    </section>

    <section class="module-grid" aria-label="LifePilot modules">
      <article class="module-panel" :class="{ clickable: authStore.isAuthenticated }" @click="authStore.isAuthenticated && router.push('/spaces')">
        <h2>生活空间</h2>
        <p>个人空间、家庭空间、成员管理与权限控制。</p>
      </article>
      <article class="module-panel" :class="{ clickable: authStore.isAuthenticated }" @click="authStore.isAuthenticated && router.push('/finance')">
        <h2>智能记账</h2>
        <p>收入、支出、分类、预算和自然语言录入。</p>
      </article>
      <article class="module-panel" :class="{ clickable: authStore.isAuthenticated }" @click="authStore.isAuthenticated && router.push('/shopping')">
        <h2>购物清单</h2>
        <p>清单项、采购状态、预算估算和库存联动。</p>
      </article>
      <article class="module-panel" :class="{ clickable: authStore.isAuthenticated }" @click="authStore.isAuthenticated && router.push('/inventory')">
        <h2>家庭库存</h2>
        <p>食品、日用品、常备物品、位置和临期提醒。</p>
      </article>
      <article class="module-panel">
        <h2>AI 生活助手</h2>
        <p>结构化解析、生活总结和温和优化建议。</p>
      </article>
    </section>
  </AppShell>
</template>
