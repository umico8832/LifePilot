<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { AlertCircle, Bot, Clock3, RefreshCw, ShieldCheck } from '@lucide/vue'

import AppShell from '@/layouts/AppShell.vue'
import { listAiCallLogs, type AiCallLog } from '@/api/ai'
import { useSpaceStore } from '@/stores/space'

const spaceStore = useSpaceStore()
const logs = ref<AiCallLog[]>([])
const loading = ref(false)
const loadError = ref(false)
const scenarioFilter = ref('')
const statusFilter = ref('')
const limit = ref(50)

const scenarioOptions = [
  { label: '自然语言记账', value: 'parse_transaction' },
  { label: '购物清单草稿', value: 'parse_shopping' },
  { label: '待办草稿', value: 'parse_todo' },
  { label: '月度报告', value: 'monthly_report' },
  { label: '菜谱推荐', value: 'recommend_recipes' },
  { label: '饮食采购草稿', value: 'meal_plan_shopping_draft' },
]

const statusOptions = [
  { label: '成功', value: 'success' },
  { label: '失败', value: 'failed' },
]

const scenarioLabelMap = Object.fromEntries(scenarioOptions.map(item => [item.value, item.label]))
const statusLabelMap: Record<string, string> = {
  success: '成功',
  failed: '失败',
}

const successCount = computed(() => logs.value.filter(log => log.status === 'success').length)
const failedCount = computed(() => logs.value.filter(log => log.status === 'failed').length)
const averageDuration = computed(() => {
  const durations = logs.value
    .map(log => log.durationMs)
    .filter((duration): duration is number => typeof duration === 'number')
  if (durations.length === 0) return 0
  return Math.round(durations.reduce((sum, duration) => sum + duration, 0) / durations.length)
})

onMounted(async () => {
  await spaceStore.fetchSpaces()
  if (spaceStore.currentSpace) {
    await loadLogs()
  }
})

async function loadLogs() {
  if (!spaceStore.currentSpace) return
  loading.value = true
  loadError.value = false
  try {
    logs.value = await listAiCallLogs(spaceStore.currentSpace.id, {
      scenario: scenarioFilter.value || undefined,
      status: statusFilter.value || undefined,
      limit: limit.value,
    })
  } catch {
    loadError.value = true
    logs.value = []
  } finally {
    loading.value = false
  }
}

async function handleSpaceChange() {
  scenarioFilter.value = ''
  statusFilter.value = ''
  await loadLogs()
}

function formatScenario(scenario: string) {
  return scenarioLabelMap[scenario] || scenario
}

function formatStatus(status: string) {
  return statusLabelMap[status] || status
}

