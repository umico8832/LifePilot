<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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

const spaceStore = useSpaceStore()
const shoppingLists = ref<ShoppingListResponse[]>([])
const activeList = ref<ShoppingListResponse | null>(null)
const loading = ref(false)
const listDialogVisible = ref(false)
const itemDialogVisible = ref(false)
const editingListId = ref<number | null>(null)
const editingItemId = ref<number | null>(null)

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
  try {
    shoppingLists.value = await listShoppingLists(spaceStore.currentSpace.id)
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
</style>