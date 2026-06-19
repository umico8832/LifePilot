<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CalendarDays, AlertCircle, RefreshCw, ChevronLeft, ChevronRight } from '@lucide/vue'

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
</style>