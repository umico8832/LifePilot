<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Home, AlertCircle, RefreshCw } from '@lucide/vue'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'
import { useAuthStore } from '@/stores/auth'
import type { MemberResponse } from '@/api/space'

const spaceStore = useSpaceStore()
const authStore = useAuthStore()
const spacesLoading = ref(false)
const spacesError = ref(false)
const newSpaceName = ref('')
const newSpaceType = ref('family')
const inviteEmail = ref('')
const inviteRole = ref('member')
const renameValue = ref('')
const renameDialogVisible = ref(false)
const inviteDialogVisible = ref(false)

const personalSpaces = computed(() => spaceStore.spaces.filter((s) => s.type === 'personal'))
const familySpaces = computed(() => spaceStore.spaces.filter((s) => s.type !== 'personal'))
const canManageMembers = computed(() => isManagerRole(spaceStore.currentSpace?.memberRole))
const activeManagerCount = computed(() => spaceStore.members.filter((m) => isManagerRole(m.role) && m.status === 'active').length)

const roleOptions = [
  { label: '所有者', value: 'owner' },
  { label: '管理员', value: 'admin' },
  { label: '成员', value: 'member' },
  { label: '只读', value: 'viewer' },
]

onMounted(async () => {
  await loadSpaces()
})

async function loadSpaces() {
  spacesLoading.value = true
  spacesError.value = false
  try {
    await spaceStore.fetchSpaces()
  } catch {
    spacesError.value = true
  } finally {
    spacesLoading.value = false
  }
}

async function handleCreateSpace() {
  if (!newSpaceName.value.trim()) return
  try {
    await spaceStore.createNewSpace(newSpaceName.value.trim(), newSpaceType.value)
    newSpaceName.value = ''
    ElMessage.success('空间创建成功')
  } catch {
    ElMessage.error('创建空间失败')
  }
}

async function handleSelectSpace(spaceId: number) {
  await spaceStore.fetchSpace(spaceId)
  await spaceStore.fetchMembers(spaceId)
}

async function handleRename() {
  if (!spaceStore.currentSpace || !renameValue.value.trim()) return
  try {
    await spaceStore.renameSpace(spaceStore.currentSpace.id, renameValue.value.trim())
    renameDialogVisible.value = false
    ElMessage.success('空间名称已更新')
  } catch {
    ElMessage.error('更新失败')
  }
}

async function handleInvite() {
  if (!spaceStore.currentSpace || !inviteEmail.value.trim()) return
  try {
    await spaceStore.inviteMember(spaceStore.currentSpace.id, inviteEmail.value.trim(), inviteRole.value)
    inviteEmail.value = ''
    inviteDialogVisible.value = false
    ElMessage.success('成员已添加')
  } catch {
    ElMessage.error('添加成员失败')
  }
}

async function handleChangeMemberRole(member: MemberResponse, role: string) {
  if (!spaceStore.currentSpace || member.role === role) return
  try {
    await spaceStore.changeMemberRole(spaceStore.currentSpace.id, member.id, role)
    ElMessage.success('成员角色已更新')
  } catch {
    ElMessage.error('角色更新失败，请确认至少保留一名管理员')
  }
}

async function handleRemoveMember(member: MemberResponse) {
  if (!spaceStore.currentSpace || !canRemoveMember(member)) return
  const confirmed = await ElMessageBox.confirm(
    `确定要移除 ${member.displayName || member.email} 吗？`,
    '移除成员',
    { type: 'warning', confirmButtonText: '移除', cancelButtonText: '取消' },
  ).catch(() => null)
  if (!confirmed) return

  try {
    await spaceStore.deleteMember(spaceStore.currentSpace.id, member.id)
    ElMessage.success('成员已移除')
  } catch {
    ElMessage.error('移除失败，请确认至少保留一名管理员')
  }
}

function openRenameDialog() {
  renameValue.value = spaceStore.currentSpace?.name || ''
  renameDialogVisible.value = true
}

