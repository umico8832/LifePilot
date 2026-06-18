<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshCw, FileText, AlertCircle, Plus, Trash2, Tags } from '@lucide/vue'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'
import {
  listTransactions,
  createTransaction,
  updateTransaction,
  deleteTransaction,
  type TransactionResponse,
  listCategories,
  createCategory,
  deleteCategory,
  type CategoryResponse,
} from '@/api/transaction'
import { parseTransaction, type TransactionDraft } from '@/api/ai'

const spaceStore = useSpaceStore()
const transactions = ref<TransactionResponse[]>([])
const loading = ref(false)
const loadError = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = ref({
  amount: 0,
  type: 'expense',
  merchant: '',
  note: '',
  categoryId: null as number | null,
})

// ---- Category management ----
const categories = ref<CategoryResponse[]>([])
const categoriesLoading = ref(false)
const categoriesError = ref(false)
const categoryDialogVisible = ref(false)
const newCategoryForm = ref({ name: '', icon: '', color: '' })

const filteredCategories = computed(() =>
  categories.value.filter((c) => c.type === form.value.type),
)

// ---- AI parse flow ----
const aiDialogVisible = ref(false)
const aiInputText = ref('')
const aiParsing = ref(false)
const aiDraftDialogVisible = ref(false)
const aiDraft = ref<TransactionDraft | null>(null)
const aiDraftForm = ref({
  amount: 0,
  type: 'expense',
  merchant: '',
  note: '',
  categoryName: '',
})

// ---- End AI flow ----

const totalExpense = computed(() =>
  transactions.value
    .filter((t) => t.type === 'expense')
    .reduce((sum, t) => sum + t.amount, 0),
)

const totalIncome = computed(() =>
  transactions.value
    .filter((t) => t.type === 'income')
    .reduce((sum, t) => sum + t.amount, 0),
)

// Watch type change to clear category if it doesn't match
watch(() => form.value.type, () => {
  if (form.value.categoryId) {
    const cat = categories.value.find(c => c.id === form.value.categoryId)
    if (cat && cat.type !== form.value.type) {
      form.value.categoryId = null
    }
  }
})

onMounted(async () => {
  await spaceStore.fetchSpaces()
  if (spaceStore.currentSpace) {
    await Promise.all([loadTransactions(), loadCategories()])
  }
})

async function loadTransactions() {
  if (!spaceStore.currentSpace) return
  loading.value = true
  loadError.value = false
  try {
    transactions.value = await listTransactions(spaceStore.currentSpace.id)
  } catch {
    loadError.value = true
    transactions.value = []
  } finally {
    loading.value = false
  }
}

async function handleSpaceChange() {
  await Promise.all([loadTransactions(), loadCategories()])
}

function openCreateDialog() {
  editingId.value = null
  form.value = { amount: 0, type: 'expense', merchant: '', note: '', categoryId: null }
  dialogVisible.value = true
}

