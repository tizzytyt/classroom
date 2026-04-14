const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')

function statusText(s) {
  if (s === 1) return '已发布'
  if (s === 2) return '已下线'
  return '草稿'
}

function toMillis(t) {
  if (!t) return NaN
  const s = String(t).trim()
  if (!s) return NaN
  return new Date(s.replace(/-/g, '/')).getTime()
}

Page({
  data: {
    courseId: '',
    list: [],
    loading: false,
    isTeacher: false
  },
  onLoad(options) {
    const cid = options.courseId != null ? String(options.courseId) : ''
    if (!cid) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    const user = getUser()
    const isTeacher = !!(user && user.roleCode === 'TEACHER')
    this.setData({ courseId: cid, isTeacher })
    wx.setNavigationBarTitle({ title: isTeacher ? '试卷与考试' : '课程考试' })
    this.loadList()
  },
  onShow() {
    if (this.data.courseId) this.loadList()
  },
  async loadList() {
    if (!this.data.courseId) return
    this.setData({ loading: true })
    try {
      const cid = this.data.courseId
      const isTeacher = this.data.isTeacher
      let raw = []
      if (isTeacher) {
        const pair = await Promise.all([
          request(`/api/teacher/courses/${cid}/exams`, 'GET'),
          request(`/api/teacher/courses/${cid}/exams/pending-grading-counts`, 'GET')
        ])
        raw = pair[0]
        const pendingMap = pair[1] || {}
        raw = Array.isArray(raw) ? raw : []
        const list = raw.map((x) => {
          const idStr = x && x.id != null ? String(x.id) : ''
          const pending = pendingMap && pendingMap[idStr] != null
            ? Number(pendingMap[idStr]) || 0
            : 0
          return {
            ...x,
            statusText: statusText(x.status),
            id: idStr,
            pendingCount: pending
          }
        })
        this.setData({ list })
        return
      } else {
        raw = await request(`/api/student/exams?courseId=${encodeURIComponent(cid)}`, 'GET')
      }
      raw = Array.isArray(raw) ? raw : []
      const list = raw.map((x) => ({
        ...x,
        statusText: statusText(x.status),
        // 大整数 id 必须以字符串参与路由，避免 setData/模板中变成不安全的 Number
        id: x.id != null ? String(x.id) : '',
        examDone: !isTeacher && x.myBestScore != null,
        studentStatusText: (!isTeacher && x.myBestScore != null)
          ? '已评分'
          : (toMillis(x.endAt) && Date.now() > toMillis(x.endAt) ? '已结束' : '进行中')
      }))
      this.setData({ list })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  goCreateExam() {
    if (!this.data.isTeacher || !this.data.courseId) return
    wx.navigateTo({
      url: `/pages/course/exam-create/exam-create?courseId=${encodeURIComponent(String(this.data.courseId))}`
    })
  },
  openPaper(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/course/exam-detail/exam-detail?courseId=${encodeURIComponent(this.data.courseId)}&paperId=${encodeURIComponent(String(id))}`
    })
  },
  goGradePaper(e) {
    const id = e.currentTarget.dataset.id
    const title = e.currentTarget.dataset.title ? String(e.currentTarget.dataset.title) : ''
    if (!id) return
    const q = title ? `&paperTitle=${encodeURIComponent(title)}` : ''
    wx.navigateTo({
      url: `/pages/course/exam-grading/exam-grading?courseId=${encodeURIComponent(this.data.courseId)}&paperId=${encodeURIComponent(String(id))}${q}`
    })
  }
})
