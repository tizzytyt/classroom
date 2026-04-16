import { http } from './http'

export async function listTeacherCoursesApi() {
  const resp = await http.get('/api/teacher/courses')
  return resp.data
}

export async function listCourseStudentGradesApi(courseId) {
  const resp = await http.get(`/api/teacher/courses/${courseId}/grades/students`)
  return resp.data
}

export async function monitorCourseStudentsApi(courseId) {
  const resp = await http.get(`/api/teacher/courses/${courseId}/monitor/students`)
  return resp.data
}

export async function exportCourseFinalScoresApi(courseId) {
  const resp = await http.get(`/api/teacher/courses/${courseId}/grades/export`, {
    responseType: 'blob',
  })
  return resp
}

