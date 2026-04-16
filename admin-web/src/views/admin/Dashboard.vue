<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getAdminStatsApi } from '../../api/adminStats'

const loading = ref(false)
const errorMsg = ref('')
const stats = reactive({
  usersTotal: 0,
  usersActive: 0,
  admins: 0,
  teachers: 0,
  students: 0,
  courses: 0,
  resources: 0,
  assignments: 0,
  checkins: 0,
})

function normalizeError(err) {
  const msg =
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.message ||
    '请求失败，请稍后重试'
  return String(msg)
}

async function fetchStats() {
  loading.value = true
  errorMsg.value = ''
  try {
    const data = await getAdminStatsApi()
    Object.assign(stats, {
      usersTotal: Number(data?.usersTotal || 0),
      usersActive: Number(data?.usersActive || 0),
      admins: Number(data?.admins || 0),
      teachers: Number(data?.teachers || 0),
      students: Number(data?.students || 0),
      courses: Number(data?.courses || 0),
      resources: Number(data?.resources || 0),
      assignments: Number(data?.assignments || 0),
      checkins: Number(data?.checkins || 0),
    })
  } catch (e) {
    errorMsg.value = normalizeError(e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchStats)
</script>

<template>
  <div class="panel">
    <div class="panel__header">
      <div class="panel__title">系统概览</div>
      <div class="panel__sub">
        <span v-if="loading">加载中...</span>
        <span v-else>数据来自后台统计接口</span>
      </div>
    </div>

    <div v-if="errorMsg" class="alert" role="alert" style="margin-top: 12px">
      {{ errorMsg }}
    </div>

    <div class="grid" style="margin-top: 12px">
      <div class="statCard">
        <div class="statCard__k">用户总数</div>
        <div class="statCard__v">{{ stats.usersTotal }}</div>
        <div class="statCard__s">启用：{{ stats.usersActive }}</div>
      </div>
      <div class="statCard">
        <div class="statCard__k">角色分布</div>
        <div class="statCard__v">{{ stats.admins + stats.teachers + stats.students }}</div>
        <div class="statCard__s">管：{{ stats.admins }} / 教：{{ stats.teachers }} / 学：{{ stats.students }}</div>
      </div>
      <div class="statCard">
        <div class="statCard__k">课程数</div>
        <div class="statCard__v">{{ stats.courses }}</div>
        <div class="statCard__s">累计创建课程</div>
      </div>
      <div class="statCard">
        <div class="statCard__k">资源数</div>
        <div class="statCard__v">{{ stats.resources }}</div>
        <div class="statCard__s">课程资源总量</div>
      </div>
      <div class="statCard">
        <div class="statCard__k">作业数</div>
        <div class="statCard__v">{{ stats.assignments }}</div>
        <div class="statCard__s">发布作业总量</div>
      </div>
      <div class="statCard">
        <div class="statCard__k">签到数</div>
        <div class="statCard__v">{{ stats.checkins }}</div>
        <div class="statCard__s">签到活动总量</div>
      </div>
    </div>

    <div class="actions" style="margin-top: 14px">
      <button class="btn btn--ghost btn--sm" :disabled="loading" @click="fetchStats">刷新</button>
    </div>
  </div>
</template>

