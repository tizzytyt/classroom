const { request, postJson } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')
const { buildPayloadFromNewQ, qTypeLabel } = require('../../../utils/examQuestionPayload')
const { baseURL } = require('../../../utils/config')

const DEBUG_STUDENT_EXAM = true
let __printedStudentExamOnce = false

function statusText(s) {
  if (s === 1) return '已发布'
  if (s === 2) return '已下线'
  return '草稿'
}

function buildStudentQuestionRows(questions) {
  return (questions || []).map((q) => {
    // 兼容后端不同字段名：qType / type / questionType / qtype ...
    const rawType =
      (q && (q.qType != null ? q.qType : undefined)) ??
      (q && (q.type != null ? q.type : undefined)) ??
      (q && (q.questionType != null ? q.questionType : undefined)) ??
      (q && (q.qtype != null ? q.qtype : undefined)) ??
      (q && (q.question_type != null ? q.question_type : undefined))
    const qt = Number(rawType)

    // 兼容选项字段名与结构：options / choices / choiceList ...
    const rawOpts =
      (q && Array.isArray(q.options) ? q.options : null) ||
      (q && Array.isArray(q.choices) ? q.choices : null) ||
      (q && Array.isArray(q.choiceList) ? q.choiceList : null) ||
      (q && Array.isArray(q.optionList) ? q.optionList : null) ||
      []
    const opts = rawOpts
      .map((o) => {
        if (!o) return null
        const key = o.key != null ? o.key : (o.value != null ? o.value : (o.label != null ? o.label : ''))
        const text = o.text != null ? o.text : (o.name != null ? o.name : (o.title != null ? o.title : (o.content != null ? o.content : '')))
        return { key: key != null ? String(key).toUpperCase() : '', text: text != null ? String(text) : '' }
      })
      .filter((o) => o && o.key)

    const choiceLabels =
      qt === 1 && opts.length ? opts.map((o) => `${o.key}. ${o.text || ''}`) : []
    return {
      ...q,
      // 模板里使用严格等于判断（===），这里把题型规范成 number
      qType: Number.isFinite(qt) ? qt : (q ? q.qType : undefined),
      idStr: q.id != null ? String(q.id) : '',
      typeLabel: qTypeLabel(Number.isFinite(qt) ? qt : (q ? q.qType : undefined)),
      // 统一 options，供单选/多选渲染
      options: opts,
      choiceLabels,
      pickIndex: 0, // 旧 picker 兼容字段
      tfPickIndex: 0,
      multiAnswer: '',
      textAnswer: '',
      blankAnswer: '',
      genericAnswer: '',
      // 新：选项式作答
      singleKey: '',
      multiKeys: [],
      tfKey: 'T'
    }
  })
}

function fnv1a32(str) {
  let h = 0x811c9dc5
  const s = String(str == null ? '' : str)
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i)
    // 32-bit FNV-1a: h *= 16777619
    h = (h + ((h << 1) + (h << 4) + (h << 7) + (h << 8) + (h << 24))) >>> 0
  }
  return h >>> 0
}

