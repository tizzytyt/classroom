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

export async function listCourseCheckinsApi(courseId) {
  const resp = await http.get(`/api/teacher/courses/${courseId}/checkins`)
  return resp.data
}

export async function createCourseCheckinApi(courseId, payload) {
  const resp = await http.post(`/api/teacher/courses/${courseId}/checkins`, null, { params: payload })
  return resp.data
}

export async function closeCourseCheckinApi(courseId, checkinId) {
  const resp = await http.post(`/api/teacher/courses/${courseId}/checkins/${checkinId}/close`)
  return resp.data
}

export async function checkinStatsApi(courseId, checkinId) {
  const resp = await http.get(`/api/teacher/courses/${courseId}/checkins/${checkinId}/stats`)
  return resp.data
}