function openEditDialog(tx: TransactionResponse) {
  editingId.value = tx.id
  form.value = {
    amount: tx.amount,
    type: tx.type,
    merchant: tx.merchant || '',
    note: tx.note || '',
    categoryId: tx.categoryId ?? null,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!spaceStore.currentSpace) return
  if (form.value.amount <= 0) {
    ElMessage.warning('金额必须大于 0')
    return
  }
  try {
    const payload = {
      amount: form.value.amount,
      type: form.value.type,
      merchant: form.value.merchant || undefined,
      note: form.value.note || undefined,
      categoryId: form.value.categoryId ?? undefined,
    }
    if (editingId.value) {
      await updateTransaction(spaceStore.currentSpace.id, editingId.value, payload)
      ElMessage.success('记录已更新')
    } else {
      await createTransaction(spaceStore.currentSpace.id, payload)
      ElMessage.success('记录已创建')
    }
    dialogVisible.value = false
    await loadTransactions()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(tx: TransactionResponse) {
  if (!spaceStore.currentSpace) return
  try {
    await ElMessageBox.confirm('确定删除这条记录？', '删除确认', { type: 'warning' })
    await deleteTransaction(spaceStore.currentSpace.id, tx.id)
    ElMessage.success('已删除')
    await loadTransactions()
  } catch {
    // cancelled or error
  }
}

function formatAmount(amount: number, type: string) {
  return (type === 'expense' ? '-' : '+') + '¥' + amount.toFixed(2)
}

// ---- Category management functions ----

async function loadCategories() {
  if (!spaceStore.currentSpace) return
  categoriesLoading.value = true
  categoriesError.value = false
  try {
    categories.value = await listCategories(spaceStore.currentSpace.id)
  } catch {
    categoriesError.value = true
    categories.value = []
  } finally {
    categoriesLoading.value = false
  }
}

function getCategoryName(id: number | null): string {
  if (!id) return ''
  const cat = categories.value.find((c) => c.id === id)
  return cat ? cat.name : ''
}

function openCategoryDialog() {
  newCategoryForm.value = { name: '', icon: '', color: '' }
  categoryDialogVisible.value = true
}

async function handleCreateCategory(type: string) {
  if (!spaceStore.currentSpace) return
  if (!newCategoryForm.value.name.trim()) {
    ElMessage.warning('分类名称不能为空')
    return
  }
  try {
    const created = await createCategory(spaceStore.currentSpace.id, {
      name: newCategoryForm.value.name.trim(),
      type,
      icon: newCategoryForm.value.icon.trim() || undefined,
      color: newCategoryForm.value.color.trim() || undefined,
    })
    categories.value.push(created)
    ElMessage.success(`分类「${created.name}」已创建`)
    newCategoryForm.value = { name: '', icon: '', color: '' }
  } catch {
    ElMessage.error('创建分类失败')
  }
}

async function handleDeleteCategory(cat: CategoryResponse) {
  if (!spaceStore.currentSpace) return
  try {
    await ElMessageBox.confirm(`确定删除分类「${cat.name}」？`, '删除确认', { type: 'warning' })
    await deleteCategory(spaceStore.currentSpace.id, cat.id)
    categories.value = categories.value.filter((c) => c.id !== cat.id)
    if (form.value.categoryId === cat.id) {
      form.value.categoryId = null
    }
    ElMessage.success('分类已删除')
  } catch {
    // cancelled or error
  }
}

// ---- AI parse flow ----

function openAiDialog() {
  aiInputText.value = ''
  aiDraft.value = null
  aiDraftDialogVisible.value = false
  aiDialogVisible.value = true
}

async function handleAiParse() {
  if (!spaceStore.currentSpace) return
  if (!aiInputText.value.trim()) {
    ElMessage.warning('请输入描述')
    return
  }
  aiParsing.value = true
  try {
    const draft = await parseTransaction(spaceStore.currentSpace.id, aiInputText.value.trim())
    aiDraft.value = draft

    // Pre-fill draft form from parsed data
    aiDraftForm.value = {
      amount: draft.amount ?? 0,
      type: draft.type ?? 'expense',
      merchant: draft.merchant ?? '',
      note: draft.note ?? '',
      categoryName: draft.categoryName ?? '',
    }

    // Switch to draft review dialog
    aiDialogVisible.value = false
    aiDraftDialogVisible.value = true
  } catch {
    ElMessage.error('解析失败，请重试')
  } finally {
    aiParsing.value = false
  }
}

async function handleAiConfirm() {
  if (!spaceStore.currentSpace) return
  if (aiDraftForm.value.amount <= 0) {
    ElMessage.warning('金额必须大于 0')
    return
  }
  try {
    await createTransaction(spaceStore.currentSpace.id, {
      amount: aiDraftForm.value.amount,
      type: aiDraftForm.value.type,
      merchant: aiDraftForm.value.merchant || undefined,
      note: aiDraftForm.value.note || undefined,
    })
    ElMessage.success('记录已创建')
    aiDraftDialogVisible.value = false
    aiDraft.value = null
    await loadTransactions()
  } catch {
    ElMessage.error('创建记录失败')
  }
}

function handleAiEdit() {
  // Close draft dialog and open manual create dialog pre-filled
  aiDraftDialogVisible.value = false
  editingId.value = null
  form.value = {
    amount: aiDraftForm.value.amount,
    type: aiDraftForm.value.type,
    merchant: aiDraftForm.value.merchant,
    note: aiDraftForm.value.note,
    categoryId: null,
  }
  dialogVisible.value = true
}
</script>

<template>
  <AppShell>
    <div class="finance-page">
      <h1>记账</h1>
      <p class="page-desc">管理你的收入和支出记录。</p>

      <!-- Space selector -->
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
        <el-button @click="openCategoryDialog">
          <Tags :size="14" />
          分类管理
        </el-button>
        <el-button @click="openAiDialog">🤖 AI 记账</el-button>
        <el-button type="primary" @click="openCreateDialog">记一笔</el-button>
      </div>

      <!-- Summary -->
      <div class="summary-band responsive-wrap">
        <div class="summary-card">
          <span class="summary-label">支出合计</span>
          <span class="summary-value expense">¥{{ totalExpense.toFixed(2) }}</span>
        </div>
        <div class="summary-card">
          <span class="summary-label">收入合计</span>
          <span class="summary-value income">¥{{ totalIncome.toFixed(2) }}</span>
        </div>
        <div class="summary-card">
          <span class="summary-label">记录数</span>
          <span class="summary-value">{{ transactions.length }}</span>
        </div>
      </div>

      <!-- No space state -->
      <div v-if="!spaceStore.currentSpace" class="empty-state">
        <FileText :size="48" class="empty-icon" />
        <p class="empty-title">请先选择一个空间</p>
        <p class="empty-desc">记账数据归属于空间，请在顶部选择空间后开始记账。</p>
      </div>

      <!-- Error state -->
      <div v-else-if="loadError" class="error-state">
        <AlertCircle :size="48" class="error-icon" />
        <p class="empty-title">数据加载失败</p>
        <p class="empty-desc">无法加载记账记录，请检查网络后重试。</p>
        <el-button type="primary" size="small" @click="loadTransactions">
          <RefreshCw :size="14" />
          重新加载
        </el-button>
      </div>

      <!-- Transaction table -->
      <template v-else>
        <div class="table-scroll">
        <el-table :data="transactions" v-loading="loading" stripe style="width: 100%">
          <el-table-column label="类型" width="80">
            <template #default="{ row }">
              <el-tag :type="row.type === 'expense' ? 'danger' : 'success'" size="small">
                {{ row.type === 'expense' ? '支出' : '收入' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120">
            <template #default="{ row }">
              <span :class="row.type === 'expense' ? 'text-expense' : 'text-income'">
                {{ formatAmount(row.amount, row.type) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="分类" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.categoryId" size="small" type="info">
                {{ getCategoryName(row.categoryId) }}
              </el-tag>
              <span v-else class="no-category">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="merchant" label="商家" />
          <el-table-column prop="note" label="备注" />
          <el-table-column prop="occurredAt" label="时间" width="180" />
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" text @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        </div>

        <!-- Empty state for table -->
        <div v-if="!loading && transactions.length === 0" class="empty-state">
          <FileText :size="48" class="empty-icon" />
          <p class="empty-title">暂无记账记录</p>
          <p class="empty-desc">点击「记一笔」或「AI 记账」开始记录你的第一笔收支。</p>
        </div>
      </template>

      <!-- Create/Edit dialog -->
      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑记录' : '记一笔'" width="450px">
        <el-form label-width="70px">
          <el-form-item label="类型">
            <el-radio-group v-model="form.type">
              <el-radio value="expense">支出</el-radio>
              <el-radio value="income">收入</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="金额">
            <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="10" style="width: 100%" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select
              v-model="form.categoryId"
              placeholder="选择分类（可选）"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="cat in filteredCategories"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
              >
                <span>
                  <span v-if="cat.icon" style="margin-right: 4px">{{ cat.icon }}</span>
                  {{ cat.name }}
                </span>
              </el-option>
            </el-select>
            <p v-if="filteredCategories.length === 0" class="category-hint">
              暂无{{ form.type === 'expense' ? '支出' : '收入' }}分类，请在「分类管理」中添加。
            </p>
          </el-form-item>
          <el-form-item label="商家">
            <el-input v-model="form.merchant" placeholder="商家名称（可选）" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.note" type="textarea" :rows="2" placeholder="备注（可选）" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">{{ editingId ? '更新' : '创建' }}</el-button>
        </template>
      </el-dialog>

      <!-- Category management dialog -->
      <el-dialog v-model="categoryDialogVisible" title="分类管理" width="520px">
        <div v-if="categoriesLoading" class="category-loading">加载中...</div>
        <div v-else-if="categoriesError" class="category-loading">加载分类失败，请重试。</div>
        <template v-else>
          <div class="category-section">
            <h4 class="category-section-title">支出分类</h4>
            <div v-if="categories.filter(c => c.type === 'expense').length === 0" class="category-empty">
              暂无支出分类
            </div>
            <div v-else class="category-list">
              <div
                v-for="cat in categories.filter(c => c.type === 'expense')"
                :key="cat.id"
                class="category-item"
              >
                <span class="category-item-name">
                  <span v-if="cat.icon" style="margin-right: 4px">{{ cat.icon }}</span>
                  {{ cat.name }}
                </span>
                <el-button size="small" text type="danger" @click="handleDeleteCategory(cat)">
                  <Trash2 :size="14" />
                </el-button>
              </div>
            </div>
          </div>
          <div class="category-section">
            <h4 class="category-section-title">收入分类</h4>
            <div v-if="categories.filter(c => c.type === 'income').length === 0" class="category-empty">
              暂无收入分类
            </div>
            <div v-else class="category-list">
              <div
                v-for="cat in categories.filter(c => c.type === 'income')"
                :key="cat.id"
                class="category-item"
              >
                <span class="category-item-name">
                  <span v-if="cat.icon" style="margin-right: 4px">{{ cat.icon }}</span>
                  {{ cat.name }}
                </span>
                <el-button size="small" text type="danger" @click="handleDeleteCategory(cat)">
                  <Trash2 :size="14" />
                </el-button>
              </div>
            </div>
          </div>
          <el-divider />
          <div class="add-category-form">
            <el-input
              v-model="newCategoryForm.name"
              placeholder="新分类名称"
              size="default"
              class="add-category-input"
              @keydown.enter="handleCreateCategory('expense')"
            />
            <el-input
              v-model="newCategoryForm.icon"
              placeholder="图标"
              size="default"
              style="width: 80px"
            />
            <el-button type="primary" @click="handleCreateCategory('expense')">
              <Plus :size="14" />
              支出
            </el-button>
            <el-button type="success" @click="handleCreateCategory('income')">
              <Plus :size="14" />
              收入
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- AI input dialog -->
      <el-dialog v-model="aiDialogVisible" title="🤖 AI 自然语言记账" width="500px">
        <p class="ai-hint">用自然语言描述你的收支，AI 会帮你解析。例如：</p>
        <div class="ai-examples">
          <el-tag
            v-for="ex in ['午餐花了32元', '超市购物120.5', '收到工资8000元', '打车25块', '咖啡28']"
            :key="ex"
            size="small"
            class="ai-example-tag"
            @click="aiInputText = ex"
          >{{ ex }}</el-tag>
        </div>
        <el-input
          v-model="aiInputText"
          type="textarea"
          :rows="3"
          placeholder="输入描述，例如：午餐花了32元"
          @keydown.enter.ctrl="handleAiParse"
        />
        <p class="ai-note">⚠️ 以上由 Mock AI 解析，结果需要您确认。</p>
        <template #footer>
          <el-button @click="aiDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="aiParsing" @click="handleAiParse">解析</el-button>
        </template>
      </el-dialog>

      <!-- AI draft review dialog -->
      <el-dialog v-model="aiDraftDialogVisible" title="确认 AI 解析结果" width="500px">
        <div v-if="aiDraft">
          <el-alert
            v-if="aiDraft.needsReview"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          >
            {{ aiDraft.validationMessage || '此解析结果可能不准确，请确认后再保存。' }}
          </el-alert>
          <p class="ai-raw-input">
            原文：<em>{{ aiDraft.rawInput }}</em>
          </p>
          <el-form label-width="70px">
            <el-form-item label="类型">
              <el-radio-group v-model="aiDraftForm.type">
                <el-radio value="expense">支出</el-radio>
                <el-radio value="income">收入</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="金额">
              <el-input-number v-model="aiDraftForm.amount" :min="0.01" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
            <el-form-item label="商家">
              <el-input v-model="aiDraftForm.merchant" placeholder="商家名称" />
            </el-form-item>
            <el-form-item v-if="aiDraftForm.categoryName" label="分类">
              <el-tag>{{ aiDraftForm.categoryName }}</el-tag>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="aiDraftForm.note" type="textarea" :rows="2" placeholder="备注" />
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <el-button @click="aiDraftDialogVisible = false">取消</el-button>
          <el-button @click="handleAiEdit">手动编辑</el-button>
          <el-button type="primary" @click="handleAiConfirm">确认创建</el-button>
        </template>
      </el-dialog>
    </div>
  </AppShell>
</template>

<style scoped>
.finance-page {
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

.summary-band {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  flex: 1;
  padding: 16px;
  border-radius: 8px;
  background: var(--color-card-bg, #f9f9f9);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-label {
  font-size: 13px;
  color: var(--color-muted, #888);
}

.summary-value {
  font-size: 20px;
  font-weight: 600;
}

.summary-value.expense {
  color: var(--el-color-danger, #f56c6c);
}

.summary-value.income {
  color: var(--el-color-success, #67c23a);
}

.text-expense {
  color: var(--el-color-danger, #f56c6c);
  font-weight: 500;
}

.text-income {
  color: var(--el-color-success, #67c23a);
  font-weight: 500;
}

.ai-hint {
  color: var(--color-muted, #888);
  font-size: 14px;
  margin-bottom: 12px;
}

.ai-examples {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.ai-example-tag {
  cursor: pointer;
}

.ai-note {
  font-size: 12px;
  color: var(--el-color-warning, #e6a23c);
  margin-top: 12px;
}

.ai-raw-input {
  font-size: 14px;
  color: var(--color-muted, #888);
  margin-bottom: 12px;
}

.ai-raw-input em {
  color: var(--color-text, #333);
  font-style: normal;
  font-weight: 500;
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

.no-category {
  color: var(--color-muted, #ccc);
  font-size: 13px;
}

.category-hint {
  font-size: 12px;
  color: var(--color-muted, #aaa);
  margin-top: 4px;
}

.category-section {
  margin-bottom: 16px;
}

.category-section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text, #333);
  margin: 0 0 8px;
}

.category-empty {
  font-size: 13px;
  color: var(--color-muted, #aaa);
  padding: 8px 0;
}

.category-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: 1px solid var(--el-border-color-lighter, #e4e7ed);
  border-radius: 6px;
  font-size: 13px;
}

.category-item-name {
  color: var(--color-text, #333);
}

.category-loading {
  text-align: center;
  padding: 24px 0;
  color: var(--color-muted, #888);
  font-size: 14px;
}

.add-category-form {
  display: flex;
  gap: 8px;
  align-items: center;
}

.add-category-input {
  flex: 1;
}
</style>