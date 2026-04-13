const { request, putJson } = require('../../../utils/request')
const { qTypeLabel } = require('../../../utils/examQuestionPayload')

Page({
  data: {
    courseId: '',
    paperId: '',
    attemptId: '',
    detail: null,
    rows: [],
    saving: false
  },
  onLoad(options) {
    const cid = options.courseId != null ? String(options.courseId) : ''
    const pid = options.paperId != null ? String(options.paperId) : ''
    const aid = options.attemptId != null ? String(options.attemptId) : ''
    if (!cid || !pid || !aid) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      return
    }
    this.setData({ courseId: cid, paperId: pid, attemptId: aid })
    this.loadDetail()
  },
  applyDetail(d) {
    const qs = (d && d.questions) ? d.questions : []
    const rows = qs.map((q) => ({
      ...q,
      questionId: q.questionId != null ? String(q.questionId) : '',
      typeLabel: qTypeLabel(q.qType),
      scoreInput:
        q.earnedScore != null && q.earnedScore !== ''
          ? String(q.earnedScore)
          : ''
    }))
    this.setData({ detail: d || null, rows })
  },
  async loadDetail() {
    try {
      const d = await request(
        `/api/teacher/courses/${this.data.courseId}/exams/${encodeURIComponent(this.data.paperId)}/attempts/${encodeURIComponent(this.data.attemptId)}`,
        'GET'
      )
      this.applyDetail(d || {})
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    }
  },
  onScoreInput(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    if (Number.isNaN(idx)) return
    this.setData({ [`rows[${idx}].scoreInput`]: e.detail.value || '' })
  },
  async saveGrades() {
    const rows = this.data.rows || []
    const items = []
    for (const r of rows) {
      const qid = r.questionId
      if (!qid) continue
      const raw = (r.scoreInput || '').trim()
      const sc = raw === '' ? 0 : Number(raw)
      if (!Number.isFinite(sc) || sc < 0) {
        wx.showToast({ title: '得分需为非负数字', icon: 'none' })
        return
      }
      items.push({ questionId: qid, score: sc })
    }
    if (!items.length) {
      wx.showToast({ title: '没有可保存的题目', icon: 'none' })
      return
    }
    this.setData({ saving: true })
    try {
      const d = await putJson(
        `/api/teacher/courses/${this.data.courseId}/exams/${encodeURIComponent(this.data.paperId)}/attempts/${encodeURIComponent(this.data.attemptId)}/grades`,
        { items }
      )
      this.applyDetail(d || {})
      wx.showToast({ title: '已保存', icon: 'success' })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '保存失败', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  }
})
