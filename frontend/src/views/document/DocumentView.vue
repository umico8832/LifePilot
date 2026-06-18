<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FileText, AlertCircle, RefreshCw, AlertTriangle } from '@lucide/vue'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'
import {
  listDocuments,
  createDocument,
  updateDocument,
  deleteDocument,
  type DocumentResponse,
} from '@/api/document'

const spaceStore = useSpaceStore()
const documents = ref<DocumentResponse[]>([])
const loading = ref(false)
const loadError = ref(false)
const filterType = ref<string>('')
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = ref({
  title: '',
  type: 'other',
  issuer: '',
  documentDate: '',
  expireAt: '',
  storageLocation: '',
  metadataJson: '',
})

const typeOptions = [
  { label: '发票', value: 'invoice' },
  { label: '收据', value: 'receipt' },
  { label: '保修卡', value: 'warranty' },
  { label: '合同', value: 'contract' },
  { label: '说明书', value: 'manual' },
  { label: '证件', value: 'certificate' },
  { label: '其他', value: 'other' },
]

const typeLabel: Record<string, string> = {
  invoice: '发票',
  receipt: '收据',
  warranty: '保修卡',
  contract: '合同',
  manual: '说明书',
  certificate: '证件',
  other: '其他',
}

const typeTagType: Record<string, string> = {
  invoice: '',
  receipt: 'success',
  warranty: 'warning',
  contract: 'danger',
  manual: 'info',
  certificate: 'warning',
  other: 'info',
}

onMounted(async () => {
  await spaceStore.fetchSpaces()
  if (spaceStore.currentSpace) {
    await loadDocuments()
  }
})

async function loadDocuments() {
  if (!spaceStore.currentSpace) return
  loading.value = true
  loadError.value = false
  try {
    documents.value = await listDocuments(spaceStore.currentSpace.id, filterType.value || undefined)
  } catch {
    loadError.value = true
    documents.value = []
  } finally {
    loading.value = false
  }
}

async function handleFilterChange(val: string) {
  filterType.value = val
  await loadDocuments()
}

async function handleSpaceChange() {
  filterType.value = ''
  await loadDocuments()
}

function openCreateDialog() {
  editingId.value = null
  form.value = { title: '', type: 'other', issuer: '', documentDate: '', expireAt: '', storageLocation: '', metadataJson: '' }
  dialogVisible.value = true
}

