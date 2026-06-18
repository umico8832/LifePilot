<script setup lang="ts">
import { computed, onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ListTodo, AlertCircle, RefreshCw, Sparkles, Plus, Delete } from '@lucide/vue'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'
import {
  listTodoTasks,
  createTodoTask,
  updateTodoTask,
  deleteTodoTask,
  type TodoResponse,
} from '@/api/todo'
import { parseTodo, type TodoDraft } from '@/api/ai'

const spaceStore = useSpaceStore()
const tasks = ref<TodoResponse[]>([])
const loading = ref(false)
const loadError = ref(false)
const filterStatus = ref<string>('')
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

// ---- AI todo draft ----
const aiInput = ref('')
const aiParsing = ref(false)
const aiDraftDialogVisible = ref(false)
const aiDraft = ref<TodoDraft | null>(null)
const editableDraft = reactive({
  items: [] as Array<{ title: string; description: string; priority: string; dueAt: string | null }>,
})

const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'urgent' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待处理', value: 'pending' },
  { label: '进行中', value: 'in_progress' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
]

const priorityTagType: Record<string, string> = {
  low: 'info',
  medium: '',
  high: 'warning',
  urgent: 'danger',
}

const priorityLabel: Record<string, string> = {
  low: '低',
  medium: '中',
  high: '高',
  urgent: '紧急',
}

const statusTagType: Record<string, string> = {
  pending: 'warning',
  in_progress: '',
  completed: 'success',
  cancelled: 'info',
}

const statusLabel: Record<string, string> = {
  pending: '待处理',
  in_progress: '进行中',
  completed: '已完成',
  cancelled: '已取消',
}

onMounted(async () => {
  await spaceStore.fetchSpaces()
  if (spaceStore.currentSpace) {
    await loadTasks()
  }
})

async function loadTasks() {
  if (!spaceStore.currentSpace) return
  loading.value = true
  loadError.value = false
  try {
    tasks.value = await listTodoTasks(spaceStore.currentSpace.id, filterStatus.value || undefined)
  } catch {
    loadError.value = true
    tasks.value = []
  } finally {
    loading.value = false
  }
}

async function handleFilterChange(val: string) {
  filterStatus.value = val
  await loadTasks()
}

async function handleSpaceChange() {
  filterStatus.value = ''
  await loadTasks()
}

// ---- AI todo draft ----

async function handleAiParse() {
  if (!spaceStore.currentSpace) return
  const text = aiInput.value.trim()
  if (!text) {
    ElMessage.warning('请输入待办描述，如"买菜、打扫房间"')
    return
  }
  aiParsing.value = true
  try {
    const draft = await parseTodo(spaceStore.currentSpace.id, text)
    aiDraft.value = draft
    editableDraft.items = draft.items.map((item) => ({
      title: item.title,
      description: item.description || '',
      priority: item.priority || 'medium',
      dueAt: item.dueAt || null,
    }))
    aiDraftDialogVisible.value = true
  } catch {
    ElMessage.error('AI 解析失败，请重试')
  } finally {
    aiParsing.value = false
  }
}

function addDraftItem() {
  editableDraft.items.push({ title: '', description: '', priority: 'medium', dueAt: null })
}

function removeDraftItem(index: number) {
  editableDraft.items.splice(index, 1)
}

async function confirmDraft() {
  if (!spaceStore.currentSpace) return
  const validItems = editableDraft.items.filter((item) => item.title.trim())
  if (validItems.length === 0) {
    ElMessage.warning('至少添加一个待办事项')
    return
  }

  try {
    for (const item of validItems) {
      await createTodoTask(spaceStore.currentSpace.id, {
        title: item.title,
        description: item.description || undefined,
        priority: item.priority,
        dueAt: item.dueAt || undefined,
      })
    }

    ElMessage.success(`已创建 ${validItems.length} 个待办任务`)
    aiDraftDialogVisible.value = false
    aiInput.value = ''
    aiDraft.value = null
    await loadTasks()
  } catch {
    ElMessage.error('创建待办任务失败')
  }
}

// ---- CRUD ----

const form = ref({
  title: '',
  description: '',
  priority: 'medium',
  dueAt: '',
})

function openCreateDialog() {
  editingId.value = null
  form.value = { title: '', description: '', priority: 'medium', dueAt: '' }
  dialogVisible.value = true
}

