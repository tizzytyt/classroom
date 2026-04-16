<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearAuth, getUser } from '../utils/auth'

const route = useRoute()
const router = useRouter()
const user = computed(() => getUser())

const nav = [
  { name: '成绩', to: '/teacher/grades' },
  { name: '学习进度', to: '/teacher/progress' },
]

function isActive(to) {
  return route.path === to || route.path.startsWith(to + '/')
}

function logout() {
  clearAuth()
  router.replace('/login')
}
</script>

<template>
  <div class="admin">
    <aside class="admin__aside">
      <div class="admin__brand">
        <div class="admin__brandTitle">学习通后台</div>
        <div class="admin__brandSub">教师端</div>
      </div>

      <nav class="admin__nav">
        <a
          v-for="item in nav"
          :key="item.to"
          class="admin__navItem"
          :class="{ 'is-active': isActive(item.to) }"
          href="javascript:void(0)"
          @click="router.push(item.to)"
        >
          {{ item.name }}
        </a>
      </nav>
    </aside>

    <main class="admin__main">
      <header class="admin__top">
        <div class="admin__topLeft">
          <div class="admin__title">{{ route.meta?.title || '教师端' }}</div>
        </div>
        <div class="admin__topRight">
          <div class="admin__who">
            <span class="admin__whoName">{{ user?.realName || user?.username || '-' }}</span>
            <span class="admin__whoRole">{{ user?.roleCode || '' }}</span>
          </div>
          <button class="btn btn--ghost btn--sm" @click="logout">退出登录</button>
        </div>
      </header>

      <section class="admin__content">
        <router-view />
      </section>
    </main>
  </div>
</template>

