const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')
const { baseURL } = require('../../../utils/config')

/** 后端 0～1 转为展示用百分数字符串 */
function pctText(ratio) {
  if (ratio == null || ratio === '') return null
  const n = Number(ratio)
  if (!Number.isFinite(n)) return null
  return String(Math.round(n * 1000) / 10)
}

Page({
  data: {
    courseId: null,
    isTeacher: false,
    /** 学生：个人总评 */
    grade: null,
    /** 教师：全班统计概览 */
    overview: null,
    /** 教师：每名学生的总评明细 */
    studentGrades: [],
    /** 学生：课程总评权重说明 */
    gradeRule: null,
    loading: false
  },
  onLoad(options) {
    const cid = options && options.courseId != null ? String(options.courseId).trim() : ''
    if (!cid) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    const user = getUser()
    const isTeacher = !!(user && user.roleCode === 'TEACHER')
    this.setData({ courseId: cid, isTeacher })
    this.loadGrade()
  },
  async loadGrade() {
    this.setData({ loading: true })
    try {
      if (this.data.isTeacher) {
        const cid = this.data.courseId
        const [raw, listRaw] = await Promise.all([
          request(`/api/teacher/courses/${cid}/stats/overview`, 'GET'),
          request(`/api/teacher/courses/${cid}/grades/students`, 'GET')
        ])
        let overview = raw || null
        if (overview) {
          overview = {
            ...overview,
            attendancePct: pctText(overview.avgAttendanceRate),
            homeworkPct: pctText(overview.avgHomeworkCompletionRate)
          }
        }
        const rows = Array.isArray(listRaw) ? listRaw : []
        const studentGrades = rows.map((r) => ({
          ...r,
          studentIdKey: r.studentId != null ? String(r.studentId) : ''
        }))
        this.setData({ overview, studentGrades, grade: null, gradeRule: null })
      } else {
        const res = await request(`/api/student/grades/final?courseId=${this.data.courseId}`, 'GET')
        const g = res && res.grade ? res.grade : null
        const rawRule = res && res.gradeRule ? res.gradeRule : null
        let gradeRule = null
        if (rawRule) {
          gradeRule = {
            ...rawRule,
            assignmentPct: pctText(rawRule.assignmentWeight),
            checkinPct: pctText(rawRule.checkinWeight),
            resourcePct: pctText(rawRule.resourceWeight),
            examPct: pctText(rawRule.examWeight)
          }
        }
        this.setData({ grade: g || null, gradeRule, overview: null, studentGrades: [] })
      }
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  exportFinalScores() {
    if (!this.data.isTeacher || !this.data.courseId) return
    const token = wx.getStorageSync('token') || ''
    const url = `${baseURL}/api/teacher/courses/${encodeURIComponent(String(this.data.courseId))}/grades/export`
    wx.showLoading({ title: '生成中...' })
    wx.downloadFile({
      url,
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success: (res) => {
        wx.hideLoading()
        if (res.statusCode < 200 || res.statusCode >= 300 || !res.tempFilePath) {
          wx.showToast({ title: '下载失败', icon: 'none' })
          return
        }
        wx.openDocument({
          filePath: res.tempFilePath,
          fileType: 'xls',
          showMenu: true,
          fail: () => wx.showToast({ title: '打开失败', icon: 'none' })
        })
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '下载失败', icon: 'none' })
      }
    })
  }
})

