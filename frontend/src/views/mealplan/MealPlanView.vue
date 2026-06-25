<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CalendarDays, AlertCircle, RefreshCw, ChevronLeft, ChevronRight, Sparkles, ShoppingCart } from '@lucide/vue'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'
import { listRecipes, type RecipeResponse } from '@/api/recipe'
import {
  listMealPlans,
  createMealPlan,
  updateMealPlan,
  deleteMealPlan,
  type MealPlanResponse,
} from '@/api/mealplan'
import { draftShoppingListFromMealPlan, recommendRecipes, type RecommendedRecipe, type ShoppingDraft } from '@/api/ai'
import { addShoppingItem, createShoppingList } from '@/api/shopping'

const spaceStore = useSpaceStore()
const mealPlans = ref<MealPlanResponse[]>([])
const recipes = ref<RecipeResponse[]>([])
const loading = ref(false)
const loadError = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const mealTypeLabels: Record<string, string> = {
  breakfast: '早餐',
  lunch: '午餐',
  dinner: '晚餐',
  snack: '加餐',
}

const mealTypeOrder = ['breakfast', 'lunch', 'dinner', 'snack']

const recommendations = ref<RecommendedRecipe[]>([])
const recommendLoading = ref(false)
const showRecommendPanel = ref(false)
const shoppingDraft = ref<ShoppingDraft | null>(null)
const shoppingDraftLoading = ref(false)
const shoppingDraftCreating = ref(false)
const showShoppingDraftPanel = ref(false)

const form = ref({
  recipeId: null as number | null,
  plannedDate: '',
  mealType: 'lunch',
  note: '',
})

const weekStart = ref(getMonday(new Date()))

function getMonday(d: Date): Date {
  const date = new Date(d)
  const day = date.getDay()
  const diff = date.getDate() - day + (day === 0 ? -6 : 1)
  date.setDate(diff)
  date.setHours(0, 0, 0, 0)
  return date
}

function formatDateStr(d: Date): string {
  return d.toISOString().split('T')[0]
}

const weekDays = computed(() => {
  const days: Date[] = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(weekStart.value)
    d.setDate(d.getDate() + i)
    days.push(d)
  }
  return days
})

const weekDayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

function getPlansForDayAndType(date: Date, mealType: string): MealPlanResponse[] {
  const dateStr = formatDateStr(date)
  return mealPlans.value.filter(p => p.plannedDate === dateStr && p.mealType === mealType)
}

function prevWeek() {
  const d = new Date(weekStart.value)
  d.setDate(d.getDate() - 7)
  weekStart.value = d
  loadMealPlans()
}

function nextWeek() {
  const d = new Date(weekStart.value)
  d.setDate(d.getDate() + 7)
  weekStart.value = d
  loadMealPlans()
}

function thisWeek() {
  weekStart.value = getMonday(new Date())
  loadMealPlans()
}

const weekRangeLabel = computed(() => {
  const s = weekStart.value
  const e = new Date(s)
  e.setDate(e.getDate() + 6)
  return `${s.getMonth() + 1}/${s.getDate()} - ${e.getMonth() + 1}/${e.getDate()}`
})

onMounted(async () => {
  await spaceStore.fetchSpaces()
  if (spaceStore.currentSpace) {
    await Promise.all([loadMealPlans(), loadRecipes()])
  }
})

async function loadRecipes() {
  if (!spaceStore.currentSpace) return
  try {
    recipes.value = await listRecipes(spaceStore.currentSpace.id)
  } catch {
    recipes.value = []
  }
}

async function loadMealPlans() {
  if (!spaceStore.currentSpace) return
  loading.value = true
  loadError.value = false
  try {
    const start = formatDateStr(weekStart.value)
    const endDate = new Date(weekStart.value)
    endDate.setDate(endDate.getDate() + 6)
    const end = formatDateStr(endDate)
    mealPlans.value = await listMealPlans(spaceStore.currentSpace.id, start, end)
  } catch {
    loadError.value = true
    mealPlans.value = []
  } finally {
    loading.value = false
  }
}

async function handleSpaceChange() {
  await Promise.all([loadMealPlans(), loadRecipes()])
}

function openCreateDialog(date: Date, mealType: string) {
  editingId.value = null
  form.value = {
    recipeId: null,
    plannedDate: formatDateStr(date),
    mealType,
    note: '',
  }
  dialogVisible.value = true
}

