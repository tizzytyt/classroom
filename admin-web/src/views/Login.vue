<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { loginApi } from '../api/auth'
import { clearAuth, setAuth } from '../utils/auth'

const router = useRouter()
const route = useRoute()

const form = reactive({
  username: '',
  password: '',
})

const loading = ref(false)
const errorMsg = ref('')

const canSubmit = computed(() => {
  return !!form.username?.trim() && !!form.password?.trim() && !loading.value
})

function normalizeError(err) {
  const msg =
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.message ||
    '登录失败，请稍后重试'
  return String(msg)
}

async function onSubmit() {
  if (!canSubmit.value) return
  errorMsg.value = ''
  loading.value = true
  try {
    clearAuth()
    const data = await loginApi({
      username: form.username.trim(),
      password: form.password,
    })

    if (!data?.token) {
      throw new Error('登录失败：未返回 token')
    }
    if (!['ADMIN', 'TEACHER'].includes(data?.roleCode)) {
      throw new Error('当前账号无 Web 端访问权限')
    }

    setAuth(data.token, {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      roleCode: data.roleCode,
    })

    const defaultPath = data?.roleCode === 'TEACHER' ? '/teacher/grades' : '/admin/dashboard'
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : defaultPath
    await router.replace(redirect)
  } catch (e) {
    errorMsg.value = normalizeError(e)
  } finally {
    loading.value = false
  }
}

</script>

<template>
  <div class="page page-login">
    <div class="card">
      <div class="brand">
        <div class="brand__title">学习通系统</div>
        <div class="brand__sub">管理端登录</div>
      </div>

      <form class="form" @submit.prevent="onSubmit">
        <label class="field">
          <span class="field__label">用户名</span>
          <input
            v-model="form.username"
            class="input"
            autocomplete="username"
            placeholder="请输入用户名"
            :disabled="loading"
          />
        </label>

        <label class="field">
          <span class="field__label">密码</span>
          <input
            v-model="form.password"
            class="input"
            type="password"
            autocomplete="current-password"
            placeholder="请输入密码"
            :disabled="loading"
          />
        </label>

        <div v-if="errorMsg" class="alert" role="alert">
          {{ errorMsg }}
        </div>

        <button class="btn" type="submit" :disabled="!canSubmit">
          <span v-if="loading">登录中...</span>
          <span v-else>登录</span>
        </button>
      </form>
    </div>
  </div>
</template>

