<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import {
  CircleCheck,
  CircleX,
  DollarSign,
  ShoppingCart,
  Package,
  AlertTriangle,
  LogIn,
  LogOut,
  RefreshCw,
} from '@lucide/vue'
import { useRouter } from 'vue-router'

import { fetchHealth } from '@/api/health'
import { getOverview, type OverviewResponse } from '@/api/statistics'
import AppShell from '@/layouts/AppShell.vue'
import { useAuthStore } from '@/stores/auth'
import { useSpaceStore } from '@/stores/space'

const healthStatus = ref('checking')
const healthMessage = ref('正在检查后端服务')
const router = useRouter()
const authStore = useAuthStore()
const spaceStore = useSpaceStore()

const overview = ref<OverviewResponse | null>(null)
const overviewLoading = ref(false)
const overviewError = ref('')

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

async function loadOverview() {
  if (!authStore.isAuthenticated || !spaceStore.currentSpace) return
  overviewLoading.value = true
  overviewError.value = ''
  try {
    overview.value = await getOverview(spaceStore.currentSpace.id)
  } catch {
    overviewError.value = '统计数据加载失败'
  } finally {
    overviewLoading.value = false
  }
}

function formatAmount(val: number): string {
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function logout() {
  authStore.logout()
}

onMounted(async () => {
  await Promise.all([loadHealth(), authStore.loadCurrentUser()])
  if (authStore.isAuthenticated) {
    await spaceStore.fetchSpaces()
    await loadOverview()
  }
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
      <div v-if="authStore.isAuthenticated && spaceStore.currentSpace" class="status-card">
        <DollarSign :size="24" class="status-icon" />
        <div>
          <p class="status-label">本月结余</p>
          <p class="status-value" v-if="overview">
            ¥{{ formatAmount(overview.netBalance) }}
          </p>
          <p class="status-value" v-else-if="overviewLoading">加载中…</p>
          <p class="status-value status-error" v-else>{{ overviewError || '暂无数据' }}</p>
        </div>
      </div>
      <div v-if="authStore.isAuthenticated && spaceStore.currentSpace" class="status-card">
        <span class="metric">{{ overview ? overview.transactionCount : '—' }}</span>
        <div>
          <p class="status-label">记账笔数</p>
          <p class="status-value">
            {{ overview ? `收入 ¥${formatAmount(overview.totalIncome)}` : '—' }}
          </p>
        </div>
      </div>
    </section>

    <section v-if="authStore.isAuthenticated && overview" class="overview-cards">
      <div class="overview-card" @click="router.push('/finance')">
        <DollarSign :size="20" />
        <div>
          <p class="overview-label">总收入</p>
          <p class="overview-amount income">¥{{ formatAmount(overview.totalIncome) }}</p>
        </div>
      </div>
      <div class="overview-card" @click="router.push('/finance')">
        <DollarSign :size="20" />
        <div>
          <p class="overview-label">总支出</p>
          <p class="overview-amount expense">¥{{ formatAmount(overview.totalExpense) }}</p>
        </div>
      </div>
      <div class="overview-card" @click="router.push('/inventory')">
        <Package :size="20" />
        <div>
          <p class="overview-label">库存物品</p>
          <p class="overview-amount">{{ overview.inventoryItemCount }} 件</p>
        </div>
      </div>
      <div class="overview-card" @click="router.push('/shopping')">
        <ShoppingCart :size="20" />
        <div>
          <p class="overview-label">购物清单</p>
          <p class="overview-amount">{{ overview.shoppingListCount }} 个</p>
        </div>
      </div>
      <div v-if="overview.inventoryAlertCount > 0" class="overview-card alert-card" @click="router.push('/inventory')">
        <AlertTriangle :size="20" class="alert-icon" />
        <div>
          <p class="overview-label">库存预警</p>
          <p class="overview-amount alert">{{ overview.inventoryAlertCount }} 项</p>
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

<style scoped>
.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 24px;
}

.overview-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: var(--color-surface);
  border-radius: 12px;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: border-color 0.2s;
}

.overview-card:hover {
  border-color: var(--color-primary);
}

.overview-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0;
}

.overview-amount {
  font-size: 18px;
  font-weight: 600;
  margin: 2px 0 0;
}

.overview-amount.income {
  color: #16a34a;
}

.overview-amount.expense {
  color: #dc2626;
}

.overview-amount.alert {
  color: #f59e0b;
}

.status-icon {
  color: var(--color-primary);
}

.alert-card {
  border-color: #fde68a;
  background: #fffbeb;
}

.alert-icon {
  color: #f59e0b;
}

.status-error {
  color: var(--color-text-muted);
}
</style>