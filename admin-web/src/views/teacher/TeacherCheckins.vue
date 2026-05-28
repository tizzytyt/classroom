<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  checkinStatsApi,
  closeCourseCheckinApi,
  createCourseCheckinApi,
  listCourseCheckinsApi,
  listTeacherCoursesApi,
} from '../../api/teacher'

const loading = ref(false)
const errorMsg = ref('')
const courses = ref([])
const courseId = ref('')
const rows = ref([])

const createOpen = ref(false)
const createLoading = ref(false)
const createError = ref('')
const createForm = reactive({
  title: '',
  endAt: '',
})

const statsOpen = ref(false)
const statsLoading = ref(false)
const statsTitle = ref('')
const statsData = reactive({
  checkedIn: [],
  notCheckedIn: [],
})

const selectedCourse = computed(() => courses.value.find((c) => String(c.id) === String(courseId.value)) || null)

function normalizeError(err) {
  const msg =
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.message ||
    '请求失败，请稍后重试'
  return String(msg)
}

function toBackendDateTime(v) {
  if (!v) return ''
  return `${v.replace('T', ' ')}:00`
}

function fmtDate(v) {
  if (!v) return '-'
  return String(v).replace('T', ' ')
}

async function fetchCourses() {
  const list = await listTeacherCoursesApi()
  courses.value = Array.isArray(list) ? list : []
  if (!courseId.value && courses.value.length > 0) {
    courseId.value = String(courses.value[0].id)
  }
}

async function fetchCheckins() {
  if (!courseId.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    const list = await listCourseCheckinsApi(courseId.value)
    rows.value = Array.isArray(list) ? list : []
  } catch (e) {
    errorMsg.value = normalizeError(e)
  } finally {
    loading.value = false
  }
}

