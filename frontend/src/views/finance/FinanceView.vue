<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'
import {
  listTransactions,
  createTransaction,
  updateTransaction,
  deleteTransaction,
  type TransactionResponse,
} from '@/api/transaction'

const spaceStore = useSpaceStore()
const transactions = ref<TransactionResponse[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = ref({
  amount: 0,
  type: 'expense',
  merchant: '',
  note: '',
})

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

onMounted(async () => {
  await spaceStore.fetchSpaces()
  if (spaceStore.currentSpace) {
    await loadTransactions()
  }
})

async function loadTransactions() {
  if (!spaceStore.currentSpace) return
  loading.value = true
  try {
    transactions.value = await listTransactions(spaceStore.currentSpace.id)
  } finally {
    loading.value = false
  }
}

async function handleSpaceChange() {
  await loadTransactions()
}

function openCreateDialog() {
  editingId.value = null
  form.value = { amount: 0, type: 'expense', merchant: '', note: '' }
  dialogVisible.value = true
}

function openEditDialog(tx: TransactionResponse) {
  editingId.value = tx.id
  form.value = {
    amount: tx.amount,
    type: tx.type,
    merchant: tx.merchant || '',
    note: tx.note || '',
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
    if (editingId.value) {
      await updateTransaction(spaceStore.currentSpace.id, editingId.value, form.value)
      ElMessage.success('记录已更新')
    } else {
      await createTransaction(spaceStore.currentSpace.id, form.value)
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
        <el-button type="primary" @click="openCreateDialog">记一笔</el-button>
      </div>

      <!-- Summary -->
      <div class="summary-band">
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

      <!-- Transaction table -->
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
</style>