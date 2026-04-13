const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')

function pad2(n) {
  return String(n).padStart(2, '0')
}

function formatDate(d) {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}

function formatTime(d) {
  return `${pad2(d.getHours())}:${pad2(d.getMinutes())}`
}

/** 组合为后端 yyyy-MM-dd HH:mm:ss */
function toDateTimePayload(dateStr, timeStr) {
  if (!dateStr || !timeStr) return null
  const t = timeStr.length === 5 ? `${timeStr}:00` : timeStr
  return `${dateStr} ${t}`
}

/** 默认截止：当前时间 +1 小时（与考试「结束时间」默认可调思路一致） */
function defaultEndPicker() {
  const end = new Date(Date.now() + 60 * 60 * 1000)
  return {
    endDate: formatDate(end),
    endTime: formatTime(end)
  }
}

Page({
  data: {
    courseId: null,
    checkinCode: '',
    submitting: false,
    user: null,
    isTeacher: false,
    historyLoading: false,
    historyList: [],
    creating: false,
    createForm: {
      title: ''
    },
    /** 是否设置签到截止时间（不设置则需教师手动结束） */
    hasEndDeadline: false,
    endDate: '',
    endTime: '',
    loadingList: false,
    checkins: []
  },
  resetEndPicker() {
    const r = defaultEndPicker()
    this.setData({ endDate: r.endDate, endTime: r.endTime })
  },
  onLoad(options) {
    const cid = options && options.courseId != null ? String(options.courseId).trim() : ''
    const user = getUser()
    const isTeacher = !!(user && user.roleCode === 'TEACHER')
    this.setData({ courseId: cid, user, isTeacher })
    if (isTeacher && cid) {
      this.resetEndPicker()
      this.loadCheckins()
      return
    }
    if (!isTeacher && cid) {
      this.loadHistory()
    }
  },
  onEndDeadlineChange(e) {
    const on = !!(e.detail && e.detail.value)
    if (on) this.resetEndPicker()
    this.setData({ hasEndDeadline: on })
  },
  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value || '' })
  },
  onEndTimeChange(e) {
    this.setData({ endTime: e.detail.value || '' })
  },
  onInput(e) {
    const v = (e.detail.value || '').replace(/\D/g, '').slice(0, 5)
    this.setData({ checkinCode: v })
  },
  async submit() {
    const code = (this.data.checkinCode || '').trim()
    if (!code || code.length < 4) {
      wx.showToast({ title: '请输入4～5位签到码', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      await request(`/api/student/checkin/by-code?code=${encodeURIComponent(code)}&source=CLICK`, 'POST', {})
      wx.showToast({ title: '签到成功', icon: 'success' })
      this.setData({ checkinCode: '' })
      this.loadHistory()
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '签到失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },
  async loadHistory() {
    if (this.data.isTeacher || !this.data.courseId) return
    this.setData({ historyLoading: true })
    try {
      const list = await request(`/api/student/checkins?courseId=${encodeURIComponent(String(this.data.courseId))}`, 'GET')
      this.setData({ historyList: Array.isArray(list) ? list : [] })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载签到记录失败', icon: 'none' })
    } finally {
      this.setData({ historyLoading: false })
    }
  },
  onCreateTitleInput(e) {
    this.setData({ 'createForm.title': (e.detail.value || '').trim() })
  },
  async createCheckin() {
    if (!this.data.isTeacher || !this.data.courseId) return
    const f = this.data.createForm || {}
    if (!f.title) {
      wx.showToast({ title: '请输入签到标题', icon: 'none' })
      return
    }
    let endAtStr = ''
    if (this.data.hasEndDeadline) {
      const endAt = toDateTimePayload(this.data.endDate, this.data.endTime)
      if (!endAt) {
        wx.showToast({ title: '请完整选择截止的日期、时间', icon: 'none' })
        return
      }
      const endMs = new Date(endAt.replace(/-/g, '/')).getTime()
      if (!Number.isFinite(endMs) || endMs <= Date.now()) {
        wx.showToast({ title: '截止时间须晚于当前时间', icon: 'none' })
        return
      }
      endAtStr = endAt
    }
    const params = []
    params.push(`title=${encodeURIComponent(f.title)}`)
    if (endAtStr) {
      params.push(`endAt=${encodeURIComponent(endAtStr)}`)
    }
    this.setData({ creating: true })
    try {
      const path = `/api/teacher/courses/${this.data.courseId}/checkins?` + params.join('&')
      const c = await request(path, 'POST', {})
      const tip = c && c.checkinCode ? `签到码：${c.checkinCode}` : '创建成功'
      wx.showToast({ title: tip, icon: 'none' })
      this.setData({
        creating: false,
        createForm: { title: '' },
        hasEndDeadline: false
      })
      this.resetEndPicker()
      await this.loadCheckins()
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '创建失败', icon: 'none' })
      this.setData({ creating: false })
    }
  },
  async loadCheckins() {
    if (!this.data.isTeacher || !this.data.courseId) return
    this.setData({ loadingList: true })
    try {
      const list = await request(`/api/teacher/courses/${this.data.courseId}/checkins`, 'GET')
      this.setData({ checkins: Array.isArray(list) ? list : [] })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载签到列表失败', icon: 'none' })
    } finally {
      this.setData({ loadingList: false })
    }
  },
  async closeCheckin(e) {
    if (!this.data.isTeacher || !this.data.courseId) return
    const id = e && e.currentTarget ? e.currentTarget.dataset.id : null
    if (!id) return
    wx.showModal({
      title: '结束签到',
      content: '确认立即结束该签到活动？',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request(`/api/teacher/courses/${this.data.courseId}/checkins/${id}/close`, 'POST', {})
          wx.showToast({ title: '已结束', icon: 'success' })
          await this.loadCheckins()
        } catch (err) {
          wx.showToast({ title: (err && err.message) ? err.message : '操作失败', icon: 'none' })
        }
      }
    })
  },
  goStatsPage(e) {
    if (!this.data.isTeacher || !this.data.courseId) return
    const id = e && e.currentTarget ? e.currentTarget.dataset.id : null
    if (!id) return
    wx.navigateTo({
      url: `/pages/course/checkin-stats/checkin-stats?courseId=${encodeURIComponent(String(this.data.courseId))}&checkinId=${encodeURIComponent(String(id))}`
    })
  }
})

