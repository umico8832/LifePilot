<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChefHat, AlertCircle, RefreshCw } from '@lucide/vue'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'
import {
  listRecipes,
  createRecipe,
  updateRecipe,
  deleteRecipe,
  type RecipeResponse,
} from '@/api/recipe'

const spaceStore = useSpaceStore()
const recipes = ref<RecipeResponse[]>([])
const loading = ref(false)
const loadError = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = ref({
  name: '',
  description: '',
  ingredientsJson: '',
  stepsJson: '',
})

onMounted(async () => {
  await spaceStore.fetchSpaces()
  if (spaceStore.currentSpace) {
    await loadRecipes()
  }
})

async function loadRecipes() {
  if (!spaceStore.currentSpace) return
  loading.value = true
  loadError.value = false
  try {
    recipes.value = await listRecipes(spaceStore.currentSpace.id)
  } catch {
    loadError.value = true
    recipes.value = []
  } finally {
    loading.value = false
  }
}

async function handleSpaceChange() {
  await loadRecipes()
}

function openCreateDialog() {
  editingId.value = null
  form.value = { name: '', description: '', ingredientsJson: '', stepsJson: '' }
  dialogVisible.value = true
}

function openEditDialog(recipe: RecipeResponse) {
  editingId.value = recipe.id
  form.value = {
    name: recipe.name,
    description: recipe.description || '',
    ingredientsJson: recipe.ingredientsJson || '',
    stepsJson: recipe.stepsJson || '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!spaceStore.currentSpace) return
  if (!form.value.name.trim()) {
    ElMessage.warning('菜谱名称不能为空')
    return
  }
  try {
    const payload = {
      name: form.value.name,
      description: form.value.description || undefined,
      ingredientsJson: form.value.ingredientsJson || undefined,
      stepsJson: form.value.stepsJson || undefined,
    }
    if (editingId.value) {
      await updateRecipe(spaceStore.currentSpace.id, editingId.value, payload)
      ElMessage.success('菜谱已更新')
    } else {
      await createRecipe(spaceStore.currentSpace.id, payload)
      ElMessage.success('菜谱已创建')
    }
    dialogVisible.value = false
    await loadRecipes()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(recipe: RecipeResponse) {
  if (!spaceStore.currentSpace) return
  try {
    await ElMessageBox.confirm('确定删除该菜谱？', '删除确认', { type: 'warning' })
    await deleteRecipe(spaceStore.currentSpace.id, recipe.id)
    ElMessage.success('已删除')
    await loadRecipes()
  } catch {
    // cancelled or error
  }
}

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function tryParseJson(str: string | null): unknown[] | null {
  if (!str) return null
  try {
    const parsed = JSON.parse(str)
    return Array.isArray(parsed) ? parsed : null
  } catch {
    return null
  }
}

function formatIngredients(str: string | null): string {
  const items = tryParseJson(str)
  if (!items) return str || '-'
  return items.map((i: any) => i.name || i).join('、')
}
</script>

<template>
  <AppShell>
    <div class="recipe-page">
      <h1>饮食计划</h1>
      <p class="page-desc">管理菜谱和烹饪步骤，记录食材与做法。</p>

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
        <div style="flex: 1" />
        <el-button type="primary" @click="openCreateDialog">添加菜谱</el-button>
      </div>

      <!-- No space state -->
      <div v-if="!spaceStore.currentSpace" class="empty-state">
        <ChefHat :size="48" class="empty-icon" />
        <p class="empty-title">请先选择一个空间</p>
        <p class="empty-desc">菜谱数据归属于空间，请在顶部选择空间后开始管理。</p>
      </div>

      <!-- Error state -->
      <div v-else-if="loadError" class="error-state">
        <AlertCircle :size="48" class="error-icon" />
        <p class="empty-title">数据加载失败</p>
        <p class="empty-desc">无法加载菜谱数据，请检查网络后重试。</p>
        <el-button type="primary" size="small" @click="loadRecipes">
          <RefreshCw :size="14" />
          重新加载
        </el-button>
      </div>

      <!-- Table + empty -->
      <template v-else>
        <div class="table-scroll">
        <el-table :data="recipes" v-loading="loading" stripe style="width: 100%">
          <el-table-column label="名称" prop="name" min-width="160" />
          <el-table-column label="描述" min-width="220">
            <template #default="{ row }">
              <span class="desc-text">{{ row.description || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="食材" min-width="160">
            <template #default="{ row }">
              <span>{{ formatIngredients(row.ingredientsJson) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button size="small" text @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        </div>

        <!-- Empty state -->
        <div v-if="!loading && recipes.length === 0" class="empty-state">
          <ChefHat :size="48" class="empty-icon" />
          <p class="empty-title">暂无菜谱</p>
          <p class="empty-desc">点击「添加菜谱」开始记录你的拿手好菜。</p>
        </div>
      </template>

      <!-- Create/Edit dialog -->
      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑菜谱' : '添加菜谱'" width="560px">
        <el-form label-width="80px">
          <el-form-item label="菜谱名称">
            <el-input v-model="form.name" placeholder="请输入菜谱名称" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="form.description" type="textarea" :rows="2" placeholder="简单描述这道菜（可选）" />
          </el-form-item>
          <el-form-item label="食材">
            <el-input
              v-model="form.ingredientsJson"
              type="textarea"
              :rows="4"
              placeholder='JSON 格式，如：[{"name":"番茄","amount":"3个"},{"name":"鸡蛋","amount":"2个"}]'
            />
          </el-form-item>
          <el-form-item label="步骤">
            <el-input
              v-model="form.stepsJson"
              type="textarea"
              :rows="4"
              placeholder='JSON 格式，如：["切番茄","打蛋","炒蛋","加入番茄翻炒"]'
            />
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
.recipe-page {
  max-width: 960px;
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

.desc-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--color-muted, #888);
  font-size: 13px;
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