function openEditDialog(task: TodoResponse) {
  editingId.value = task.id
  form.value = {
    title: task.title,
    description: task.description || '',
    priority: task.priority,
    dueAt: task.dueAt || '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!spaceStore.currentSpace) return
  if (!form.value.title.trim()) {
    ElMessage.warning('任务标题不能为空')
    return
  }
  try {
    const payload = {
      title: form.value.title,
      description: form.value.description || undefined,
      priority: form.value.priority,
      dueAt: form.value.dueAt || undefined,
    }
    if (editingId.value) {
      await updateTodoTask(spaceStore.currentSpace.id, editingId.value, payload)
      ElMessage.success('任务已更新')
    } else {
      await createTodoTask(spaceStore.currentSpace.id, payload)
      ElMessage.success('任务已创建')
    }
    dialogVisible.value = false
    await loadTasks()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleStatusChange(task: TodoResponse, newStatus: string) {
  if (!spaceStore.currentSpace) return
  try {
    await updateTodoTask(spaceStore.currentSpace.id, task.id, { status: newStatus })
    ElMessage.success('状态已更新')
    await loadTasks()
  } catch {
    ElMessage.error('状态更新失败')
  }
}

async function handleDelete(task: TodoResponse) {
  if (!spaceStore.currentSpace) return
  try {
    await ElMessageBox.confirm('确定删除该任务？', '删除确认', { type: 'warning' })
    await deleteTodoTask(spaceStore.currentSpace.id, task.id)
    ElMessage.success('已删除')
    await loadTasks()
  } catch {
    // cancelled or error
  }
}

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const pendingCount = computed(() => tasks.value.filter(t => t.status === 'pending').length)
const overdueCount = computed(() => tasks.value.filter(t => t.overdue).length)
</script>

<template>
  <AppShell>
    <div class="todo-page">
      <h1>生活待办</h1>
      <p class="page-desc">管理日常任务和待办事项，跟踪完成状态。</p>

      <!-- AI todo assistant -->
      <div v-if="spaceStore.currentSpace" class="ai-section">
        <div class="ai-input-row">
          <el-input
            v-model="aiInput"
            placeholder='用自然语言描述待办事项，如"明天买菜、紧急处理客户反馈"'
            :disabled="aiParsing"
            clearable
            @keyup.enter="handleAiParse"
          />
          <el-button
            type="primary"
            :loading="aiParsing"
            @click="handleAiParse"
          >
            <Sparkles :size="14" />
            AI 生成
          </el-button>
        </div>
        <p class="ai-hint">AI 助手 · 自动解析优先级和截止时间</p>
      </div>

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
        <el-select
          v-model="filterStatus"
          placeholder="筛选状态"
          size="default"
          style="width: 120px"
          @change="handleFilterChange"
        >
          <el-option
            v-for="opt in statusOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <div style="flex: 1" />
        <el-button type="primary" @click="openCreateDialog">添加任务</el-button>
      </div>

      <!-- Summary bar -->
      <div v-if="spaceStore.currentSpace && tasks.length > 0" class="summary-bar">
        <span class="summary-item">共 {{ tasks.length }} 项</span>
        <span class="summary-item">待处理 {{ pendingCount }}</span>
        <span v-if="overdueCount > 0" class="summary-item summary-overdue">逾期 {{ overdueCount }}</span>
      </div>

      <!-- No space state -->
      <div v-if="!spaceStore.currentSpace" class="empty-state">
        <ListTodo :size="48" class="empty-icon" />
        <p class="empty-title">请先选择一个空间</p>
        <p class="empty-desc">待办数据归属于空间，请在顶部选择空间后开始管理。</p>
      </div>

      <!-- Error state -->
      <div v-else-if="loadError" class="error-state">
        <AlertCircle :size="48" class="error-icon" />
        <p class="empty-title">数据加载失败</p>
        <p class="empty-desc">无法加载待办数据，请检查网络后重试。</p>
        <el-button type="primary" size="small" @click="loadTasks">
          <RefreshCw :size="14" />
          重新加载
        </el-button>
      </div>

      <!-- Table + empty -->
      <template v-else>
        <div class="table-scroll">
        <el-table :data="tasks" v-loading="loading" stripe style="width: 100%">
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="(statusTagType[row.status] as any)" size="small">
                {{ statusLabel[row.status] || row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="优先级" width="80">
            <template #default="{ row }">
              <el-tag :type="(priorityTagType[row.priority] as any)" size="small">
                {{ priorityLabel[row.priority] || row.priority }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标题" prop="title" min-width="200" />
          <el-table-column label="截止日期" width="120">
            <template #default="{ row }">
              <span :class="{ 'overdue-text': row.overdue }">
                {{ formatDate(row.dueAt) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280">
            <template #default="{ row }">
              <el-button v-if="row.status === 'pending'" size="small" text type="primary" @click="handleStatusChange(row, 'in_progress')">开始</el-button>
              <el-button v-if="row.status === 'in_progress'" size="small" text type="success" @click="handleStatusChange(row, 'completed')">完成</el-button>
              <el-button v-if="row.status === 'pending'" size="small" text type="success" @click="handleStatusChange(row, 'completed')">完成</el-button>
              <el-button v-if="row.status !== 'completed' && row.status !== 'cancelled'" size="small" text type="info" @click="handleStatusChange(row, 'cancelled')">取消</el-button>
              <el-button size="small" text @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        </div>

        <!-- Empty state -->
        <div v-if="!loading && tasks.length === 0" class="empty-state">
          <ListTodo :size="48" class="empty-icon" />
          <p class="empty-title">{{ filterStatus ? '没有匹配的任务' : '暂无待办任务' }}</p>
          <p class="empty-desc">{{ filterStatus ? '当前筛选条件下没有任务。' : '点击「添加任务」或使用 AI 助手创建你的第一个待办。' }}</p>
        </div>
      </template>

      <!-- Create/Edit dialog -->
      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑任务' : '添加任务'" width="500px">
        <el-form label-width="100px">
          <el-form-item label="任务标题">
            <el-input v-model="form.title" placeholder="请输入任务标题" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="任务详细描述（可选）" />
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="form.priority" style="width: 100%">
              <el-option
                v-for="opt in priorityOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="截止日期">
            <el-date-picker
              v-model="form.dueAt"
              type="datetime"
              placeholder="选择截止日期（可选）"
              style="width: 100%"
              value-format="YYYY-MM-DDTHH:mm:ss"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">{{ editingId ? '更新' : '创建' }}</el-button>
        </template>
      </el-dialog>

      <!-- AI draft review dialog -->
      <el-dialog v-model="aiDraftDialogVisible" title="AI 生成待办任务" width="580px">
        <div v-if="aiDraft" class="ai-draft-content">
          <p v-if="aiDraft.validationMessage" class="ai-draft-warning">
            {{ aiDraft.validationMessage }}
          </p>

          <div class="draft-items-header">
            <span>待办事项（{{ editableDraft.items.length }} 项）</span>
            <el-button size="small" text type="primary" @click="addDraftItem">
              <Plus :size="14" />
              添加
            </el-button>
          </div>

          <div v-if="editableDraft.items.length === 0" class="draft-empty">
            暂无待办，请手动添加。
          </div>

          <div
            v-for="(item, index) in editableDraft.items"
            :key="index"
            class="draft-item-card"
          >
            <div class="draft-item-row">
              <el-input
                v-model="item.title"
                placeholder="任务标题"
                style="flex: 2"
              />
              <el-select
                v-model="item.priority"
                style="width: 90px"
                size="default"
              >
                <el-option label="低" value="low" />
                <el-option label="中" value="medium" />
                <el-option label="高" value="high" />
                <el-option label="紧急" value="urgent" />
              </el-select>
              <el-button
                text
                type="danger"
                size="small"
                @click="removeDraftItem(index)"
                :disabled="editableDraft.items.length <= 1"
              >
                <Delete :size="14" />
              </el-button>
            </div>
            <div class="draft-item-meta">
              <el-date-picker
                v-model="item.dueAt"
                type="datetime"
                placeholder="截止日期（可选）"
                size="small"
                style="flex: 1"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </div>
          </div>

          <p class="ai-draft-note">
            以上内容由 AI mock 解析生成，请检查后确认创建。
          </p>
        </div>
        <template #footer>
          <el-button @click="aiDraftDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDraft">确认创建</el-button>
        </template>
      </el-dialog>
    </div>
  </AppShell>
</template>

<style scoped>
.todo-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-desc {
  color: var(--color-muted, #888);
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.summary-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--color-muted, #888);
}

.summary-item {
  padding: 4px 8px;
  background: var(--el-fill-color-light, #f5f5f5);
  border-radius: 4px;
}

.summary-overdue {
  color: var(--el-color-danger, #f56c6c);
  font-weight: bold;
}

.overdue-text {
  color: var(--el-color-danger, #f56c6c);
  font-weight: bold;
}

.empty-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}

.empty-icon {
  color: var(--color-muted, #aaa);
  margin-bottom: 16px;
}

.error-icon {
  color: var(--el-color-danger, #f56c6c);
  margin-bottom: 16px;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text, #333);
  margin: 0 0 8px;
}

.empty-desc {
  font-size: 13px;
  color: var(--color-muted, #888);
  margin: 0 0 16px;
}

/* ---- AI section ---- */

.ai-section {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--el-fill-color-light, #f5f7fa);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter, #e4e7ed);
}

.ai-input-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.ai-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--color-muted, #999);
}

/* ---- AI draft dialog ---- */

.ai-draft-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-draft-warning {
  margin: 0;
  padding: 8px 12px;
  background: var(--el-color-warning-light-9, #fdf6ec);
  border-radius: 4px;
  color: var(--el-color-warning, #e6a23c);
  font-size: 13px;
}

.draft-items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text, #333);
  margin-top: 4px;
}

.draft-empty {
  padding: 20px;
  text-align: center;
  color: var(--color-muted, #999);
  font-size: 13px;
}

.draft-item-card {
  padding: 8px;
  border: 1px solid var(--el-border-color-lighter, #e4e7ed);
  border-radius: 6px;
  background: var(--el-fill-color-blank, #fff);
}

.draft-item-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.draft-item-meta {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.ai-draft-note {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--color-muted, #999);
  text-align: center;
}

@media (max-width: 600px) {
  .ai-input-row {
    flex-direction: column;
    align-items: stretch;
  }

  .draft-item-row {
    flex-wrap: wrap;
  }
}
</style>