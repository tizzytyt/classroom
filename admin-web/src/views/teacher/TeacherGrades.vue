<script setup>
import { computed, onMounted, ref } from 'vue'
import { exportCourseFinalScoresApi, listCourseStudentGradesApi, listTeacherCoursesApi } from '../../api/teacher'
import { downloadBlob, filenameFromContentDisposition } from '../../utils/download'

const loading = ref(false)
const errorMsg = ref('')

const courses = ref([])
const courseId = ref('')

const rows = ref([])
const exportLoading = ref(false)

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

async function fetchGrades() {
  if (!courseId.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    const list = await listCourseStudentGradesApi(courseId.value)
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
    await fetchGrades()
  } catch (e) {
    errorMsg.value = normalizeError(e)
  } finally {
    loading.value = false
  }
}

function fmt(n) {
  if (n === null || n === undefined || Number.isNaN(Number(n))) return '-'
  return Number(n).toFixed(1)
}

async function exportExcel() {
  if (!courseId.value || exportLoading.value) return
  exportLoading.value = true
  try {
    const resp = await exportCourseFinalScoresApi(courseId.value)
    const cd = resp?.headers?.['content-disposition'] || resp?.headers?.['Content-Disposition']
    const nameFromHeader = filenameFromContentDisposition(cd)
    const fallback = `course_final_scores_${courseId.value}.xls`
    downloadBlob(resp.data, nameFromHeader || fallback)
  } catch (e) {
    window.alert(normalizeError(e))
  } finally {
    exportLoading.value = false
  }
}

onMounted(init)
</script>

<template>
  <div class="panel">
    <div class="panel__header">
      <div class="panel__title">成绩</div>
      <div class="panel__headerRight">
        <div class="panel__sub">{{ selectedCourse?.name ? `课程：${selectedCourse.name}` : '请选择课程' }}</div>
        <select v-model="courseId" class="input input--sm" style="width: 220px" :disabled="loading" @change="fetchGrades">
          <option value="" disabled>选择课程</option>
          <option v-for="c in courses" :key="c.id" :value="String(c.id)">{{ c.name || `课程 ${c.id}` }}</option>
        </select>
        <button class="btn btn--ghost btn--sm" :disabled="loading || !courseId" @click="fetchGrades">刷新</button>
        <button class="btn btn--sm" :disabled="loading || exportLoading || !courseId" @click="exportExcel">
          <span v-if="exportLoading">导出中...</span>
          <span v-else>导出 Excel</span>
        </button>
      </div>
    </div>

    <div v-if="errorMsg" class="alert" role="alert" style="margin-top: 12px">{{ errorMsg }}</div>

    <div class="tableWrap" style="margin-top: 12px">
      <table class="table">
        <thead>
          <tr>
            <th style="width: 120px">学生ID</th>
            <th style="width: 160px">姓名</th>
            <th style="width: 110px">作业</th>
            <th style="width: 110px">签到</th>
            <th style="width: 110px">资源</th>
            <th style="width: 110px">考试</th>
            <th style="width: 110px">总评</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && rows.length === 0">
            <td colspan="7" class="table__empty">暂无数据</td>
          </tr>
          <tr v-for="r in rows" :key="r.studentId">
            <td>{{ r.studentId }}</td>
            <td>{{ r.studentName || '-' }}</td>
            <td>{{ fmt(r.assignmentScore) }}</td>
            <td>{{ fmt(r.checkinScore) }}</td>
            <td>{{ fmt(r.resourceScore) }}</td>
            <td>{{ fmt(r.examScore) }}</td>
            <td><span class="tag tag--ok">{{ fmt(r.finalScore) }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

