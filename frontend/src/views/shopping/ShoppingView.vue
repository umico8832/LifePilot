<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, AlertCircle, RefreshCw, Sparkles, Plus, Delete } from '@lucide/vue'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'
import {
  listShoppingLists,
  getShoppingList,
  createShoppingList,
  updateShoppingList,
  deleteShoppingList,
  addShoppingItem,
  updateShoppingItem,
  deleteShoppingItem,
  type ShoppingListResponse,
  type ShoppingItemResponse,
} from '@/api/shopping'
import { parseShoppingList, type ShoppingDraft, type ShoppingDraftItem } from '@/api/ai'

const spaceStore = useSpaceStore()
const shoppingLists = ref<ShoppingListResponse[]>([])
const activeList = ref<ShoppingListResponse | null>(null)
const loading = ref(false)
const loadError = ref(false)
const listDialogVisible = ref(false)
const itemDialogVisible = ref(false)
const editingListId = ref<number | null>(null)
const editingItemId = ref<number | null>(null)

// ---- AI shopping draft ----
const aiInput = ref('')
const aiParsing = ref(false)
const aiDraftDialogVisible = ref(false)
const aiDraft = ref<ShoppingDraft | null>(null)
const editableDraft = reactive({
  listName: '',
  items: [] as Array<{ name: string; quantity: number; unit: string; estimatedPrice: number | null }>,
})

const listForm = ref({
  name: '',
  estimatedBudget: 0,
})

const itemForm = ref({
  name: '',
  quantity: 1,
  unit: '',
  estimatedPrice: 0,
})

onMounted(async () => {
  await spaceStore.fetchSpaces()
  if (spaceStore.currentSpace) {
    await loadLists()
  }
})

async function loadLists() {
  if (!spaceStore.currentSpace) return
  loading.value = true
  loadError.value = false
  try {
    shoppingLists.value = await listShoppingLists(spaceStore.currentSpace.id)
  } catch {
    loadError.value = true
    shoppingLists.value = []
  } finally {
    loading.value = false
  }
}

async function handleSpaceChange() {
  activeList.value = null
  await loadLists()
}

async function openListDetail(list: ShoppingListResponse) {
  if (!spaceStore.currentSpace) return
  try {
    activeList.value = await getShoppingList(spaceStore.currentSpace.id, list.id)
  } catch {
    ElMessage.error('加载清单详情失败')
  }
}

function backToList() {
  activeList.value = null
}

// ---- AI shopping draft ----

async function handleAiParse() {
  if (!spaceStore.currentSpace) return
  const text = aiInput.value.trim()
  if (!text) {
    ElMessage.warning('请输入描述，如"买苹果、牛奶、面包"')
    return
  }
  aiParsing.value = true
  try {
    const draft = await parseShoppingList(spaceStore.currentSpace.id, text)
    aiDraft.value = draft
    editableDraft.listName = draft.listName
    editableDraft.items = draft.items.map((item) => ({
      name: item.name,
      quantity: item.quantity,
      unit: item.unit || '',
      estimatedPrice: item.estimatedPrice,
    }))
    aiDraftDialogVisible.value = true
  } catch {
    ElMessage.error('AI 解析失败，请重试')
  } finally {
    aiParsing.value = false
  }
}

function addDraftItem() {
  editableDraft.items.push({ name: '', quantity: 1, unit: '', estimatedPrice: null })
}

function removeDraftItem(index: number) {
  editableDraft.items.splice(index, 1)
}

async function confirmDraft() {
  if (!spaceStore.currentSpace) return
  const validItems = editableDraft.items.filter((item) => item.name.trim())
  if (!editableDraft.listName.trim()) {
    ElMessage.warning('清单名称不能为空')
    return
  }
  if (validItems.length === 0) {
    ElMessage.warning('至少添加一个购物物品')
    return
  }

  try {
    // Create shopping list
    const list = await createShoppingList(spaceStore.currentSpace.id, {
      name: editableDraft.listName,
    })

    // Add all items
    for (const item of validItems) {
      await addShoppingItem(spaceStore.currentSpace.id, list.id, {
        name: item.name,
        quantity: item.quantity,
        unit: item.unit || undefined,
        estimatedPrice: item.estimatedPrice && item.estimatedPrice > 0 ? item.estimatedPrice : undefined,
      })
    }

    ElMessage.success('购物清单已创建')
    aiDraftDialogVisible.value = false
    aiInput.value = ''
    aiDraft.value = null
    await loadLists()
    // Open the newly created list
    await openListDetail(list)
  } catch {
    ElMessage.error('创建购物清单失败')
  }
}