function mulberry32(seed) {
  let a = seed >>> 0
  return function () {
    a |= 0
    a = (a + 0x6d2b79f5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

function shuffledCopy(arr, seedStr) {
  const a = Array.isArray(arr) ? arr.slice() : []
  if (a.length <= 1) return a
  const rand = mulberry32(fnv1a32(seedStr))
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(rand() * (i + 1))
    const tmp = a[i]
    a[i] = a[j]
    a[j] = tmp
  }
  return a
}

Page({
  data: {
    courseId: '',
    paperId: '',
    isTeacher: false,
    detail: null,
    questions: [],
    statusText: '',
    isDraft: false,
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
    tfIndex: 0,
    adding: false,
    publishing: false,
    studentDetail: null,
    studentQs: [],
    attemptId: '',
    examStarted: false,
    examSubmitted: false,
    startingExam: false,
    submittingExam: false,
    examScoreResult: null,
    stTfLabels: ['正确 (T)', '错误 (F)']
  },
  onLoad(options) {
    const cid = options.courseId != null ? String(options.courseId) : ''
    const pid = options.paperId != null ? String(options.paperId) : ''
    const user = getUser()
    if (!cid || !pid) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      return
    }
    const isTeacher = !!(user && user.roleCode === 'TEACHER')
    this.setData({ courseId: cid, paperId: pid, isTeacher })
    wx.setNavigationBarTitle({ title: isTeacher ? '试卷详情' : '在线考试' })
  },
  onShow() {
    if (!(this.data.courseId && this.data.paperId)) return
    if (this.data.isTeacher) {
      this.loadDetail()
    } else {
      this.loadStudentExam()
    }
  },
  applyDetail(d) {
    const qs = (d.questions || []).map((q) => ({
      ...q,
      typeLabel: qTypeLabel(q.qType)
    }))
    const isDraft = d.status === 0
    this.setData({
      detail: d,
      questions: qs,
      statusText: statusText(d.status),
      isDraft,
      'newQ.sortNo': String(qs.length + 1)
    })
  },
  async loadDetail() {
    try {
      const d = await request(
        `/api/teacher/courses/${this.data.courseId}/exams/${encodeURIComponent(this.data.paperId)}`,
        'GET'
      )
      this.applyDetail(d || {})
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    }
  },
  async loadStudentExam() {
    try {
      const d = await request(
        `/api/student/exams/${encodeURIComponent(this.data.paperId)}?courseId=${encodeURIComponent(this.data.courseId)}`,
        'GET'
      )
      if (DEBUG_STUDENT_EXAM && !__printedStudentExamOnce) {
        __printedStudentExamOnce = true
        const qs = (d && d.questions) || []
        try {
          console.log('[studentExam] raw detail keys:', d ? Object.keys(d) : d)
          console.log('[studentExam] questions length:', Array.isArray(qs) ? qs.length : 'not-array')
          console.log(
            '[studentExam] qType snapshot:',
            (Array.isArray(qs) ? qs.slice(0, 10) : []).map((q) => ({
              id: q && q.id,
              qType: q && q.qType,
              qTypeTypeof: typeof (q && q.qType),
              type: q && q.type,
              questionType: q && q.questionType
            }))
          )
        } catch (e) {
          console.log('[studentExam] debug print failed:', e)
        }
      }
      const studentQs = buildStudentQuestionRows((d && d.questions) || [])
      if (DEBUG_STUDENT_EXAM && __printedStudentExamOnce) {
        try {
          console.log(
            '[studentExam] normalized qType snapshot:',
            (studentQs || []).slice(0, 10).map((q) => ({
              idStr: q && q.idStr,
              qType: q && q.qType,
              qTypeTypeof: typeof (q && q.qType),
              typeLabel: q && q.typeLabel
            }))
          )
        } catch (e) {}
      }
      this.setData({
        studentDetail: d || null,
        studentQs,
        examStarted: false,
        examSubmitted: false,
        attemptId: '',
        examScoreResult: null
      })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    }
  },
  async startStudentExam() {
    this.setData({ startingExam: true })
    try {
      const res = await request(
        `/api/student/exams/${encodeURIComponent(this.data.paperId)}/start?courseId=${encodeURIComponent(this.data.courseId)}`,
        'POST',
        {}
      )
      const aid = res && res.attemptId
      if (aid == null) throw new Error('未返回答卷ID')
      const attemptId = String(aid)
      let nextQs = this.data.studentQs || []
      const shuffleOn = !!(this.data.studentDetail && this.data.studentDetail.shuffleQuestions)
      if (shuffleOn) {
        // 使用 attemptId 作为种子：不同学生/不同次作答顺序不同；同一次答题顺序稳定
        nextQs = shuffledCopy(nextQs, `attempt:${attemptId}`)
      }
      // 将整套答题数据保存到本地（避免 URL 传大量数据）
      try {
        wx.setStorageSync(`exam_attempt_${attemptId}`, {
          courseId: this.data.courseId,
          paperId: this.data.paperId,
          attemptId,
          detail: this.data.studentDetail,
          questions: nextQs,
          answers: {}
        })
      } catch (e) {}
      this.setData({ attemptId, examStarted: true, studentQs: nextQs })
      wx.navigateTo({
        url: `/pages/course/exam-do/exam-do?courseId=${encodeURIComponent(this.data.courseId)}&paperId=${encodeURIComponent(this.data.paperId)}&attemptId=${encodeURIComponent(attemptId)}&i=0`
      })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '开始失败', icon: 'none' })
    } finally {
      this.setData({ startingExam: false })
    }
  },
  onStChoicePick(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    const v = Number(e.detail.value)
    this.setData({ [`studentQs[${idx}].pickIndex`]: v })
  },
  onStSingleChange(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    const key = e.detail.value != null ? String(e.detail.value) : ''
    this.setData({ [`studentQs[${idx}].singleKey`]: key })
  },
  onStTfPick(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    const v = Number(e.detail.value)
    this.setData({ [`studentQs[${idx}].tfPickIndex`]: v })
  },
  onStTfRadioChange(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    const key = e.detail.value != null ? String(e.detail.value) : 'T'
    this.setData({ [`studentQs[${idx}].tfKey`]: key === 'F' ? 'F' : 'T' })
  },
  onStMultiInput(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    this.setData({ [`studentQs[${idx}].multiAnswer`]: e.detail.value || '' })
  },
  onStMultiChange(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    const vs = Array.isArray(e.detail.value) ? e.detail.value : []
    // 保持 A,B,C... 顺序
    const ordered = vs
      .map((x) => String(x).toUpperCase())
      .filter((x) => x)
      .sort()
    this.setData({ [`studentQs[${idx}].multiKeys`]: ordered })
  },
  onStTextInput(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    this.setData({ [`studentQs[${idx}].textAnswer`]: e.detail.value || '' })
  },
  onStBlankInput(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    this.setData({ [`studentQs[${idx}].blankAnswer`]: e.detail.value || '' })
  },
  onStGenericInput(e) {
    const idx = Number(e.currentTarget.dataset.idx)
    this.setData({ [`studentQs[${idx}].genericAnswer`]: e.detail.value || '' })
  },
  collectStudentAnswers() {
    const qs = this.data.studentQs || []
    const answers = []
    for (let i = 0; i < qs.length; i++) {
      const q = qs[i]
      const qt = Number(q.qType)
      let ans = ''
      if (qt === 1) {
        // 优先使用选项式作答；若未选则兼容旧 picker
        ans = (q.singleKey || '').trim()
        if (!ans) {
          const opts = q.options || []
          const pi = q.pickIndex != null ? Number(q.pickIndex) : 0
          const opt = opts[pi]
          ans = opt && opt.key ? String(opt.key) : ''
        }
      } else if (qt === 3) {
        // 优先 radio；否则兼容旧 picker
        ans = (q.tfKey || '').trim()
        if (!ans) ans = Number(q.tfPickIndex) === 1 ? 'F' : 'T'
      } else if (qt === 2) {
        // 优先 checkbox；否则兼容手输 A,C
        if (Array.isArray(q.multiKeys) && q.multiKeys.length) {
          ans = q.multiKeys.join(',')
        } else {
          ans = (q.multiAnswer || '').trim()
        }
      } else if (qt === 5) {
        ans = (q.textAnswer || '').trim()
      } else if (qt === 4) {
        ans = (q.blankAnswer || '').trim()
      } else {
        ans = (q.genericAnswer || '').trim()
      }
      const qid = q.id
      if (qid != null && qid !== '') {
        answers.push({ questionId: qid, answer: ans })
      }
    }
    return answers
  },
  async submitStudentExam() {
    if (!this.data.examStarted || !this.data.attemptId) {
      wx.showToast({ title: '请先开始答题', icon: 'none' })
      return
    }
    if (this.data.examSubmitted) return
    const answers = this.collectStudentAnswers()
    this.setData({ submittingExam: true })
    try {
      const res = await postJson(
        `/api/student/exams/${encodeURIComponent(this.data.paperId)}/attempts/${encodeURIComponent(this.data.attemptId)}/submit?courseId=${encodeURIComponent(this.data.courseId)}`,
        { answers }
      )
      this.setData({ examSubmitted: true, examScoreResult: res || null })
      wx.showToast({ title: '已提交', icon: 'success' })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '提交失败', icon: 'none' })
    } finally {
      this.setData({ submittingExam: false })
    }
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
  onSortInput(e) {
    this.setData({ 'newQ.sortNo': (e.detail.value || '').trim() })
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
  async addQuestion() {
    if (!this.data.isDraft) return
    const n = this.data.newQ
    const built = buildPayloadFromNewQ(n)
    if (!built.ok) {
      wx.showToast({ title: built.msg, icon: 'none' })
      return
    }
    const payload = built.payload
    this.setData({ adding: true })
    try {
      await postJson(
        `/api/teacher/courses/${this.data.courseId}/exams/${encodeURIComponent(this.data.paperId)}/questions`,
        payload
      )
      wx.showToast({ title: '已添加', icon: 'success' })
      this.setData({
        adding: false,
        newQ: {
          qType: n.qType,
          stem: '',
          optA: '',
          optB: '',
          optC: '',
          optD: '',
          correctAnswer: n.qType === 3 ? 'T' : 'A',
          score: String(n.score || '5'),
          sortNo: '1'
        },
        correctIndex: 0,
        tfIndex: 0
      })
      await this.loadDetail()
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '添加失败', icon: 'none' })
      this.setData({ adding: false })
    }
  },
  publishPaper() {
    if (!this.data.isDraft) return
    const n = this.data.questions.length
    wx.showModal({
      title: '发布试卷',
      content: n ? `共 ${n} 道题，发布后学生可见，确定发布？` : '尚未添加题目，确定仍要发布吗？',
      success: async (res) => {
        if (!res.confirm) return
        this.setData({ publishing: true })
        try {
          await request(
            `/api/teacher/courses/${this.data.courseId}/exams/${encodeURIComponent(this.data.paperId)}/publish`,
            'POST',
            {}
          )
          wx.showToast({ title: '已发布', icon: 'success' })
          await this.loadDetail()
        } catch (e) {
          wx.showToast({ title: (e && e.message) ? e.message : '发布失败', icon: 'none' })
        } finally {
          this.setData({ publishing: false })
        }
      }
    })
  },
  goGradeAttempts() {
    if (!this.data.isTeacher || !this.data.courseId || !this.data.paperId) return
    const t = (this.data.detail && this.data.detail.title) ? String(this.data.detail.title) : ''
    const q = t ? `&paperTitle=${encodeURIComponent(t)}` : ''
    wx.navigateTo({
      url: `/pages/course/exam-grading/exam-grading?courseId=${encodeURIComponent(this.data.courseId)}&paperId=${encodeURIComponent(this.data.paperId)}${q}`
    })
  }
  ,
  goExamStats() {
    if (!this.data.isTeacher || !this.data.courseId || !this.data.paperId) return
    const t = (this.data.detail && this.data.detail.title) ? String(this.data.detail.title) : ''
    const q = t ? `&paperTitle=${encodeURIComponent(t)}` : ''
    wx.navigateTo({
      url: `/pages/course/exam-stats/exam-stats?courseId=${encodeURIComponent(this.data.courseId)}&paperId=${encodeURIComponent(this.data.paperId)}${q}`
    })
  }
  ,
  goExamAnalysis() {
    if (!this.data.isTeacher || !this.data.courseId || !this.data.paperId) return
    const t = (this.data.detail && this.data.detail.title) ? String(this.data.detail.title) : ''
    const q = t ? `&paperTitle=${encodeURIComponent(t)}` : ''
    wx.navigateTo({
      url: `/pages/course/exam-analysis/exam-analysis?courseId=${encodeURIComponent(this.data.courseId)}&paperId=${encodeURIComponent(this.data.paperId)}${q}`
    })
  }
  ,
  exportScores() {
    if (!this.data.isTeacher || !this.data.courseId || !this.data.paperId) return
    const token = wx.getStorageSync('token') || ''
    const url = `${baseURL}/api/teacher/courses/${encodeURIComponent(this.data.courseId)}/exams/${encodeURIComponent(this.data.paperId)}/export-scores`
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