async function init() {
  loading.value = true
  errorMsg.value = ''
  try {
    await fetchCourses()
    await fetchCheckins()
  } catch (e) {
    errorMsg.value = normalizeError(e)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  createError.value = ''
  createForm.title = ''
  createForm.endAt = ''
  createOpen.value = true
}

function closeCreate() {
  if (createLoading.value) return
  createOpen.value = false
}

async function submitCreate() {
  if (!courseId.value || createLoading.value) return
  if (!createForm.title.trim()) {
    createError.value = '请输入签到标题'
    return
  }
  createLoading.value = true
  createError.value = ''
  try {
    const payload = { title: createForm.title.trim() }
    if (createForm.endAt) payload.endAt = toBackendDateTime(createForm.endAt)
    await createCourseCheckinApi(courseId.value, payload)
    createOpen.value = false
    await fetchCheckins()
  } catch (e) {
    createError.value = normalizeError(e)
  } finally {
    createLoading.value = false
  }
}

async function closeCheckin(item) {
  if (!courseId.value) return
  const ok = window.confirm(`确认结束签到「${item.title || item.id}」吗？`)
  if (!ok) return
  try {
    await closeCourseCheckinApi(courseId.value, item.id)
    await fetchCheckins()
  } catch (e) {
    window.alert(normalizeError(e))
  }
}

async function openStats(item) {
  if (!courseId.value) return
  statsOpen.value = true
  statsLoading.value = true
  statsTitle.value = item.title || `签到 ${item.id}`
  statsData.checkedIn = []
  statsData.notCheckedIn = []
  try {
    const data = await checkinStatsApi(courseId.value, item.id)
    statsData.checkedIn = Array.isArray(data?.checkedIn) ? data.checkedIn : []
    statsData.notCheckedIn = Array.isArray(data?.notCheckedIn) ? data.notCheckedIn : []
  } catch (e) {
    window.alert(normalizeError(e))
  } finally {
    statsLoading.value = false
  }
}

onMounted(init)
</script>

<template>
  <div class="panel">
    <div class="panel__header">
      <div class="panel__title">签到管理</div>
      <div class="panel__headerRight">
        <div class="panel__sub">{{ selectedCourse?.name ? `课程：${selectedCourse.name}` : '请选择课程' }}</div>
        <select v-model="courseId" class="input input--sm" style="width: 220px" :disabled="loading" @change="fetchCheckins">
          <option value="" disabled>选择课程</option>
          <option v-for="c in courses" :key="c.id" :value="String(c.id)">{{ c.name || `课程 ${c.id}` }}</option>
        </select>
        <button class="btn btn--ghost btn--sm" :disabled="loading || !courseId" @click="fetchCheckins">刷新</button>
        <button class="btn btn--sm" :disabled="loading || !courseId" @click="openCreate">发起签到</button>
      </div>
    </div>

    <div v-if="errorMsg" class="alert" role="alert" style="margin-top: 12px">{{ errorMsg }}</div>

    <div class="tableWrap" style="margin-top: 12px">
      <table class="table">
        <thead>
          <tr>
            <th>标题</th>
            <th style="width: 160px">签到码</th>
            <th style="width: 180px">开始时间</th>
            <th style="width: 180px">结束时间</th>
            <th style="width: 100px">状态</th>
            <th style="width: 200px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && rows.length === 0">
            <td colspan="6" class="table__empty">暂无数据</td>
          </tr>
          <tr v-for="r in rows" :key="r.id">
            <td>{{ r.title || '-' }}</td>
            <td>{{ r.checkinCode || '-' }}</td>
            <td>{{ fmtDate(r.startAt) }}</td>
            <td>{{ fmtDate(r.endAt) }}</td>
            <td>
              <span class="tag" :class="r.status === 1 ? 'tag--ok' : 'tag--off'">
                {{ r.status === 1 ? '进行中' : '已结束' }}
              </span>
            </td>
            <td>
              <div class="ops">
                <button class="btn btn--ghost btn--xs" @click="openStats(r)">统计</button>
                <button class="btn btn--danger btn--xs" :disabled="r.status !== 1" @click="closeCheckin(r)">结束</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="createOpen" class="modal" @click.self="closeCreate">
      <div class="modal__card" style="width: 520px">
        <div class="modal__title">发起签到</div>
        <div class="formGrid" style="grid-template-columns: 1fr">
          <label class="field">
            <span class="field__label">签到标题 *</span>
            <input v-model="createForm.title" class="input input--sm" placeholder="例如：第3周课堂签到" :disabled="createLoading" />
          </label>
          <label class="field">
            <span class="field__label">结束时间（可选）</span>
            <input v-model="createForm.endAt" class="input input--sm" type="datetime-local" :disabled="createLoading" />
          </label>
        </div>
        <div v-if="createError" class="alert" role="alert" style="margin-top: 10px">{{ createError }}</div>
        <div class="modal__actions">
          <button class="btn btn--ghost btn--sm" :disabled="createLoading" @click="closeCreate">取消</button>
          <button class="btn btn--sm" :disabled="createLoading" @click="submitCreate">
            <span v-if="createLoading">提交中...</span>
            <span v-else>创建</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="statsOpen" class="modal" @click.self="statsOpen = false">
      <div class="modal__card" style="width: 760px">
        <div class="modal__title">签到统计：{{ statsTitle }}</div>
        <div class="panel__sub" style="margin-top: 6px">
          已签到 {{ statsData.checkedIn.length }} 人，未签到 {{ statsData.notCheckedIn.length }} 人
        </div>

        <div v-if="statsLoading" class="panel__sub" style="margin-top: 10px">加载中...</div>
        <div v-else class="grid" style="grid-template-columns: 1fr 1fr; margin-top: 12px">
          <div class="tableWrap">
            <table class="table">
              <thead>
                <tr>
                  <th>已签到</th>
                  <th style="width: 180px">时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="statsData.checkedIn.length === 0">
                  <td colspan="2" class="table__empty">暂无</td>
                </tr>
                <tr v-for="u in statsData.checkedIn" :key="`${u.studentId}-in`">
                  <td>{{ u.studentName }}</td>
                  <td>{{ fmtDate(u.checkedInAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="tableWrap">
            <table class="table">
              <thead>
                <tr>
                  <th>未签到</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="statsData.notCheckedIn.length === 0">
                  <td class="table__empty">暂无</td>
                </tr>
                <tr v-for="u in statsData.notCheckedIn" :key="`${u.studentId}-out`">
                  <td>{{ u.studentName }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="modal__actions">
          <button class="btn btn--ghost btn--sm" @click="statsOpen = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

