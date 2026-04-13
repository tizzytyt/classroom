const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')

function toPercentInt(value) {
  const n = Number(value)
  if (!Number.isFinite(n)) return 0
  return Math.round(n * 100)
}

/** 后端 riskLevel：学习风险（非「活跃度」）；LOW=低风险较好 */
function riskLabel(level) {
  const u = String(level || '').toUpperCase()
  if (u === 'HIGH') return '高风险'
  if (u === 'LOW') return '低风险'
  return level || '—'
}

Page({
  data: {
    courseId: null,
    loading: false,
    list: [],
    showRec: false,
    recStudentId: null,
    recStudentName: '',
    recLoading: false,
    recList: [],
    pushReason: '',
    pushing: false
  },
  onLoad(options) {
    const cid = options && options.courseId != null ? String(options.courseId).trim() : ''
    if (!cid) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    const user = getUser()
    if (!user || user.roleCode !== 'TEACHER') {
      wx.showToast({ title: '无权限', icon: 'none' })
      return
    }
    this.setData({ courseId: cid })
    this.load()
  },
  async load() {
    this.setData({ loading: true })
    try {
      const res = await request(`/api/teacher/courses/${this.data.courseId}/monitor/students`, 'GET')
      const rawList = Array.isArray(res) ? res : []
      const list = rawList.map(item => {
        const x = item || {}
        return {
          ...x,
          riskLabel: riskLabel(x.riskLevel),
          resourceCompletionRatePercent: toPercentInt(x.resourceCompletionRate),
          homeworkCompletionRatePercent: toPercentInt(x.homeworkCompletionRate),
          examCompletionRatePercent: toPercentInt(x.examCompletionRate),
          attendanceRatePercent: toPercentInt(x.attendanceRate)
        }
      })
      this.setData({ list })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  async showRecommend(e) {
    const studentId = e.currentTarget.dataset.id
    const studentName = e.currentTarget.dataset.name || ''
    if (!studentId) return
    this.setData({
      showRec: true,
      recStudentId: String(studentId),
      recStudentName: studentName,
      recLoading: true,
      recList: []
    })
    try {
      const list = await request(`/api/teacher/courses/${this.data.courseId}/monitor/students/${studentId}/recommendations?limit=8`, 'GET')
      this.setData({ recList: Array.isArray(list) ? list : [] })
    } catch (e2) {
      wx.showToast({ title: (e2 && e2.message) ? e2.message : '加载推荐失败', icon: 'none' })
    } finally {
      this.setData({ recLoading: false })
    }
  },
  closeRec() {
    this.setData({ showRec: false, pushReason: '', pushing: false })
  },
  onPushReasonInput(e) {
    this.setData({ pushReason: (e.detail.value || '').trim() })
  },
  async pushRec() {
    if (!this.data.recStudentId) return
    const ids = (this.data.recList || []).map(x => x.id).filter(Boolean)
    if (!ids.length) {
      wx.showToast({ title: '暂无可推送资源', icon: 'none' })
      return
    }
    this.setData({ pushing: true })
    try {
      const res = await request(
        `/api/teacher/courses/${this.data.courseId}/monitor/students/${this.data.recStudentId}/recommendations/push`,
        'POST',
        { resourceIds: ids, reason: this.data.pushReason || '' }
      )
      wx.showToast({ title: `已推送${(res && res.pushedCount) || 0}条`, icon: 'success' })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '推送失败', icon: 'none' })
    } finally {
      this.setData({ pushing: false })
    }
  }
})