function openEditDialog(doc: DocumentResponse) {
  editingId.value = doc.id
  form.value = {
    title: doc.title,
    type: doc.type,
    issuer: doc.issuer || '',
    documentDate: doc.documentDate || '',
    expireAt: doc.expireAt || '',
    storageLocation: doc.storageLocation || '',
    metadataJson: doc.metadataJson || '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!spaceStore.currentSpace) return
  if (!form.value.title.trim()) {
    ElMessage.warning('文档标题不能为空')
    return
  }
  try {
    const payload = {
      title: form.value.title,
      type: form.value.type,
      issuer: form.value.issuer || undefined,
      documentDate: form.value.documentDate || undefined,
      expireAt: form.value.expireAt || undefined,
      storageLocation: form.value.storageLocation || undefined,
      metadataJson: form.value.metadataJson || undefined,
    }
    if (editingId.value) {
      await updateDocument(spaceStore.currentSpace.id, editingId.value, payload)
      ElMessage.success('文档已更新')
    } else {
      await createDocument(spaceStore.currentSpace.id, payload)
      ElMessage.success('文档已创建')
    }
    dialogVisible.value = false
    await loadDocuments()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(doc: DocumentResponse) {
  if (!spaceStore.currentSpace) return
  try {
    await ElMessageBox.confirm('确定删除该文档记录？', '删除确认', { type: 'warning' })
    await deleteDocument(spaceStore.currentSpace.id, doc.id)
    ElMessage.success('已删除')
    await loadDocuments()
  } catch {
    // cancelled or error
  }
}

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const expiringCount = computed(() => documents.value.filter(d => d.expiringSoon).length)
</script>

<template>
  <AppShell>
    <div class="document-page">
      <h1>票据与文件</h1>
      <p class="page-desc">管理发票、收据、保修卡、合同、说明书和证件索引。</p>

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
          v-model="filterType"
          placeholder="筛选类型"
          size="default"
          style="width: 130px"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option
            v-for="opt in typeOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <div style="flex: 1" />
        <el-button type="primary" @click="openCreateDialog">添加文档</el-button>
      </div>

      <!-- Summary bar -->
      <div v-if="spaceStore.currentSpace && documents.length > 0" class="summary-bar">
        <span class="summary-item">共 {{ documents.length }} 项</span>
        <span v-if="expiringCount > 0" class="summary-item summary-expiring">
          <AlertTriangle :size="12" /> 即将过期 {{ expiringCount }}
        </span>
      </div>

      <!-- No space state -->
      <div v-if="!spaceStore.currentSpace" class="empty-state">
        <FileText :size="48" class="empty-icon" />
        <p class="empty-title">请先选择一个空间</p>
        <p class="empty-desc">文档数据归属于空间，请在顶部选择空间后开始管理。</p>
      </div>

      <!-- Error state -->
      <div v-else-if="loadError" class="error-state">
        <AlertCircle :size="48" class="error-icon" />
        <p class="empty-title">数据加载失败</p>
        <p class="empty-desc">无法加载文档数据，请检查网络后重试。</p>
        <el-button type="primary" size="small" @click="loadDocuments">
          <RefreshCw :size="14" />
          重新加载
        </el-button>
      </div>

      <!-- Table + empty -->
      <template v-else>
        <div class="table-scroll">
        <el-table :data="documents" v-loading="loading" stripe style="width: 100%">
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="(typeTagType[row.type] as any)" size="small">
                {{ typeLabel[row.type] || row.type }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标题" prop="title" min-width="200" />
          <el-table-column label="签发方" prop="issuer" width="140">
            <template #default="{ row }">
              {{ row.issuer || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="文档日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.documentDate) }}
            </template>
          </el-table-column>
          <el-table-column label="到期日" width="120">
            <template #default="{ row }">
              <span :class="{ 'expiring-text': row.expiringSoon }">
                {{ formatDate(row.expireAt) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="存放位置" width="140">
            <template #default="{ row }">
              {{ row.storageLocation || '-' }}
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
        <div v-if="!loading && documents.length === 0" class="empty-state">
          <FileText :size="48" class="empty-icon" />
          <p class="empty-title">{{ filterType ? '没有匹配的文档' : '暂无文档记录' }}</p>
          <p class="empty-desc">{{ filterType ? '当前筛选条件下没有文档。' : '点击「添加文档」开始管理你的票据和文件。' }}</p>
        </div>
      </template>

      <!-- Create/Edit dialog -->
      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑文档' : '添加文档'" width="560px">
        <el-form label-width="100px">
          <el-form-item label="文档标题">
            <el-input v-model="form.title" placeholder="请输入文档标题" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="form.type" style="width: 100%">
              <el-option
                v-for="opt in typeOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="签发方">
            <el-input v-model="form.issuer" placeholder="签发方/来源（可选）" />
          </el-form-item>
          <el-form-item label="文档日期">
            <el-date-picker
              v-model="form.documentDate"
              type="date"
              placeholder="选择日期（可选）"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="到期日期">
            <el-date-picker
              v-model="form.expireAt"
              type="date"
              placeholder="选择到期日（可选）"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="存放位置">
            <el-input v-model="form.storageLocation" placeholder="存放位置（可选）" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.metadataJson" type="textarea" :rows="2" placeholder="备注信息（可选）" />
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
.document-page {
  max-width: 1000px;
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
  display: flex;
  align-items: center;
  gap: 4px;
}

.summary-expiring {
  color: var(--el-color-warning, #e6a23c);
  font-weight: bold;
}

.expiring-text {
  color: var(--el-color-warning, #e6a23c);
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