function formatDateTime(value: string) {
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatDuration(duration: number | null) {
  return typeof duration === 'number' ? `${duration} ms` : '-'
}

function summarizeJson(value: string | null) {
  if (!value) return '-'
  try {
    const parsed = JSON.parse(value) as Record<string, unknown>
    const entries = Object.entries(parsed)
    if (entries.length === 0) return '{}'
    return entries
      .slice(0, 4)
      .map(([key, item]) => `${key}: ${formatJsonValue(item)}`)
      .join(' · ')
  } catch {
    return value
  }
}

function formatJsonValue(value: unknown) {
  if (Array.isArray(value)) return `${value.length} 项`
  if (value === null || value === undefined) return '-'
  if (typeof value === 'object') return '对象'
  return String(value)
}
</script>

<template>
  <AppShell>
    <div class="ai-log-page">
      <header class="page-header">
        <div>
          <p class="eyebrow">AI 审计</p>
          <h1>AI 调用日志</h1>
          <p class="page-desc">查看当前空间内的 AI 调用记录、脱敏摘要、状态和耗时。</p>
        </div>
        <div class="privacy-note">
          <ShieldCheck :size="18" />
          <span>自然语言原文不会显示在日志中</span>
        </div>
      </header>

      <div class="toolbar">
        <el-select
          v-if="spaceStore.spaces.length > 0"
          :model-value="spaceStore.currentSpace?.id"
          placeholder="选择空间"
          size="default"
          @change="(val: number) => { const s = spaceStore.spaces.find(sp => sp.id === val); if (s) { spaceStore.setCurrentSpace(s); handleSpaceChange() } }"
        >
          <el-option
            v-for="s in spaceStore.spaces"
            :key="s.id"
            :label="s.name"
            :value="s.id"
          />
        </el-select>
        <el-select v-model="scenarioFilter" clearable placeholder="场景" size="default" class="filter-control">
          <el-option label="全部场景" value="" />
          <el-option
            v-for="item in scenarioOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select v-model="statusFilter" clearable placeholder="状态" size="default" class="filter-control">
          <el-option label="全部状态" value="" />
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-input-number v-model="limit" :min="1" :max="100" :step="10" size="default" class="limit-control" />
        <el-button type="primary" :loading="loading" @click="loadLogs">
          <RefreshCw :size="14" />
          查询
        </el-button>
      </div>

      <div v-if="spaceStore.currentSpace && logs.length > 0" class="summary-grid">
        <div class="summary-card">
          <span class="summary-label">记录数</span>
          <strong>{{ logs.length }}</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">成功</span>
          <strong class="success-text">{{ successCount }}</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">失败</span>
          <strong class="failed-text">{{ failedCount }}</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">平均耗时</span>
          <strong>{{ averageDuration }} ms</strong>
        </div>
      </div>

      <div v-if="!spaceStore.currentSpace" class="empty-state">
        <Bot :size="48" class="empty-icon" />
        <p class="empty-title">请先选择一个空间</p>
        <p class="empty-desc">AI 调用日志归属于空间，请在顶部选择空间后查看。</p>
      </div>

      <div v-else-if="loadError" class="error-state">
        <AlertCircle :size="48" class="error-icon" />
        <p class="empty-title">日志加载失败</p>
        <p class="empty-desc">无法读取 AI 调用日志，请检查网络或稍后重试。</p>
        <el-button type="primary" size="small" @click="loadLogs">
          <RefreshCw :size="14" />
          重新加载
        </el-button>
      </div>

      <div v-else class="log-list" :class="{ 'is-loading': loading }">
        <div v-if="loading" class="loading-state">正在加载日志...</div>

        <article v-for="log in logs" :key="log.id" class="log-card">
          <div class="log-card-header">
            <div>
              <div class="log-title-row">
                <span class="scenario-label">{{ formatScenario(log.scenario) }}</span>
                <el-tag :type="log.status === 'success' ? 'success' : 'danger'" size="small">
                  {{ formatStatus(log.status) }}
                </el-tag>
              </div>
              <p class="log-meta">
                {{ log.provider }} · {{ formatDateTime(log.createdAt) }}
              </p>
            </div>
            <span class="duration-pill">
              <Clock3 :size="14" />
              {{ formatDuration(log.durationMs) }}
            </span>
          </div>

          <div class="log-detail-grid">
            <div class="detail-block">
              <span class="detail-label">请求摘要</span>
              <p>{{ summarizeJson(log.requestJson) }}</p>
            </div>
            <div class="detail-block">
              <span class="detail-label">响应摘要</span>
              <p>{{ summarizeJson(log.responseJson) }}</p>
            </div>
            <div class="detail-block">
              <span class="detail-label">Prompt Hash</span>
              <p class="hash-text">{{ log.promptHash || '-' }}</p>
            </div>
            <div class="detail-block">
              <span class="detail-label">错误摘要</span>
              <p>{{ log.errorMessage || '-' }}</p>
            </div>
          </div>
        </article>

        <div v-if="!loading && logs.length === 0" class="empty-state">
          <Bot :size="48" class="empty-icon" />
          <p class="empty-title">暂无 AI 调用日志</p>
          <p class="empty-desc">当前筛选条件下没有记录。使用 AI 记账、月报或菜谱推荐后会在这里看到审计摘要。</p>
        </div>
      </div>
    </div>
  </AppShell>
</template>

<style scoped>
.ai-log-page {
  display: grid;
  gap: 20px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.page-header h1 {
  font-size: 32px;
}

.page-desc {
  margin: 10px 0 0;
  color: #4b5565;
  font-size: 15px;
  line-height: 1.6;
}

.privacy-note {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 40px;
  padding: 8px 12px;
  border: 1px solid #b7dfd2;
  border-radius: 8px;
  color: #146c5a;
  background: #f0faf6;
  font-size: 13px;
  font-weight: 650;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  padding: 14px;
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  background: #ffffff;
}

.filter-control {
  width: 170px;
}

.limit-control {
  width: 132px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  display: grid;
  gap: 6px;
  min-height: 76px;
  padding: 14px;
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  background: #ffffff;
}

.summary-label {
  color: #667085;
  font-size: 13px;
}

.summary-card strong {
  color: #172026;
  font-size: 22px;
}

.success-text {
  color: #14804a !important;
}

.failed-text {
  color: #c03744 !important;
}

.log-list {
  display: grid;
  gap: 12px;
}

.log-list.is-loading {
  opacity: 0.72;
}

.loading-state,
.empty-state,
.error-state {
  display: grid;
  justify-items: center;
  gap: 8px;
  padding: 44px 20px;
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  background: #ffffff;
  text-align: center;
}

.empty-icon {
  color: #98a2b3;
}

.error-icon {
  color: #c03744;
}

.empty-title {
  margin: 0;
  color: #172026;
  font-size: 16px;
  font-weight: 700;
}

.empty-desc {
  max-width: 520px;
  margin: 0;
  color: #667085;
  font-size: 14px;
  line-height: 1.6;
}

.log-card {
  display: grid;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  background: #ffffff;
}

.log-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.log-title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.scenario-label {
  color: #172026;
  font-weight: 700;
}

.log-meta {
  margin: 6px 0 0;
  color: #667085;
  font-size: 13px;
}

.duration-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 30px;
  padding: 5px 9px;
  border-radius: 8px;
  color: #1b4d63;
  background: #eef7fb;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.log-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-block {
  min-width: 0;
  padding: 12px;
  border-radius: 8px;
  background: #f7f8fa;
}

.detail-label {
  color: #667085;
  font-size: 12px;
  font-weight: 700;
}

.detail-block p {
  margin: 6px 0 0;
  color: #344054;
  font-size: 13px;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.hash-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", monospace;
}

@media (max-width: 900px) {
  .page-header {
    display: grid;
  }

  .summary-grid,
  .log-detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .filter-control,
  .limit-control,
  .toolbar :deep(.el-select),
  .toolbar :deep(.el-input-number) {
    width: 100%;
  }

  .toolbar :deep(.el-button) {
    width: 100%;
  }

  .log-card-header {
    display: grid;
  }
}
</style>
