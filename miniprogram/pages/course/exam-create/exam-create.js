const { request, postJson } = require('../../../utils/request')
const { buildPayloadFromNewQ, qTypeLabel, payloadToNewQ } = require('../../../utils/examQuestionPayload')

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

function defaultTimeRange() {
  const start = new Date()
  const end = new Date(start.getTime() + 2 * 60 * 60 * 1000)
  return {
    startDate: formatDate(start),
    startTime: formatTime(start),
    endDate: formatDate(end),
    endTime: formatTime(end)
  }
}

Page({
  data: {
    courseId: '',
    creating: false,
    createForm: {
      title: '',
      durationMinutes: '',
      shuffleQuestions: false
    },
    startDate: '',
    startTime: '',
    endDate: '',
    endTime: '',
    draftQuestions: [],
    editingDraftIndex: -1,
    newQ: {
      qType: 1,
      stem: '',
      optA: '',
      optB: '',
      optC: '',
      optD: '',
      correctAnswer: 'A',
      score: '5',
      sortNo: '1'
    },
    correctChoices: ['A', 'B', 'C', 'D'],
    correctIndex: 0,
    tfLabels: ['正确 (T)', '错误 (F)'],
    tfIndex: 0
  },
  resetTimePickers() {
    const r = defaultTimeRange()
    this.setData({
      startDate: r.startDate,
      startTime: r.startTime,
      endDate: r.endDate,
      endTime: r.endTime
    })
  },
  onLoad(options) {
    const cid = options.courseId != null ? String(options.courseId) : ''
    if (!cid) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    this.setData({ courseId: cid })
    this.resetTimePickers()
  },
  onTitleInput(e) {
    this.setData({ 'createForm.title': (e.detail.value || '').trim() })
  },
  onDurationInput(e) {
    this.setData({ 'createForm.durationMinutes': (e.detail.value || '').trim() })
  },
  onShuffleChange(e) {
    this.setData({ 'createForm.shuffleQuestions': !!(e && e.detail && e.detail.value) })
  },
  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value || '' })
  },
  onStartTimeChange(e) {
    this.setData({ startTime: e.detail.value || '' })
  },
  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value || '' })
  },
  onEndTimeChange(e) {
    this.setData({ endTime: e.detail.value || '' })
  },
  setQType(e) {
    const t = Number(e.currentTarget.dataset.type)
    if (t === 3) {
      this.setData({
        'newQ.qType': 3,
        tfIndex: 0,
        'newQ.correctAnswer': 'T'
      })
    } else {
      this.setData({
        'newQ.qType': 1,
        correctIndex: 0,
        'newQ.correctAnswer': 'A'
      })
    }
  },
  onStemInput(e) {
    this.setData({ 'newQ.stem': e.detail.value || '' })
  },
  onOptA(e) {
    this.setData({ 'newQ.optA': e.detail.value || '' })
  },
  onOptB(e) {
    this.setData({ 'newQ.optB': e.detail.value || '' })
  },
  onOptC(e) {
    this.setData({ 'newQ.optC': e.detail.value || '' })
  },
  onOptD(e) {
    this.setData({ 'newQ.optD': e.detail.value || '' })
  },
  onScoreInput(e) {
    this.setData({ 'newQ.score': (e.detail.value || '').trim() })
  },
  onCorrectPick(e) {
    const i = Number(e.detail.value)
    const key = this.data.correctChoices[i]
    this.setData({ correctIndex: i, 'newQ.correctAnswer': key })
  },
  onTfPick(e) {
    const i = Number(e.detail.value)
    const v = i === 1 ? 'F' : 'T'
    this.setData({ tfIndex: i, 'newQ.correctAnswer': v })
  },
  saveDraftQuestion() {
    const built = buildPayloadFromNewQ(this.data.newQ)
    if (!built.ok) {
      wx.showToast({ title: built.msg, icon: 'none' })
      return
    }
    const p = built.payload
    const stem = p.stem || ''
    const previewStem = stem.length > 28 ? `${stem.slice(0, 28)}…` : stem
    const idx = this.data.editingDraftIndex
    const prev = this.data.draftQuestions || []
    let nextList
    let draftId
    if (idx >= 0 && idx < prev.length) {
      draftId = prev[idx].draftId
      nextList = prev.map((row, i) =>
        i === idx
          ? {
              draftId,
              payload: p,
              typeLabel: qTypeLabel(p.qType),
              score: p.score,
              previewStem
            }
          : row
      )
    } else {
      draftId = `d${Date.now()}_${Math.random().toString(36).slice(2, 8)}`
      nextList = [
        ...prev,
        {
          draftId,
          payload: p,
          typeLabel: qTypeLabel(p.qType),
          score: p.score,
          previewStem
        }
      ]
    }
    const keepType = this.data.newQ.qType
    this.setData({
      draftQuestions: nextList,
      editingDraftIndex: -1,
      newQ: {
        qType: keepType,
        stem: '',
        optA: '',
        optB: '',
        optC: '',
        optD: '',
        correctAnswer: keepType === 3 ? 'T' : 'A',
        score: '5',
        sortNo: String(nextList.length + 1)
      },
      correctIndex: 0,
      tfIndex: 0
    })
    wx.showToast({ title: idx >= 0 ? '已更新' : '已加入列表', icon: 'success' })
  },
  cancelEditDraft() {
    const n = this.data.draftQuestions.length
    this.setData({
      editingDraftIndex: -1,
      newQ: {
        qType: 1,
        stem: '',
        optA: '',
        optB: '',
        optC: '',
        optD: '',
        correctAnswer: 'A',
        score: '5',
        sortNo: String(n + 1)
      },
      correctIndex: 0,
      tfIndex: 0
    })
  },
  editDraftQuestion(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    const row = (this.data.draftQuestions || [])[idx]
    if (!row || !row.payload) return
    const { newQ, correctIndex, tfIndex } = payloadToNewQ(row.payload)
    this.setData({ newQ, correctIndex, tfIndex, editingDraftIndex: idx })
  },
  removeDraftQuestion(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    const prev = this.data.draftQuestions || []
    if (idx < 0 || idx >= prev.length) return
    const nextList = prev.filter((_, i) => i !== idx)
    let editingDraftIndex = this.data.editingDraftIndex
    if (editingDraftIndex === idx) editingDraftIndex = -1
    else if (editingDraftIndex > idx) editingDraftIndex -= 1
    this.setData({
      draftQuestions: nextList,
      editingDraftIndex,
      'newQ.sortNo': String(nextList.length + 1)
    })
  },
  async createPaper() {
    const f = this.data.createForm || {}
    if (!(f.title || '').trim()) {
      wx.showToast({ title: '请填写试卷标题', icon: 'none' })
      return
    }
    const { startDate, startTime, endDate, endTime } = this.data
    const startAt = toDateTimePayload(startDate, startTime)
    const endAt = toDateTimePayload(endDate, endTime)
    if (!startAt || !endAt) {
      wx.showToast({ title: '请完整选择开放开始与结束的日期、时间', icon: 'none' })
      return
    }
    const startMs = new Date(startAt.replace(/-/g, '/')).getTime()
    const endMs = new Date(endAt.replace(/-/g, '/')).getTime()
    if (!Number.isFinite(startMs) || !Number.isFinite(endMs) || endMs <= startMs) {
      wx.showToast({ title: '结束时间必须晚于开始时间', icon: 'none' })
      return
    }
    const dm = (f.durationMinutes || '').trim()
    const payload = {
      title: f.title.trim(),
      durationMinutes: dm ? Number(dm) : null,
      startAt,
      endAt,
      shuffleQuestions: !!f.shuffleQuestions
    }
    const drafts = this.data.draftQuestions || []
    const rawQuestions = drafts.map((r) => (r && r.payload) ? r.payload : null)
    if (drafts.length && rawQuestions.some((q) => !q)) {
      wx.showToast({ title: '题目列表中存在异常项，请删除后重新添加', icon: 'none' })
      return
    }
    const questions = rawQuestions.map((q, idx) => {
      const qTypeNum = Number(q.qType)
      if (!Number.isFinite(qTypeNum)) {
        throw new Error(`第${idx + 1}题题型无效`)
      }
      const scoreNum = q.score == null ? 0 : Number(q.score)
      const sortNum = q.sortNo == null ? 0 : Number(q.sortNo)
      return {
        ...q,
        qType: qTypeNum,
        score: Number.isFinite(scoreNum) ? scoreNum : 0,
        sortNo: Number.isFinite(sortNum) ? sortNum : 0,
        options: Array.isArray(q.options) ? q.options : []
      }
    })
    this.setData({ creating: true })
    try {
      const paper = await postJson(`/api/teacher/courses/${this.data.courseId}/exams/with-questions`, {
        ...payload,
        questions
      })
      const pid = paper && paper.id != null ? String(paper.id) : ''
      if (!pid) {
        throw new Error('未返回试卷ID')
      }
      wx.showToast({
        title: drafts.length ? `创建成功，已写入${drafts.length}道题` : '创建成功',
        icon: 'success'
      })
      wx.navigateTo({
        url: `/pages/course/exam-detail/exam-detail?courseId=${encodeURIComponent(this.data.courseId)}&paperId=${encodeURIComponent(pid)}`
      })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '创建失败', icon: 'none' })
      this.setData({ creating: false })
    }
  }
})