function openInviteDialog() {
  inviteDialogVisible.value = true
}

function isManagerRole(role?: string) {
  return role === 'owner' || role === 'admin'
}

function roleLabel(role: string) {
  return roleOptions.find((option) => option.value === role)?.label || role
}

function roleTagType(role: string) {
  if (role === 'owner') return 'danger'
  if (role === 'admin') return 'warning'
  if (role === 'viewer') return 'info'
  return 'success'
}

function canRemoveMember(member: MemberResponse) {
  if (!canManageMembers.value) return false
  if (authStore.user?.id === member.userId) return false
  return !(isManagerRole(member.role) && activeManagerCount.value <= 1)
}
</script>

<template>
  <AppShell>
    <div class="space-page">
      <h1>生活空间</h1>
      <p class="page-desc">管理你的个人空间和家庭空间，邀请家庭成员共同协作。</p>

      <div class="space-layout">
        <!-- Space list sidebar -->
        <aside class="space-sidebar">
          <div class="sidebar-section">
            <h3>个人空间</h3>
            <ul class="space-list">
              <li
                v-for="space in personalSpaces"
                :key="space.id"
                :class="{ active: spaceStore.currentSpace?.id === space.id }"
                @click="handleSelectSpace(space.id)"
              >
                <span class="space-name">{{ space.name }}</span>
                <span class="space-role">{{ space.memberRole }}</span>
              </li>
              <li v-if="personalSpaces.length === 0" class="empty-item">暂无个人空间</li>
            </ul>
          </div>
          <div class="sidebar-section">
            <h3>家庭空间</h3>
            <ul class="space-list">
              <li
                v-for="space in familySpaces"
                :key="space.id"
                :class="{ active: spaceStore.currentSpace?.id === space.id }"
                @click="handleSelectSpace(space.id)"
              >
                <span class="space-name">{{ space.name }}</span>
                <span class="space-role">{{ space.memberRole }}</span>
              </li>
              <li v-if="familySpaces.length === 0" class="empty-item">暂无家庭空间</li>
            </ul>
          </div>
          <div class="create-space-form">
            <h3>创建家庭空间</h3>
            <el-input v-model="newSpaceName" placeholder="空间名称" size="small" />
            <el-button type="primary" size="small" :disabled="!newSpaceName.trim()" @click="handleCreateSpace">
              创建
            </el-button>
          </div>
        </aside>

        <!-- Space detail -->
        <main class="space-detail">
          <template v-if="spaceStore.currentSpace">
            <div class="detail-header">
              <div>
                <h2>{{ spaceStore.currentSpace.name }}</h2>
                <p class="space-meta">
                  类型: {{ spaceStore.currentSpace.type === 'personal' ? '个人空间' : '家庭空间' }}
                  · 角色: {{ spaceStore.currentSpace.memberRole }}
                </p>
              </div>
              <div class="detail-actions">
                <el-button v-if="canManageMembers" size="small" @click="openRenameDialog">重命名</el-button>
                <el-button v-if="canManageMembers && spaceStore.currentSpace.type !== 'personal'" size="small" type="primary" @click="openInviteDialog">
                  邀请成员
                </el-button>
              </div>
            </div>

            <div class="members-section">
              <h3>成员列表</h3>
              <div v-if="spaceStore.members.length > 0" class="table-scroll">
                <el-table :data="spaceStore.members" stripe style="width: 100%">
                  <el-table-column prop="displayName" label="名称" min-width="140">
                    <template #default="{ row }">
                      <span>{{ row.displayName }}</span>
                      <span v-if="authStore.user?.id === row.userId" class="self-mark">你</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="email" label="邮箱" min-width="190" />
                  <el-table-column prop="role" label="角色" min-width="150">
                    <template #default="{ row }">
                      <el-select
                        v-if="canManageMembers"
                        :model-value="row.role"
                        size="small"
                        class="role-select"
                        @change="(role: string) => handleChangeMemberRole(row, role)"
                      >
                        <el-option
                          v-for="option in roleOptions"
                          :key="option.value"
                          :label="option.label"
                          :value="option.value"
                        />
                      </el-select>
                      <el-tag v-else :type="roleTagType(row.role)" size="small">
                        {{ roleLabel(row.role) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag size="small" type="success">{{ row.status === 'active' ? '正常' : row.status }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column v-if="canManageMembers" label="操作" width="100" fixed="right">
                    <template #default="{ row }">
                      <el-button
                        size="small"
                        type="danger"
                        plain
                        :disabled="!canRemoveMember(row)"
                        @click="handleRemoveMember(row)"
                      >
                        移除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <div v-else class="members-empty">
                <p>暂无成员，{{ spaceStore.currentSpace.type !== 'personal' ? '点击「邀请成员」添加家庭成员。' : '个人空间仅包含你自己。' }}</p>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="empty-state">
              <Home :size="48" class="empty-icon" />
              <p class="empty-title">选择一个空间</p>
              <p class="empty-desc">请从左侧选择一个空间，或创建新的家庭空间。</p>
            </div>
          </template>
        </main>
      </div>

      <!-- Rename dialog -->
      <el-dialog v-model="renameDialogVisible" title="重命名空间" width="400px">
        <el-input v-model="renameValue" placeholder="新名称" />
        <template #footer>
          <el-button @click="renameDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleRename">确认</el-button>
        </template>
      </el-dialog>

      <!-- Invite dialog -->
      <el-dialog v-model="inviteDialogVisible" title="邀请成员" width="400px">
        <el-form label-width="60px">
          <el-form-item label="邮箱">
            <el-input v-model="inviteEmail" placeholder="成员邮箱" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="inviteRole" style="width: 100%">
              <el-option label="成员 (member)" value="member" />
              <el-option label="管理员 (admin)" value="admin" />
              <el-option label="只读 (viewer)" value="viewer" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="inviteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleInvite">邀请</el-button>
        </template>
      </el-dialog>
    </div>
  </AppShell>
</template>

<style scoped>
.space-page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-desc {
  color: var(--color-muted, #888);
  margin-bottom: 24px;
}

.space-layout {
  display: flex;
  gap: 24px;
  min-height: 400px;
}

.space-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.sidebar-section {
  margin-bottom: 20px;
}

.sidebar-section h3 {
  font-size: 14px;
  color: var(--color-muted, #888);
  margin-bottom: 8px;
}

.space-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.space-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.space-list li:hover {
  background: var(--color-hover, #f0f0f0);
}

.space-list li.active {
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
}

.space-role {
  font-size: 12px;
  color: var(--color-muted, #888);
}

.empty-item {
  color: var(--color-muted, #aaa);
  font-size: 13px;
  cursor: default;
}

.create-space-form {
  border-top: 1px solid var(--color-border, #eee);
  padding-top: 16px;
}

.create-space-form h3 {
  font-size: 14px;
  color: var(--color-muted, #888);
  margin-bottom: 8px;
}

.create-space-form .el-input {
  margin-bottom: 8px;
}

.space-detail {
  flex: 1;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.detail-header h2 {
  margin: 0 0 4px 0;
}

.space-meta {
  color: var(--color-muted, #888);
  font-size: 13px;
}

.self-mark {
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
  padding: 1px 6px;
  border-radius: 999px;
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
  font-size: 12px;
}

.role-select {
  width: 118px;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.members-section h3 {
  font-size: 16px;
  margin-bottom: 12px;
}

.members-empty {
  padding: 32px 24px;
  text-align: center;
  color: var(--color-muted, #888);
  font-size: 13px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  text-align: center;
}

.empty-icon {
  color: var(--color-muted, #aaa);
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
  margin: 0;
}

@media (max-width: 768px) {
  .space-layout {
    flex-direction: column;
  }

  .space-sidebar {
    width: 100%;
  }

  .detail-header {
    flex-direction: column;
    gap: 12px;
  }

  .detail-actions {
    width: 100%;
  }
}
</style>