function openEditDialog(plan: MealPlanResponse) {
  editingId.value = plan.id
  form.value = {
    recipeId: plan.recipeId,
    plannedDate: plan.plannedDate,
    mealType: plan.mealType,
    note: plan.note || '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!spaceStore.currentSpace) return
  if (!form.value.recipeId) {
    ElMessage.warning('请选择一个菜谱')
    return
  }
  if (!form.value.plannedDate) {
    ElMessage.warning('请选择日期')
    return
  }
  try {
    if (editingId.value) {
      await updateMealPlan(spaceStore.currentSpace.id, editingId.value, {
        recipeId: form.value.recipeId,
        plannedDate: form.value.plannedDate,
        mealType: form.value.mealType,
        note: form.value.note || undefined,
      })
      ElMessage.success('饮食计划已更新')
    } else {
      await createMealPlan(spaceStore.currentSpace.id, {
        recipeId: form.value.recipeId,
        plannedDate: form.value.plannedDate,
        mealType: form.value.mealType,
        note: form.value.note || undefined,
      })
      ElMessage.success('饮食计划已创建')
    }
    dialogVisible.value = false
    await loadMealPlans()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(plan: MealPlanResponse) {
  if (!spaceStore.currentSpace) return
  try {
    await ElMessageBox.confirm('确定删除该饮食计划？', '删除确认', { type: 'warning' })
    await deleteMealPlan(spaceStore.currentSpace.id, plan.id)
    ElMessage.success('已删除')
    await loadMealPlans()
  } catch {
    // cancelled or error
  }
}

function isToday(d: Date): boolean {
  const today = new Date()
  return d.toDateString() === today.toDateString()
}

async function loadRecommendations() {
  if (!spaceStore.currentSpace) return
  recommendLoading.value = true
  showRecommendPanel.value = true
  try {
    const result = await recommendRecipes(spaceStore.currentSpace.id)
    recommendations.value = result.recipes
  } catch {
    recommendations.value = []
    ElMessage.error('获取推荐失败')
  } finally {
    recommendLoading.value = false
  }
}

async function loadShoppingDraft() {
  if (!spaceStore.currentSpace) return
  shoppingDraftLoading.value = true
  showShoppingDraftPanel.value = true
  try {
    const start = formatDateStr(weekStart.value)
    const endDate = new Date(weekStart.value)
    endDate.setDate(endDate.getDate() + 6)
    shoppingDraft.value = await draftShoppingListFromMealPlan(
      spaceStore.currentSpace.id,
      start,
      formatDateStr(endDate),
    )
  } catch {
    shoppingDraft.value = null
    ElMessage.error('生成采购清单失败')
  } finally {
    shoppingDraftLoading.value = false
  }
}

async function createShoppingListFromDraft() {
  if (!spaceStore.currentSpace || !shoppingDraft.value) return
  if (shoppingDraft.value.items.length === 0) {
    ElMessage.warning('当前没有可创建的采购项')
    return
  }
  shoppingDraftCreating.value = true
  try {
    const list = await createShoppingList(spaceStore.currentSpace.id, {
      name: shoppingDraft.value.listName || '饮食计划采购清单',
      estimatedBudget: shoppingDraft.value.estimatedBudget ?? undefined,
    })
    for (const item of shoppingDraft.value.items) {
      await addShoppingItem(spaceStore.currentSpace.id, list.id, {
        name: item.name,
        quantity: item.quantity,
        unit: item.unit ?? undefined,
        estimatedPrice: item.estimatedPrice ?? undefined,
      })
    }
    ElMessage.success('采购清单已创建')
    shoppingDraft.value = null
    showShoppingDraftPanel.value = false
  } catch {
    ElMessage.error('创建采购清单失败')
  } finally {
    shoppingDraftCreating.value = false
  }
}

function useRecommendation(recipe: RecommendedRecipe) {
  // Pre-fill the create dialog with the recommended recipe
  const recipeObj = recipes.value.find(r => r.id === recipe.recipeId)
  if (recipeObj) {
    editingId.value = null
    form.value = {
      recipeId: recipe.recipeId,
      plannedDate: formatDateStr(new Date()),
      mealType: 'lunch',
      note: `AI 推荐：${recipe.reason}`,
    }
    dialogVisible.value = true
  }
}

function getScorePercent(score: number): number {
  return Math.round(score * 100)
}

function getScoreColor(score: number): string {
  if (score >= 0.7) return 'var(--el-color-success, #67c23a)'
  if (score >= 0.3) return 'var(--el-color-warning, #e6a23c)'
  return 'var(--el-color-danger, #f56c6c)'
}
</script>

<template>
  <AppShell>
    <div class="mealplan-page">
      <h1>一周饮食计划</h1>
      <p class="page-desc">规划每周菜谱，合理安排早午晚餐。</p>

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
      </div>

      <!-- No space state -->
      <div v-if="!spaceStore.currentSpace" class="empty-state">
        <CalendarDays :size="48" class="empty-icon" />
        <p class="empty-title">请先选择一个空间</p>
        <p class="empty-desc">饮食计划归属于空间，请在顶部选择空间后开始规划。</p>
      </div>

      <!-- Error state -->
      <div v-else-if="loadError" class="error-state">
        <AlertCircle :size="48" class="error-icon" />
        <p class="empty-title">数据加载失败</p>
        <p class="empty-desc">无法加载饮食计划，请检查网络后重试。</p>
        <el-button type="primary" size="small" @click="loadMealPlans">
          <RefreshCw :size="14" />
          重新加载
        </el-button>
      </div>

      <!-- Calendar view -->
      <template v-else>
        <!-- AI Recommend button -->
        <div class="recommend-bar">
          <el-button type="primary" :loading="recommendLoading" @click="loadRecommendations">
            <Sparkles :size="14" style="margin-right: 4px" />
            AI 菜谱推荐
          </el-button>
          <el-button :loading="shoppingDraftLoading" @click="loadShoppingDraft">
            <ShoppingCart :size="14" style="margin-right: 4px" />
            生成采购清单
          </el-button>
          <span class="recommend-hint">根据库存推荐可制作的菜谱</span>
        </div>

        <!-- Meal-plan shopping draft panel -->
        <div v-if="showShoppingDraftPanel" class="shopping-draft-panel">
          <div v-if="shoppingDraftLoading" class="recommend-loading">
            <el-icon class="is-loading"><RefreshCw :size="16" /></el-icon>
            正在分析本周饮食计划和库存…
          </div>
          <template v-else-if="shoppingDraft">
            <div class="shopping-draft-header">
              <div>
                <div class="shopping-draft-title">{{ shoppingDraft.listName }}</div>
                <div v-if="shoppingDraft.validationMessage" class="shopping-draft-message">
                  {{ shoppingDraft.validationMessage }}
                </div>
              </div>
              <el-button
                type="primary"
                :disabled="shoppingDraft.items.length === 0"
                :loading="shoppingDraftCreating"
                @click="createShoppingListFromDraft"
              >
                创建清单
              </el-button>
            </div>
            <div v-if="shoppingDraft.items.length === 0" class="recommend-empty">
              暂无需要采购的食材。
            </div>
            <div v-else class="shopping-draft-items">
              <div v-for="item in shoppingDraft.items" :key="`${item.name}-${item.unit || ''}`" class="shopping-draft-item">
                <span class="shopping-draft-name">{{ item.name }}</span>
                <span class="shopping-draft-quantity">
                  {{ item.quantity }}{{ item.unit || '' }}
                </span>
              </div>
            </div>
          </template>
          <div v-else class="recommend-empty">
            暂未生成采购草稿。
          </div>
        </div>

        <!-- Recommendation panel -->
        <div v-if="showRecommendPanel" class="recommend-panel">
          <div v-if="recommendLoading" class="recommend-loading">
            <el-icon class="is-loading"><RefreshCw :size="16" /></el-icon>
            正在分析库存和菜谱…
          </div>
          <div v-else-if="recommendations.length === 0" class="recommend-empty">
            暂无推荐。请先添加库存物品和菜谱。
          </div>
          <div v-else class="recommend-list">
            <div
              v-for="rec in recommendations"
              :key="rec.recipeId"
              class="recommend-item"
              @click="useRecommendation(rec)"
            >
              <div class="recommend-item-header">
                <span class="recommend-name">{{ rec.recipeName }}</span>
                <span class="recommend-score" :style="{ color: getScoreColor(rec.matchScore) }">
                  {{ getScorePercent(rec.matchScore) }}% 匹配
                </span>
              </div>
              <div class="recommend-bar-visual">
                <div
                  class="recommend-bar-fill"
                  :style="{ width: getScorePercent(rec.matchScore) + '%', background: getScoreColor(rec.matchScore) }"
                />
              </div>
              <div class="recommend-detail">
                <span v-if="rec.matchedIngredients.length > 0" class="recommend-matched">
                  ✓ {{ rec.matchedIngredients.join('、') }}
                </span>
                <span v-if="rec.missingIngredients.length > 0" class="recommend-missing">
                  ✗ {{ rec.missingIngredients.join('、') }}
                </span>
              </div>
              <div class="recommend-reason">{{ rec.reason }}</div>
            </div>
          </div>
        </div>

        <div class="week-nav">
          <el-button text @click="prevWeek"><ChevronLeft :size="18" /></el-button>
          <el-button text @click="thisWeek">本周</el-button>
          <span class="week-range">{{ weekRangeLabel }}</span>
          <el-button text @click="nextWeek"><ChevronRight :size="18" /></el-button>
        </div>

        <div v-loading="loading" class="calendar-grid">
          <div class="calendar-header">
            <div class="cell-label"></div>
            <div
              v-for="(day, idx) in weekDays"
              :key="idx"
              class="cell-header"
              :class="{ today: isToday(day) }"
            >
              <div class="day-name">{{ weekDayLabels[idx] }}</div>
              <div class="day-date">{{ day.getMonth() + 1 }}/{{ day.getDate() }}</div>
            </div>
          </div>

          <div v-for="mealType in mealTypeOrder" :key="mealType" class="calendar-row">
            <div class="cell-label">{{ mealTypeLabels[mealType] }}</div>
            <div
              v-for="(day, idx) in weekDays"
              :key="idx"
              class="cell-body"
              :class="{ today: isToday(day) }"
            >
              <div
                v-for="plan in getPlansForDayAndType(day, mealType)"
                :key="plan.id"
                class="plan-chip"
                @click="openEditDialog(plan)"
              >
                <span class="plan-recipe-name">{{ plan.recipeName }}</span>
                <span v-if="plan.note" class="plan-note">{{ plan.note }}</span>
              </div>
              <el-button
                class="add-btn"
                text
                size="small"
                @click="openCreateDialog(day, mealType)"
              >
                + 添加
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="!loading && mealPlans.length === 0" class="empty-state inline-empty">
          <CalendarDays :size="36" class="empty-icon" />
          <p class="empty-title">本周暂无饮食计划</p>
          <p class="empty-desc">点击「+ 添加」按钮，为每一天安排菜谱。</p>
        </div>
      </template>

      <!-- Create/Edit dialog -->
      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑饮食计划' : '添加饮食计划'" width="520px">
        <el-form label-width="80px">
          <el-form-item label="菜谱">
            <el-select
              v-model="form.recipeId"
              placeholder="请选择菜谱"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="r in recipes"
                :key="r.id"
                :label="r.name"
                :value="r.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="日期">
            <el-date-picker
              v-model="form.plannedDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="餐次">
            <el-select v-model="form.mealType" style="width: 100%">
              <el-option label="早餐" value="breakfast" />
              <el-option label="午餐" value="lunch" />
              <el-option label="晚餐" value="dinner" />
              <el-option label="加餐" value="snack" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.note" placeholder="备注（可选）" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button v-if="editingId" type="danger" text @click="handleDelete(mealPlans.find(p => p.id === editingId)!)">删除</el-button>
          <div style="flex: 1" />
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">{{ editingId ? '更新' : '创建' }}</el-button>
        </template>
      </el-dialog>
    </div>
  </AppShell>
</template>

<style scoped>
.mealplan-page {
  max-width: 1100px;
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

.week-nav {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 16px;
}

.week-range {
  font-size: 15px;
  font-weight: 600;
  min-width: 140px;
  text-align: center;
}

.calendar-grid {
  border: 1px solid var(--el-border-color-light, #e4e7ed);
  border-radius: 8px;
  overflow: hidden;
}

.calendar-header,
.calendar-row {
  display: grid;
  grid-template-columns: 64px repeat(7, 1fr);
}

.cell-label {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text, #333);
  background: var(--el-fill-color-light, #f5f7fa);
  border-bottom: 1px solid var(--el-border-color-light, #e4e7ed);
  border-right: 1px solid var(--el-border-color-light, #e4e7ed);
  padding: 8px 4px;
}

.cell-header {
  text-align: center;
  padding: 8px 4px;
  background: var(--el-fill-color-light, #f5f7fa);
  border-bottom: 1px solid var(--el-border-color-light, #e4e7ed);
  border-right: 1px solid var(--el-border-color-light, #e4e7ed);
}

.cell-header:last-child {
  border-right: none;
}

.cell-header.today {
  background: var(--el-color-primary-light-9, #ecf5ff);
}

.day-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text, #333);
}

.day-date {
  font-size: 12px;
  color: var(--color-muted, #888);
}

.cell-body {
  min-height: 72px;
  padding: 6px;
  border-bottom: 1px solid var(--el-border-color-light, #e4e7ed);
  border-right: 1px solid var(--el-border-color-light, #e4e7ed);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cell-body:last-child {
  border-right: none;
}

.calendar-row:last-child .cell-body {
  border-bottom: none;
}

.cell-body.today {
  background: var(--el-color-primary-light-9, #ecf5ff);
}

.plan-chip {
  background: var(--el-color-primary-light-7, #d9ecff);
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 12px;
  line-height: 1.4;
  transition: background 0.2s;
}

.plan-chip:hover {
  background: var(--el-color-primary-light-5, #a0cfff);
}

.plan-recipe-name {
  font-weight: 600;
  color: var(--el-color-primary, #409eff);
  display: block;
}

.plan-note {
  color: var(--color-muted, #888);
  font-size: 11px;
  display: block;
}

.add-btn {
  font-size: 12px;
  color: var(--color-muted, #aaa);
  padding: 2px 4px;
  align-self: flex-start;
}

.add-btn:hover {
  color: var(--el-color-primary, #409eff);
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

.inline-empty {
  padding: 32px 24px;
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

.recommend-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.recommend-hint {
  font-size: 13px;
  color: var(--color-muted, #888);
}

.recommend-panel {
  background: var(--el-fill-color-blank, #fff);
  border: 1px solid var(--el-border-color-light, #e4e7ed);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.shopping-draft-panel {
  background: var(--el-fill-color-blank, #fff);
  border: 1px solid var(--el-border-color-light, #e4e7ed);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.shopping-draft-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.shopping-draft-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text, #333);
}

.shopping-draft-message {
  margin-top: 4px;
  color: var(--color-muted, #888);
  font-size: 13px;
  line-height: 1.5;
}

.shopping-draft-items {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 8px;
}

.shopping-draft-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 8px 10px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  border-radius: 6px;
  background: var(--el-fill-color-light, #f5f7fa);
  font-size: 13px;
}

.shopping-draft-name {
  color: var(--color-text, #333);
  overflow-wrap: anywhere;
}

.shopping-draft-quantity {
  color: var(--el-color-primary, #409eff);
  font-weight: 600;
  white-space: nowrap;
}

.recommend-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-muted, #888);
  font-size: 14px;
  padding: 12px 0;
}

.recommend-empty {
  color: var(--color-muted, #888);
  font-size: 14px;
  padding: 12px 0;
  text-align: center;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-item {
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  border-radius: 6px;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.recommend-item:hover {
  border-color: var(--el-color-primary, #409eff);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.recommend-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.recommend-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--color-text, #333);
}

.recommend-score {
  font-size: 13px;
  font-weight: 600;
}

.recommend-bar-visual {
  height: 4px;
  background: var(--el-fill-color-light, #f5f7fa);
  border-radius: 2px;
  margin-bottom: 8px;
  overflow: hidden;
}

.recommend-bar-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s;
}

.recommend-detail {
  font-size: 12px;
  line-height: 1.6;
  margin-bottom: 4px;
}

.recommend-matched {
  color: var(--el-color-success, #67c23a);
  display: block;
}

.recommend-missing {
  color: var(--el-color-danger, #f56c6c);
  display: block;
}

.recommend-reason {
  font-size: 12px;
  color: var(--color-muted, #888);
}
</style>
