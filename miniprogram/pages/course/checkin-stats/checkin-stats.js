const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')

function pctText(n) {
  if (!Number.isFinite(n)) return '—'
  return `${String(Math.round(n * 1000) / 10)}%`
}

Page({
  data: {
    courseId: '',
    checkinId: '',
    loading: false,
    errorText: '',
    shouldAttend: 0,
    attended: 0,
    attendanceRateText: '—',
    absentList: []
  },
  onLoad(options) {
    const user = getUser()
    if (!user || user.roleCode !== 'TEACHER') {
      wx.showToast({ title: '无权限', icon: 'none' })
      return
    }
    const courseId = options && options.courseId != null ? String(options.courseId).trim() : ''
    const checkinId = options && options.checkinId != null ? String(options.checkinId).trim() : ''
    if (!courseId || !checkinId) {
      wx.showToast({ title: '缺少参数', icon: 'none' })
      return
    }
    this.setData({ courseId, checkinId })
    this.load()
  },
  async load() {
    this.setData({ loading: true, errorText: '' })
    try {
      const { courseId, checkinId } = this.data
      const stats = await request(`/api/teacher/courses/${courseId}/checkins/${checkinId}/stats`, 'GET')
      const checked = (stats && stats.checkedIn) ? stats.checkedIn : []
      const notChecked = (stats && stats.notCheckedIn) ? stats.notCheckedIn : []
      const checkedList = Array.isArray(checked) ? checked : []
      const absentList = Array.isArray(notChecked) ? notChecked : []
      const attended = checkedList.length
      const shouldAttend = attended + absentList.length
      const rate = shouldAttend > 0 ? (attended / shouldAttend) : NaN
      this.setData({
        shouldAttend,
        attended,
        attendanceRateText: pctText(rate),
        absentList
      })
    } catch (e) {
      this.setData({ errorText: (e && e.message) ? e.message : '加载失败' })
    } finally {
      this.setData({ loading: false })
    }
  }
})

