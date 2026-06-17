<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Package, AlertCircle, RefreshCw } from '@lucide/vue'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'
import {
  listInventoryItems,
  createInventoryItem,
  updateInventoryItem,
  deleteInventoryItem,
  listInventoryAlerts,
  type InventoryItemResponse,
} from '@/api/inventory'

const spaceStore = useSpaceStore()
const items = ref<InventoryItemResponse[]>([])
const alertItems = ref<InventoryItemResponse[]>([])
const loading = ref(false)
const loadError = ref(false)
const showAlerts = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = ref({
  name: '',
  category: '',
  quantity: 0,
  unit: '',
  location: '',
  lowStockThreshold: 0,
})

onMounted(async () => {
  await spaceStore.fetchSpaces()
  if (spaceStore.currentSpace) {
    await loadItems()
  }
})

async function loadItems() {
  if (!spaceStore.currentSpace) return
  loading.value = true
  loadError.value = false
  try {
    items.value = await listInventoryItems(spaceStore.currentSpace.id)
  } catch {
    loadError.value = true
    items.value = []
  } finally {
    loading.value = false
  }
}

async function loadAlerts() {
  if (!spaceStore.currentSpace) return
  loading.value = true
  try {
    alertItems.value = await listInventoryAlerts(spaceStore.currentSpace.id)
    showAlerts.value = true
  } finally {
    loading.value = false
  }
}

function hideAlerts() {
  showAlerts.value = false
}

async function handleSpaceChange() {
  showAlerts.value = false
  await loadItems()
}

function openCreateDialog() {
  editingId.value = null
  form.value = { name: '', category: '', quantity: 0, unit: '', location: '', lowStockThreshold: 0 }
  dialogVisible.value = true
}

function openEditDialog(item: InventoryItemResponse) {
  editingId.value = item.id
  form.value = {
    name: item.name,
    category: item.category || '',
    quantity: item.quantity,
    unit: item.unit || '',
    location: item.location || '',
    lowStockThreshold: item.lowStockThreshold || 0,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!spaceStore.currentSpace) return
  if (!form.value.name.trim()) {
    ElMessage.warning('物品名称不能为空')
    return
  }
  try {
    const payload = {
      name: form.value.name,
      category: form.value.category || undefined,
      quantity: form.value.quantity,
      unit: form.value.unit || undefined,
      location: form.value.location || undefined,
      lowStockThreshold: form.value.lowStockThreshold > 0 ? form.value.lowStockThreshold : undefined,
    }
    if (editingId.value) {
      await updateInventoryItem(spaceStore.currentSpace.id, editingId.value, payload)
      ElMessage.success('库存物品已更新')
    } else {
      await createInventoryItem(spaceStore.currentSpace.id, payload)
      ElMessage.success('库存物品已创建')
    }
    dialogVisible.value = false
    await loadItems()
    if (showAlerts.value) {
      await loadAlerts()
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(item: InventoryItemResponse) {
  if (!spaceStore.currentSpace) return
  try {
    await ElMessageBox.confirm('确定删除该库存物品？', '删除确认', { type: 'warning' })
    await deleteInventoryItem(spaceStore.currentSpace.id, item.id)
    ElMessage.success('已删除')
    await loadItems()
    if (showAlerts.value) {
      await loadAlerts()
    }
  } catch {
    // cancelled or error
  }
}

const displayItems = computed(() => showAlerts.value ? alertItems.value : items.value)
</script>

<template>
  <AppShell>
    <div class="inventory-page">
      <h1>家庭库存</h1>
      <p class="page-desc">管理家庭物品库存，设置低库存提醒。</p>

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
        <el-button v-if="showAlerts" @click="hideAlerts">显示全部</el-button>
        <el-button v-else type="warning" @click="loadAlerts">低库存提醒</el-button>
        <div style="flex: 1" />
        <el-button type="primary" @click="openCreateDialog">添加物品</el-button>
      </div>

      <!-- No space state -->
      <div v-if="!spaceStore.currentSpace" class="empty-state">
        <Package :size="48" class="empty-icon" />
        <p class="empty-title">请先选择一个空间</p>
        <p class="empty-desc">库存数据归属于空间，请在顶部选择空间后开始管理。</p>
      </div>

      <!-- Error state -->
      <div v-else-if="loadError" class="error-state">
        <AlertCircle :size="48" class="error-icon" />
        <p class="empty-title">数据加载失败</p>
        <p class="empty-desc">无法加载库存数据，请检查网络后重试。</p>
        <el-button type="primary" size="small" @click="loadItems">
          <RefreshCw :size="14" />
          重新加载
        </el-button>
      </div>

      <!-- Table + empty -->
      <template v-else>
        <div class="table-scroll">
        <el-table :data="displayItems" v-loading="loading" stripe style="width: 100%">
          <el-table-column label="物品名称" prop="name" min-width="150" />
          <el-table-column label="分类" width="120">
            <template #default="{ row }">
              {{ row.category || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template #default="{ row }">
              <span :class="{ 'low-stock': row.lowStock }">
                {{ row.quantity }}{{ row.unit ? ' ' + row.unit : '' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="存放位置" width="140">
            <template #default="{ row }">
              {{ row.location || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="低库存阈值" width="120">
            <template #default="{ row }">
              {{ row.lowStockThreshold ?? '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.lowStock" type="danger" size="small">低库存</el-tag>
              <el-tag v-else type="success" size="small">正常</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" text @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        </div>

        <!-- Empty state -->
        <div v-if="!loading && displayItems.length === 0" class="empty-state">
          <Package :size="48" class="empty-icon" />
          <p class="empty-title">{{ showAlerts ? '没有低库存物品' : '暂无库存物品' }}</p>
          <p class="empty-desc">{{ showAlerts ? '所有物品库存充足。' : '点击「添加物品」开始管理你的家庭库存。' }}</p>
        </div>
      </template>

      <!-- Create/Edit dialog -->
      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑库存物品' : '添加库存物品'" width="500px">
        <el-form label-width="100px">
          <el-form-item label="物品名称">
            <el-input v-model="form.name" placeholder="请输入物品名称" />
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="form.category" placeholder="如：食品、清洁用品（可选）" />
          </el-form-item>
          <el-form-item label="数量">
            <el-input-number v-model="form.quantity" :min="0" :precision="2" :step="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="单位">
            <el-input v-model="form.unit" placeholder="如：个、斤、瓶（可选）" />
          </el-form-item>
          <el-form-item label="存放位置">
            <el-input v-model="form.location" placeholder="如：冰箱、柜子（可选）" />
          </el-form-item>
          <el-form-item label="低库存阈值">
            <el-input-number v-model="form.lowStockThreshold" :min="0" :precision="2" :step="1" style="width: 100%" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">{{ editingId ? '更新' : '创建' }}</el-button>
        </template>
      </el-dialog>
    </div>
  </AppShell>
</template>

<style scoped>
.inventory-page {
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

.low-stock {
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
</style>