// ---- List CRUD ----

function openCreateListDialog() {
  editingListId.value = null
  listForm.value = { name: '', estimatedBudget: 0 }
  listDialogVisible.value = true
}

function openEditListDialog(list: ShoppingListResponse) {
  editingListId.value = list.id
  listForm.value = {
    name: list.name,
    estimatedBudget: list.estimatedBudget || 0,
  }
  listDialogVisible.value = true
}

async function handleListSubmit() {
  if (!spaceStore.currentSpace) return
  if (!listForm.value.name.trim()) {
    ElMessage.warning('清单名称不能为空')
    return
  }
  try {
    if (editingListId.value) {
      await updateShoppingList(spaceStore.currentSpace.id, editingListId.value, {
        name: listForm.value.name,
        estimatedBudget: listForm.value.estimatedBudget > 0 ? listForm.value.estimatedBudget : undefined,
      })
      ElMessage.success('清单已更新')
    } else {
      await createShoppingList(spaceStore.currentSpace.id, {
        name: listForm.value.name,
        estimatedBudget: listForm.value.estimatedBudget > 0 ? listForm.value.estimatedBudget : undefined,
      })
      ElMessage.success('清单已创建')
    }
    listDialogVisible.value = false
    await loadLists()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDeleteList(list: ShoppingListResponse) {
  if (!spaceStore.currentSpace) return
  try {
    await ElMessageBox.confirm('确定删除该清单及其所有物品？', '删除确认', { type: 'warning' })
    await deleteShoppingList(spaceStore.currentSpace.id, list.id)
    ElMessage.success('已删除')
    activeList.value = null
    await loadLists()
  } catch {
    // cancelled or error
  }
}

// ---- Item CRUD ----

function openCreateItemDialog() {
  editingItemId.value = null
  itemForm.value = { name: '', quantity: 1, unit: '', estimatedPrice: 0 }
  itemDialogVisible.value = true
}

function openEditItemDialog(item: ShoppingItemResponse) {
  editingItemId.value = item.id
  itemForm.value = {
    name: item.name,
    quantity: item.quantity,
    unit: item.unit || '',
    estimatedPrice: item.estimatedPrice || 0,
  }
  itemDialogVisible.value = true
}

async function handleItemSubmit() {
  if (!spaceStore.currentSpace || !activeList.value) return
  if (!itemForm.value.name.trim()) {
    ElMessage.warning('物品名称不能为空')
    return
  }
  try {
    if (editingItemId.value) {
      await updateShoppingItem(spaceStore.currentSpace.id, activeList.value.id, editingItemId.value, {
        name: itemForm.value.name,
        quantity: itemForm.value.quantity,
        unit: itemForm.value.unit || undefined,
        estimatedPrice: itemForm.value.estimatedPrice > 0 ? itemForm.value.estimatedPrice : undefined,
      })
      ElMessage.success('物品已更新')
    } else {
      await addShoppingItem(spaceStore.currentSpace.id, activeList.value.id, {
        name: itemForm.value.name,
        quantity: itemForm.value.quantity,
        unit: itemForm.value.unit || undefined,
        estimatedPrice: itemForm.value.estimatedPrice > 0 ? itemForm.value.estimatedPrice : undefined,
      })
      ElMessage.success('物品已添加')
    }
    itemDialogVisible.value = false
    // Reload the detail
    activeList.value = await getShoppingList(spaceStore.currentSpace.id, activeList.value.id)
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleTogglePurchased(item: ShoppingItemResponse) {
  if (!spaceStore.currentSpace || !activeList.value) return
  try {
    await updateShoppingItem(spaceStore.currentSpace.id, activeList.value.id, item.id, {
      purchased: !item.purchased,
    })
    activeList.value = await getShoppingList(spaceStore.currentSpace.id, activeList.value.id)
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDeleteItem(item: ShoppingItemResponse) {
  if (!spaceStore.currentSpace || !activeList.value) return
  try {
    await ElMessageBox.confirm('确定删除该物品？', '删除确认', { type: 'warning' })
    await deleteShoppingItem(spaceStore.currentSpace.id, activeList.value.id, item.id)
    ElMessage.success('已删除')
    activeList.value = await getShoppingList(spaceStore.currentSpace.id, activeList.value.id)
  } catch {
    // cancelled or error
  }
}

function getStatusLabel(status: string) {
  return status === 'completed' ? '已完成' : '进行中'
}
</script>

<template>
  <AppShell>
    <div class="shopping-page">
      <!-- List overview -->
      <template v-if="!activeList">
        <h1>购物清单</h1>
        <p class="page-desc">管理你的购物清单和采购计划。</p>

        <!-- AI shopping assistant -->
        <div v-if="spaceStore.currentSpace" class="ai-section">
          <div class="ai-input-row">
            <el-input
              v-model="aiInput"
              placeholder='用自然语言描述要买的东西，如"买苹果、2斤牛奶、面包"'
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
          <p class="ai-hint">AI 助手 · 自动解析购物物品并创建清单</p>
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
          <el-button type="primary" @click="openCreateListDialog">新建清单</el-button>
        </div>

        <!-- No space state -->
        <div v-if="!spaceStore.currentSpace" class="empty-state">
          <ShoppingCart :size="48" class="empty-icon" />
          <p class="empty-title">请先选择一个空间</p>
          <p class="empty-desc">购物清单归属于空间，请在顶部选择空间后开始管理。</p>
        </div>

        <!-- Error state -->
        <div v-else-if="loadError" class="error-state">
          <AlertCircle :size="48" class="error-icon" />
          <p class="empty-title">数据加载失败</p>
          <p class="empty-desc">无法加载购物清单，请检查网络后重试。</p>
          <el-button type="primary" size="small" @click="loadLists">
            <RefreshCw :size="14" />
            重新加载
          </el-button>
        </div>

        <!-- Table + empty -->
        <template v-else>
          <div class="table-scroll">
          <el-table :data="shoppingLists" v-loading="loading" stripe style="width: 100%">
            <el-table-column label="清单名称">
              <template #default="{ row }">
                <el-button link type="primary" @click="openListDetail(row)">{{ row.name }}</el-button>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'completed' ? 'success' : 'info'" size="small">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="预算" width="120">
              <template #default="{ row }">
                {{ row.estimatedBudget ? '¥' + row.estimatedBudget.toFixed(2) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createdAt" width="180" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" text @click="openEditListDialog(row)">编辑</el-button>
                <el-button size="small" text type="danger" @click="handleDeleteList(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          </div>
          <div v-if="!loading && shoppingLists.length === 0" class="empty-state">
            <ShoppingCart :size="48" class="empty-icon" />
            <p class="empty-title">暂无购物清单</p>
            <p class="empty-desc">点击「新建清单」或使用 AI 助手创建你的第一个购物计划。</p>
          </div>
        </template>
      </template>

      <!-- List detail with items -->
      <template v-else>
        <div class="detail-header">
          <el-button text @click="backToList">← 返回列表</el-button>
          <h1>{{ activeList.name }}</h1>
        </div>

        <div class="toolbar">
          <el-tag :type="activeList.status === 'completed' ? 'success' : 'info'" size="small">
            {{ getStatusLabel(activeList.status) }}
          </el-tag>
          <span v-if="activeList.estimatedBudget" class="budget-label">
            预算：¥{{ activeList.estimatedBudget.toFixed(2) }}
          </span>
          <div style="flex: 1" />
          <el-button size="small" type="primary" @click="openCreateItemDialog">添加物品</el-button>
        </div>

        <div v-if="activeList.items.length > 0" class="table-scroll">
        <el-table :data="activeList.items" stripe style="width: 100%">
          <el-table-column label="已购" width="60">
            <template #default="{ row }">
              <el-checkbox
                :model-value="row.purchased"
                @change="() => handleTogglePurchased(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="物品名称">
            <template #default="{ row }">
              <span :class="{ 'item-purchased': row.purchased }">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="100">
            <template #default="{ row }">
              {{ row.quantity }}{{ row.unit ? ' ' + row.unit : '' }}
            </template>
          </el-table-column>
          <el-table-column label="预估价格" width="120">
            <template #default="{ row }">
              {{ row.estimatedPrice ? '¥' + row.estimatedPrice.toFixed(2) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" text @click="openEditItemDialog(row)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDeleteItem(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        </div>
        <div v-else class="empty-state">
          <ShoppingCart :size="48" class="empty-icon" />
          <p class="empty-title">清单里还没有物品</p>
          <p class="empty-desc">点击「添加物品」开始加入要采购的东西。</p>
        </div>
      </template>

      <!-- Create/Edit list dialog -->
      <el-dialog v-model="listDialogVisible" :title="editingListId ? '编辑清单' : '新建清单'" width="450px">
        <el-form label-width="80px">
          <el-form-item label="清单名称">
            <el-input v-model="listForm.name" placeholder="请输入清单名称" />
          </el-form-item>
          <el-form-item label="预算">
            <el-input-number v-model="listForm.estimatedBudget" :min="0" :precision="2" :step="50" style="width: 100%" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="listDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleListSubmit">{{ editingListId ? '更新' : '创建' }}</el-button>
        </template>
      </el-dialog>

      <!-- Create/Edit item dialog -->
      <el-dialog v-model="itemDialogVisible" :title="editingItemId ? '编辑物品' : '添加物品'" width="450px">
        <el-form label-width="80px">
          <el-form-item label="物品名称">
            <el-input v-model="itemForm.name" placeholder="请输入物品名称" />
          </el-form-item>
          <el-form-item label="数量">
            <el-input-number v-model="itemForm.quantity" :min="0.01" :precision="2" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="单位">
            <el-input v-model="itemForm.unit" placeholder="如：个、斤、瓶（可选）" />
          </el-form-item>
          <el-form-item label="预估价格">
            <el-input-number v-model="itemForm.estimatedPrice" :min="0" :precision="2" :step="10" style="width: 100%" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="itemDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleItemSubmit">{{ editingItemId ? '更新' : '添加' }}</el-button>
        </template>
      </el-dialog>

      <!-- AI draft review dialog -->
      <el-dialog v-model="aiDraftDialogVisible" title="AI 生成购物清单" width="550px">
        <div v-if="aiDraft" class="ai-draft-content">
          <p v-if="aiDraft.validationMessage" class="ai-draft-warning">
            {{ aiDraft.validationMessage }}
          </p>
          <el-form label-width="80px">
            <el-form-item label="清单名称">
              <el-input v-model="editableDraft.listName" />
            </el-form-item>
          </el-form>

          <div class="draft-items-header">
            <span>购物物品（{{ editableDraft.items.length }} 项）</span>
            <el-button size="small" text type="primary" @click="addDraftItem">
              <Plus :size="14" />
              添加
            </el-button>
          </div>

          <div v-if="editableDraft.items.length === 0" class="draft-empty">
            暂无物品，请手动添加。
          </div>

          <div
            v-for="(item, index) in editableDraft.items"
            :key="index"
            class="draft-item-row"
          >
            <el-input
              v-model="item.name"
              placeholder="物品名称"
              style="flex: 2"
            />
            <el-input-number
              v-model="item.quantity"
              :min="0.01"
              :precision="2"
              :step="1"
              size="default"
              style="flex: 1"
              controls-position="right"
            />
            <el-input
              v-model="item.unit"
              placeholder="单位"
              style="flex: 0.8"
            />
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
.shopping-page {
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

.detail-header {
  margin-bottom: 12px;
}

.detail-header h1 {
  margin: 4px 0 0 0;
}

.budget-label {
  color: var(--color-muted, #888);
  font-size: 14px;
}

.item-purchased {
  text-decoration: line-through;
  color: var(--color-muted, #888);
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

.draft-item-row {
  display: flex;
  gap: 8px;
  align-items: center;
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

  .draft-item-row .el-input,
  .draft-item-row .el-input-number {
    min-width: 0;
  }
}
</style>