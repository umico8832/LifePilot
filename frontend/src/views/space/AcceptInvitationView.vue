<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Link2, ShieldCheck } from '@lucide/vue'

import AppShell from '@/layouts/AppShell.vue'
import { useSpaceStore } from '@/stores/space'

const route = useRoute()
const router = useRouter()
const spaceStore = useSpaceStore()

const token = ref(typeof route.query.token === 'string' ? route.query.token : '')
const submitting = ref(false)
const errorMessage = ref('')
const acceptedSpaceId = ref<number | null>(null)

const canSubmit = computed(() => token.value.trim().length > 0 && !submitting.value)

async function handleAccept() {
  if (!canSubmit.value) return
  submitting.value = true
  errorMessage.value = ''
  try {
    const invitation = await spaceStore.acceptSpaceInvitation(token.value.trim())
    acceptedSpaceId.value = invitation.householdId
    ElMessage.success('已加入家庭空间')
  } catch (error) {
    errorMessage.value = resolveErrorMessage(error)
  } finally {
    submitting.value = false
  }
}

async function goToSpaces() {
  if (acceptedSpaceId.value) {
    const space = spaceStore.spaces.find((item) => item.id === acceptedSpaceId.value)
    if (space) {
      spaceStore.setCurrentSpace(space)
    }
  }
  await router.push('/spaces')
}

function resolveErrorMessage(error: unknown) {
  const response = error as { response?: { data?: { code?: string; message?: string } } }
  const code = response.response?.data?.code
  const message = response.response?.data?.message
  if (code === 'FORBIDDEN') return '邀请邮箱与当前登录账号不匹配，请切换账号后再试。'
  if (code === 'NOT_FOUND') return '邀请链接无效，请确认链接是否完整。'
  if (code === 'CONFLICT') return '你已经是该空间成员，无需重复加入。'
  if (message?.includes('expired')) return '邀请已过期，请联系管理员重新生成。'
  if (message?.includes('pending')) return '邀请已被撤销或已被接受。'
  return message || '接受邀请失败，请稍后重试。'
}
</script>

<template>
  <AppShell>
    <div class="accept-page">
      <section class="accept-panel">
        <div class="panel-icon">
          <Link2 :size="26" />
        </div>
        <div class="panel-copy">
          <h1>接受家庭空间邀请</h1>
          <p>登录后输入邀请 token，确认加入对应家庭空间。</p>
        </div>

        <div v-if="acceptedSpaceId" class="success-state">
          <ShieldCheck :size="34" />
          <div>
            <h2>邀请已接受</h2>
            <p>空间列表已刷新，可以进入空间查看家庭成员和协作数据。</p>
          </div>
          <el-button type="primary" @click="goToSpaces">查看空间</el-button>
        </div>

        <form v-else class="accept-form" @submit.prevent="handleAccept">
          <label for="invite-token">邀请 token</label>
          <el-input
            id="invite-token"
            v-model="token"
            placeholder="粘贴邀请链接中的 token"
            clearable
          />
          <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
          <el-button type="primary" native-type="submit" :loading="submitting" :disabled="!canSubmit">
            接受邀请
          </el-button>
        </form>
      </section>
    </div>
  </AppShell>
</template>

<style scoped>
.accept-page {
  max-width: 720px;
  margin: 0 auto;
}

.accept-panel {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 16px;
  padding: 28px 0;
}

.panel-icon {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
}

.panel-copy h1 {
  margin: 0 0 8px;
  font-size: 24px;
}

.panel-copy p,
.success-state p {
  margin: 0;
  color: var(--color-muted, #666);
  line-height: 1.6;
}

.accept-form,
.success-state {
  grid-column: 1 / -1;
  border-top: 1px solid var(--color-border, #e5e7eb);
  padding-top: 20px;
}

.accept-form {
  display: grid;
  gap: 12px;
}

.accept-form label {
  font-size: 13px;
  font-weight: 600;
}

.accept-form .el-button {
  justify-self: start;
}

.success-state {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 16px;
  color: var(--el-color-success, #67c23a);
}

.success-state h2 {
  margin: 0 0 4px;
  font-size: 18px;
  color: var(--color-text, #222);
}

.error-text {
  margin: 0;
  color: var(--el-color-danger, #f56c6c);
  font-size: 13px;
}

@media (max-width: 640px) {
  .accept-panel,
  .success-state {
    grid-template-columns: 1fr;
  }

  .success-state .el-button {
    justify-self: start;
  }
}
</style>
