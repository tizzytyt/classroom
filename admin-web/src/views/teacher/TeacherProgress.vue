<script setup>
import { computed, onMounted, ref } from 'vue'
import { listTeacherCoursesApi, monitorCourseStudentsApi } from '../../api/teacher'

const loading = ref(false)
const errorMsg = ref('')

const courses = ref([])
const courseId = ref('')
const rows = ref([])

function normalizeError(err) {
  const msg =
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.message ||
    '请求失败，请稍后重试'
  return String(msg)
}

const selectedCourse = computed(() => courses.value.find((c) => String(c.id) === String(courseId.value)) || null)

async function fetchCourses() {
  const list = await listTeacherCoursesApi()
  courses.value = Array.isArray(list) ? list : []
  if (!courseId.value && courses.value.length > 0) {
    courseId.value = String(courses.value[0].id)
  }
}

async function fetchProgress() {
  if (!courseId.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    const list = await monitorCourseStudentsApi(courseId.value)
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
    await fetchProgress()
  } catch (e) {
    errorMsg.value = normalizeError(e)
  } finally {
    loading.value = false
  }
}

function pct(n) {
  if (n === null || n === undefined || Number.isNaN(Number(n))) return '-'
  return `${Math.round(Number(n) * 100)}%`
}

function riskLabel(v) {
  const r = String(v || '').toUpperCase()
  if (r === 'HIGH') return { text: '高风险', cls: 'tag--off' }
  if (r === 'MEDIUM') return { text: '中风险', cls: '' }
  return { text: '低风险', cls: 'tag--ok' }
}

onMounted(init)
</script>

<template>
  <div class="panel">
    <div class="panel__header">
      <div class="panel__title">学习进度</div>
      <div class="panel__headerRight">
        <div class="panel__sub">{{ selectedCourse?.name ? `课程：${selectedCourse.name}` : '请选择课程' }}</div>
        <select v-model="courseId" class="input input--sm" style="width: 220px" :disabled="loading" @change="fetchProgress">
          <option value="" disabled>选择课程</option>
          <option v-for="c in courses" :key="c.id" :value="String(c.id)">{{ c.name || `课程 ${c.id}` }}</option>
        </select>
        <button class="btn btn--ghost btn--sm" :disabled="loading || !courseId" @click="fetchProgress">刷新</button>
      </div>
    </div>

    <div v-if="errorMsg" class="alert" role="alert" style="margin-top: 12px">{{ errorMsg }}</div>

    <div class="tableWrap" style="margin-top: 12px">
      <table class="table">
        <thead>
          <tr>
            <th style="width: 120px">学生ID</th>
            <th style="width: 160px">姓名</th>
            <th style="width: 120px">资源完成</th>
            <th style="width: 120px">作业完成</th>
            <th style="width: 120px">考试完成</th>
            <th style="width: 120px">出勤率</th>
            <th style="width: 110px">平均进度</th>
            <th style="width: 110px">风险</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && rows.length === 0">
            <td colspan="8" class="table__empty">暂无数据</td>
          </tr>
          <tr v-for="r in rows" :key="r.studentId">
            <td>{{ r.studentId }}</td>
            <td>{{ r.studentName || '-' }}</td>
            <td>{{ r.completedResources ?? 0 }}/{{ r.totalResources ?? 0 }}（{{ pct(r.resourceCompletionRate) }}）</td>
            <td>{{ r.submittedAssignments ?? 0 }}/{{ r.totalAssignments ?? 0 }}（{{ pct(r.homeworkCompletionRate) }}）</td>
            <td>{{ r.completedExams ?? 0 }}/{{ r.totalExams ?? 0 }}（{{ pct(r.examCompletionRate) }}）</td>
            <td>{{ pct(r.attendanceRate) }}</td>
            <td>{{ r.avgProgressPercent === null || r.avgProgressPercent === undefined ? '-' : `${Math.round(r.avgProgressPercent)}%` }}</td>
            <td>
              <span class="tag" :class="riskLabel(r.riskLevel).cls">{{ riskLabel(r.riskLevel).text }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

