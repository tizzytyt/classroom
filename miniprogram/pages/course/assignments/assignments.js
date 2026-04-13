const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')
const { isPastDueAt } = require('../../../utils/assignmentDue')

Page({
  data: {
    courseId: null,
    list: [],
    loading: false,
    user: null,
    isTeacher: false,
  },
  onShow() {
    if (this.data.courseId) this.loadAssignments()
  },
  onLoad(options) {
    const cid = options.courseId != null && String(options.courseId).trim() !== '' ? String(options.courseId).trim() : ''
    if (!cid) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    const user = getUser()
    const isTeacher = !!(user && user.roleCode === 'TEACHER')
    this.setData({ courseId: cid, user, isTeacher })
    this.loadAssignments()
  },
  async loadAssignments() {
    this.setData({ loading: true })
    try {
      const cid = this.data.courseId
      const isTeacher = this.data.isTeacher
      let raw = []
      let statsList = []
      if (isTeacher) {
        const [a, s] = await Promise.all([
          request(`/api/teacher/courses/${cid}/assignments`, 'GET'),
          request(`/api/teacher/courses/${cid}/stats/homeworks`, 'GET')
        ])
        raw = Array.isArray(a) ? a : []
        statsList = Array.isArray(s) ? s : []
      } else {
        raw = await request(`/api/student/assignments?courseId=${cid}`, 'GET')
        raw = Array.isArray(raw) ? raw : []
      }
      const statMap = {}
      statsList.forEach((st) => {
        const hid = st.homeworkId != null ? String(st.homeworkId) : ''
        if (hid) statMap[hid] = st
      })
      const list = raw.map((x) => {
        const idStr = x.id != null ? String(x.id) : ''
        const row = {
          ...x,
          id: idStr,
          isPastDue: isPastDueAt(x.dueAt)
        }
        if (isTeacher) {
          const st = statMap[idStr] || {}
          const submit = st.submitCount != null ? Number(st.submitCount) : 0
          const total = st.studentCount != null ? Number(st.studentCount) : 0
          const pct = total > 0 ? Math.min(100, Math.round((submit / total) * 1000) / 10) : 0
          row.submitCount = submit
          row.studentCount = total
          row.notSubmitCount = Math.max(0, total - submit)
          row.progressPercent = pct
        }
        return row
      })
      this.setData({ list })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载作业失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/course/assignment-detail/assignment-detail?id=${encodeURIComponent(String(id))}&courseId=${encodeURIComponent(String(this.data.courseId))}`
    })
  },
  goCreateAssignment() {
    if (!this.data.isTeacher || !this.data.courseId) return
    wx.navigateTo({
      url: `/pages/course/assignment-create/assignment-create?courseId=${encodeURIComponent(String(this.data.courseId))}`
    })
  }
})

