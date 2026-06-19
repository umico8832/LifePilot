<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const displayName = ref('')
const avatarUrl = ref('')
const saveSuccess = ref(false)
const saveError = ref('')
const saving = computed(() => authStore.loading)
const user = computed(() => authStore.user)

const noSpace = computed(() => !authStore.isAuthenticated)

onMounted(() => {
  if (user.value) {
    displayName.value = user.value.displayName || ''
    avatarUrl.value = user.value.avatarUrl || ''
  }
})

async function handleSubmit() {
  saveSuccess.value = false
  saveError.value = ''

  if (!displayName.value.trim()) {
    saveError.value = '显示名称不能为空'
    return
  }

  try {
    await authStore.updateProfile({
      displayName: displayName.value.trim(),
      avatarUrl: avatarUrl.value.trim() || undefined,
    })
    saveSuccess.value = true
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '更新失败，请重试'
    saveError.value = msg
  }
}
</script>

<template>
  <div class="page-container">
    <div v-if="noSpace" class="empty-state">
      <p>请先登录后查看个人设置。</p>
    </div>

    <template v-else>
      <h2 class="page-title">个人设置</h2>

      <div class="profile-card">
        <div class="profile-avatar-section">
          <div class="avatar-preview">
            <img
              v-if="avatarUrl"
              :src="avatarUrl"
              alt="头像"
              class="avatar-img"
              @error="($event.target as HTMLImageElement).style.display = 'none'"
            />
            <div v-else class="avatar-placeholder">
              {{ (displayName || '?').charAt(0).toUpperCase() }}
            </div>
          </div>
        </div>

        <form class="profile-form" @submit.prevent="handleSubmit">
          <div class="form-group">
            <label class="form-label">邮箱</label>
            <input
              type="email"
              class="form-input"
              :value="user?.email"
              disabled
            />
            <span class="form-hint">邮箱不可修改</span>
          </div>

          <div class="form-group">
            <label class="form-label" for="displayName">显示名称</label>
            <input
              id="displayName"
              v-model="displayName"
              type="text"
              class="form-input"
              placeholder="请输入显示名称"
              maxlength="100"
            />
          </div>

          <div class="form-group">
            <label class="form-label" for="avatarUrl">头像链接</label>
            <input
              id="avatarUrl"
              v-model="avatarUrl"
              type="url"
              class="form-input"
              placeholder="https://example.com/avatar.jpg"
              maxlength="500"
            />
            <span class="form-hint">支持外部图片 URL</span>
          </div>

          <div v-if="saveSuccess" class="success-message">保存成功！</div>
          <div v-if="saveError" class="error-message">{{ saveError }}</div>

          <button type="submit" class="btn-primary" :disabled="saving">
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </form>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page-container {
  padding: 24px;
  max-width: 600px;
  margin: 0 auto;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1a1a2e;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.profile-avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 28px;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  background: #f0f0f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 32px;
  font-weight: 600;
  color: #6c5ce7;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.form-input {
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
  outline: none;
}

.form-input:focus {
  border-color: #6c5ce7;
}

.form-input:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.form-hint {
  font-size: 12px;
  color: #999;
}

.success-message {
  color: #27ae60;
  font-size: 14px;
  padding: 8px 12px;
  background: #f0faf4;
  border-radius: 6px;
}

.error-message {
  color: #e74c3c;
  font-size: 14px;
  padding: 8px 12px;
  background: #fdf0f0;
  border-radius: 6px;
}

.btn-primary {
  padding: 12px 24px;
  background: #6c5ce7;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  align-self: flex-start;
}

.btn-primary:hover:not(:disabled) {
  background: #5a4bd1;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.empty-state {
  text-align: center;
  padding: 48px 24px;
  color: #999;
  font-size: 15px;
}
</style>