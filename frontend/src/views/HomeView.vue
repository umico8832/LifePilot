<script setup lang="ts">
import { onMounted, ref } from 'vue'
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
  FileText,
} from '@lucide/vue'
import { useRouter } from 'vue-router'

import { fetchHealth } from '@/api/health'
import { getOverview, type OverviewResponse } from '@/api/statistics'
import { generateMonthlyReport, type MonthlyReport } from '@/api/ai'
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

// Monthly report
const reportLoading = ref(false)
const reportDialogVisible = ref(false)
const monthlyReport = ref<MonthlyReport | null>(null)

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

async function handleGenerateReport() {
  if (!spaceStore.currentSpace) return
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  reportLoading.value = true
  try {
    monthlyReport.value = await generateMonthlyReport(spaceStore.currentSpace.id, year, month)
    reportDialogVisible.value = true
  } catch {
    // handled silently
  } finally {
    reportLoading.value = false
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

    <!-- Monthly report action -->
    <section v-if="authStore.isAuthenticated && spaceStore.currentSpace" class="report-section">
      <el-button
        type="primary"
        :loading="reportLoading"
        @click="handleGenerateReport"
      >
        <FileText :size="14" />
        生成本月生活报告
      </el-button>
      <p class="report-hint">AI 助手 · 聚合财务、库存、购物和待办数据生成月度报告</p>
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
      <article class="module-panel" :class="{ clickable: authStore.isAuthenticated }" @click="authStore.isAuthenticated && router.push('/todo')">
        <h2>生活待办</h2>
        <p>日常任务、状态流转、优先级和截止日期管理。</p>
      </article>
      <article class="module-panel">
        <h2>AI 生活助手</h2>
        <p>结构化解析、生活总结和温和优化建议。</p>
      </article>
    </section>

    <!-- Monthly report dialog -->
    <el-dialog v-model="reportDialogVisible" title="AI 月度生活报告" width="600px">
      <div v-if="monthlyReport" class="report-content">
        <div class="report-highlights">
          <p class="report-section-title">📊 本月亮点</p>
          <ul>
            <li v-for="(h, i) in monthlyReport.highlights" :key="i">{{ h }}</li>
          </ul>
        </div>

        <div class="report-finance-summary">
          <div class="report-stat">
            <span class="stat-label">收入</span>
            <span class="stat-value income">¥{{ formatAmount(monthlyReport.finance.totalIncome) }}</span>
          </div>
          <div class="report-stat">
            <span class="stat-label">支出</span>
            <span class="stat-value expense">¥{{ formatAmount(monthlyReport.finance.totalExpense) }}</span>
          </div>
          <div class="report-stat">
            <span class="stat-label">结余</span>
            <span class="stat-value" :class="monthlyReport.finance.balance >= 0 ? 'income' : 'expense'">
              ¥{{ formatAmount(monthlyReport.finance.balance) }}
            </span>
          </div>
          <div class="report-stat">
            <span class="stat-label">交易</span>
            <span class="stat-value">{{ monthlyReport.finance.transactionCount }} 笔</span>
          </div>
        </div>

        <div v-if="monthlyReport.finance.topExpenseCategories.length > 0" class="report-categories">
          <p class="report-section-title">💸 支出分类 TOP</p>
          <div
            v-for="cat in monthlyReport.finance.topExpenseCategories.slice(0, 5)"
            :key="cat.name"
            class="category-row"
          >
            <span class="cat-name">{{ cat.name }}</span>
            <span class="cat-amount">¥{{ formatAmount(cat.amount) }}</span>
            <span class="cat-count">{{ cat.count }} 笔</span>
          </div>
        </div>

        <div class="report-summary-grid">
          <div class="report-stat">
            <span class="stat-label">📦 库存</span>
            <span class="stat-value">{{ monthlyReport.inventory.totalItems }} 件</span>
          </div>
          <div v-if="monthlyReport.inventory.lowStockCount > 0" class="report-stat">
            <span class="stat-label">⚠️ 低库存</span>
            <span class="stat-value alert">{{ monthlyReport.inventory.lowStockCount }} 件</span>
          </div>
          <div class="report-stat">
            <span class="stat-label">🛒 购物清单</span>
            <span class="stat-value">{{ monthlyReport.shopping.listCount }} 个</span>
          </div>
          <div class="report-stat">
            <span class="stat-label">✅ 待办</span>
            <span class="stat-value">{{ monthlyReport.todo.pendingCount }} 待处理 / {{ monthlyReport.todo.completedCount }} 已完成</span>
          </div>
          <div v-if="monthlyReport.todo.overdueCount > 0" class="report-stat">
            <span class="stat-label">⏰ 逾期</span>
            <span class="stat-value alert">{{ monthlyReport.todo.overdueCount }} 项</span>
          </div>
        </div>

        <div class="report-suggestions">
          <p class="report-section-title">💡 建议</p>
          <ul>
            <li v-for="(s, i) in monthlyReport.suggestions" :key="i">{{ s }}</li>
          </ul>
        </div>

        <el-collapse>
          <el-collapse-item title="查看完整报告文本">
            <pre class="report-text">{{ monthlyReport.reportText }}</pre>
          </el-collapse-item>
        </el-collapse>
      </div>
      <template #footer>
        <el-button @click="reportDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
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

/* ---- Report section ---- */

.report-section {
  margin-bottom: 24px;
  padding: 16px;
  background: var(--el-fill-color-light, #f5f7fa);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter, #e4e7ed);
  display: flex;
  align-items: center;
  gap: 12px;
}

.report-hint {
  font-size: 12px;
  color: var(--color-muted, #999);
  margin: 0;
}

/* ---- Report dialog ---- */

.report-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.report-section-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 8px;
  color: var(--color-text, #333);
}

.report-highlights ul,
.report-suggestions ul {
  margin: 0;
  padding-left: 20px;
}

.report-highlights li,
.report-suggestions li {
  font-size: 13px;
  margin-bottom: 4px;
  color: var(--color-text, #333);
}

.report-finance-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
  padding: 12px;
  background: var(--el-fill-color-light, #f5f7fa);
  border-radius: 8px;
}

.report-summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 8px;
}

.report-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-label {
  font-size: 12px;
  color: var(--color-muted, #888);
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
}

.stat-value.income {
  color: #16a34a;
}

.stat-value.expense {
  color: #dc2626;
}

.stat-value.alert {
  color: #f59e0b;
}

.report-categories {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.category-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.cat-name {
  flex: 1;
  font-weight: 500;
}

.cat-amount {
  font-weight: 600;
  color: var(--color-text, #333);
}

.cat-count {
  color: var(--color-muted, #888);
  font-size: 12px;
  min-width: 40px;
  text-align: right;
}

.report-text {
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--color-text, #333);
  margin: 0;
  font-family: inherit;
}

@media (max-width: 600px) {
  .report-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>