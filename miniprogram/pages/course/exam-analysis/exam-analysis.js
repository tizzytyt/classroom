const { request } = require('../../../utils/request')

Page({
  data: {
    courseId: '',
    paperId: '',
    paperTitle: '',
    loading: false,
    questions: [],
    wrongRank: []
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
    this.loadAnalysis()
  },
  async loadAnalysis() {
    this.setData({ loading: true })
    try {
      const res = await request(
        `/api/teacher/courses/${this.data.courseId}/exams/${encodeURIComponent(this.data.paperId)}/analysis`,
        'GET'
      )
      const qs = res && Array.isArray(res.questions) ? res.questions : []
      const wr = res && Array.isArray(res.wrongRank) ? res.wrongRank : []
      this.setData({ questions: qs, wrongRank: wr })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})

