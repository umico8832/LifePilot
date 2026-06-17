<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ArrowLeft, LockKeyhole, UserPlus } from '@lucide/vue'
import { useRouter } from 'vue-router'

import AppShell from '@/layouts/AppShell.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const mode = ref<'login' | 'register'>('login')
const errorMessage = ref('')

const form = reactive({
  email: '',
  password: '',
  displayName: '',
})

async function submit() {
  errorMessage.value = ''

  try {
    if (mode.value === 'register') {
      await authStore.register({
        email: form.email,
        password: form.password,
        displayName: form.displayName,
      })
    } else {
      await authStore.login({
        email: form.email,
        password: form.password,
      })
    }

    const redirect = (router.currentRoute.value.query.redirect as string) || '/'
    await router.push(redirect)
  } catch (error) {
    errorMessage.value = readErrorMessage(error)
  }
}

function readErrorMessage(error: unknown) {
  if (
    typeof error === 'object' &&
    error !== null &&
    'response' in error &&
    typeof error.response === 'object' &&
    error.response !== null &&
    'data' in error.response
  ) {
    const data = error.response.data as { message?: string }
    return data.message || '请求失败'
  }

  return '请求失败'
}
</script>

<template>
  <AppShell>
    <section class="auth-header">
      <button class="text-button" type="button" title="返回总览" @click="router.push('/')">
        <ArrowLeft :size="18" />
        总览
      </button>
      <div class="auth-title">
        <p class="eyebrow">LifePilot Account</p>
        <h1>{{ mode === 'login' ? '登录' : '注册' }}</h1>
      </div>
    </section>

    <section class="auth-surface">
      <div class="segmented-control" role="tablist" aria-label="Auth mode">
        <button
          type="button"
          :class="{ active: mode === 'login' }"
          role="tab"
          @click="mode = 'login'"
        >
          登录
        </button>
        <button
          type="button"
          :class="{ active: mode === 'register' }"
          role="tab"
          @click="mode = 'register'"
        >
          注册
        </button>
      </div>

      <form class="auth-form" @submit.prevent="submit">
        <label>
          邮箱
          <input v-model.trim="form.email" type="email" autocomplete="email" required />
        </label>
        <label v-if="mode === 'register'">
          昵称
          <input v-model.trim="form.displayName" type="text" autocomplete="name" required />
        </label>
        <label>
          密码
          <input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            minlength="8"
            required
          />
        </label>

        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

        <button class="primary-button" type="submit" :disabled="authStore.loading">
          <component :is="mode === 'login' ? LockKeyhole : UserPlus" :size="18" />
          {{ mode === 'login' ? '登录' : '注册' }}
        </button>
      </form>
    </section>
  </AppShell>
</template>
