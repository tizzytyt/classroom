const { request } = require('../../../utils/request')

Page({
  data: {
    courseId: '',
    paperId: '',
    paperTitle: '',
    list: [],
    loading: false
  },
  onLoad(options) {
    const cid = options.courseId != null ? String(options.courseId) : ''
    const pid = options.paperId != null ? String(options.paperId) : ''
    const title = options.paperTitle ? decodeURIComponent(options.paperTitle) : ''
    if (!cid || !pid) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      return
    }
    this.setData({ courseId: cid, paperId: pid, paperTitle: title })
    if (title) wx.setNavigationBarTitle({ title: '答卷 · ' + (title.length > 8 ? title.slice(0, 8) + '…' : title) })
    this.loadList()
  },
  async loadList() {
    this.setData({ loading: true })
    try {
      const raw = await request(
        `/api/teacher/courses/${this.data.courseId}/exams/${encodeURIComponent(this.data.paperId)}/attempts`,
        'GET'
      )
      const arr = Array.isArray(raw) ? raw : []
      this.setData({
        list: arr.map((x) => ({
          ...x,
          attemptId: x.attemptId != null ? String(x.attemptId) : ''
        }))
      })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  openAttempt(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/course/exam-attempt-grade/exam-attempt-grade?courseId=${encodeURIComponent(this.data.courseId)}&paperId=${encodeURIComponent(this.data.paperId)}&attemptId=${encodeURIComponent(id)}`
    })
  }
})
