<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'

const spaceStore = useSpaceStore()
const newSpaceName = ref('')
const newSpaceType = ref('family')
const inviteEmail = ref('')
const inviteRole = ref('member')
const renameValue = ref('')
const renameDialogVisible = ref(false)
const inviteDialogVisible = ref(false)

const personalSpaces = computed(() => spaceStore.spaces.filter((s) => s.type === 'personal'))
const familySpaces = computed(() => spaceStore.spaces.filter((s) => s.type !== 'personal'))

onMounted(async () => {
  await spaceStore.fetchSpaces()
})

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

function openRenameDialog() {
  renameValue.value = spaceStore.currentSpace?.name || ''
  renameDialogVisible.value = true
}

function openInviteDialog() {
  inviteDialogVisible.value = true
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
                <el-button size="small" @click="openRenameDialog">重命名</el-button>
                <el-button v-if="spaceStore.currentSpace.type !== 'personal'" size="small" type="primary" @click="openInviteDialog">
                  邀请成员
                </el-button>
              </div>
            </div>

            <div class="members-section">
              <h3>成员列表</h3>
              <el-table :data="spaceStore.members" stripe style="width: 100%">
                <el-table-column prop="displayName" label="名称" />
                <el-table-column prop="email" label="邮箱" />
                <el-table-column prop="role" label="角色" width="120" />
                <el-table-column prop="status" label="状态" width="100" />
              </el-table>
            </div>
          </template>
          <template v-else>
            <div class="empty-state">
              <p>请从左侧选择一个空间，或创建新的家庭空间。</p>
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

.detail-actions {
  display: flex;
  gap: 8px;
}

.members-section h3 {
  font-size: 16px;
  margin-bottom: 12px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: var(--color-muted, #888);
}
</style>