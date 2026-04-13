const { postJson } = require('../../../utils/request')
const { baseURL } = require('../../../utils/config')

function storageKey(attemptId) {
  return `exam_attempt_${String(attemptId)}`
}

function safeUpper(s) {
  return String(s == null ? '' : s).trim().toUpperCase()
}

function parseMultiKeys(ans) {
  const raw = safeUpper(ans)
  if (!raw) return []
  const parts = raw.replace(' ', '').split(/[,，;；/|]+/)
  const set = {}
  parts.forEach((p) => {
    const t = safeUpper(p)
    if (t) set[t] = true
  })
  return Object.keys(set).sort()
}

function getSaConfig(q) {
  const keys = Array.isArray(q && q.options)
    ? q.options.map((o) => String(o && o.key ? o.key : '').toUpperCase())
    : []
  const allowText = keys.includes('TEXT') || keys.length === 0
  const allowImage = keys.includes('IMAGE')
  const allowFile = keys.includes('FILE')
  return { allowText, allowImage, allowFile }
}

Page({
  data: {
    courseId: '',
    paperId: '',
    attemptId: '',
    detail: null,
    questions: [],
    index: 0,
    total: 0,
    q: null,
    // 当前题答案（用于表单控件绑定）
    answerSingleKey: '',
    answerMultiKeys: [],
    answerTfKey: 'T',
    answerText: '',
    answerSaFiles: [],
    saAllowText: true,
    saAllowImage: false,
    saAllowFile: false,
    saUploading: false,
    submitting: false
  },
  onLoad(options) {
    const cid = options.courseId != null ? String(options.courseId) : ''
    const pid = options.paperId != null ? String(options.paperId) : ''
    const aid = options.attemptId != null ? String(options.attemptId) : ''
    const i = options.i != null ? Number(options.i) : 0
    if (!cid || !pid || !aid) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      return
    }
    this.setData({ courseId: cid, paperId: pid, attemptId: aid, index: Number.isFinite(i) ? i : 0 })
    this.loadFromStorage()
  },
  loadFromStorage() {
    const key = storageKey(this.data.attemptId)
    const pack = wx.getStorageSync(key) || null
    if (!pack || !Array.isArray(pack.questions)) {
      wx.showToast({ title: '答题数据丢失，请返回重试', icon: 'none' })
      return
    }
    const idx = Math.max(0, Math.min(pack.questions.length - 1, Number(this.data.index) || 0))
    this.setData({
      detail: pack.detail || null,
      questions: pack.questions,
      total: pack.questions.length,
      index: idx
    })
    this.applyQuestion()
  },
  persistPack(nextPack) {
    const key = storageKey(this.data.attemptId)
    wx.setStorageSync(key, nextPack)
  },
  getPack() {
    const key = storageKey(this.data.attemptId)
    return wx.getStorageSync(key) || null
  },
  applyQuestion() {
    const qs = this.data.questions || []
    const idx = Number(this.data.index) || 0
    const q = qs[idx] || null
    const pack = this.getPack() || {}
    const answers = pack.answers || {}
    const qid = q && q.id != null ? String(q.id) : ''
    const saved = qid && answers[qid] != null ? String(answers[qid]) : ''
    const qt = q ? Number(q.qType) : NaN

    let answerSingleKey = ''
    let answerTfKey = 'T'
    let answerMultiKeys = []
    let answerText = ''
    let answerSaFiles = []
    if (qt === 1) {
      answerSingleKey = safeUpper(saved)
    } else if (qt === 3) {
      answerTfKey = safeUpper(saved) === 'F' ? 'F' : 'T'
    } else if (qt === 2) {
      answerMultiKeys = parseMultiKeys(saved)
    } else if (qt === 5) {
      if (saved) {
        try {
          const obj = JSON.parse(saved)
          answerText = obj && obj.text ? String(obj.text) : ''
          answerSaFiles = Array.isArray(obj && obj.files) ? obj.files : []
        } catch (e) {
          answerText = saved
          answerSaFiles = []
        }
      }
    } else {
      answerText = saved
    }
    const saCfg = getSaConfig(q)
    this.setData({
      q,
      answerSingleKey,
      answerTfKey,
      answerMultiKeys,
      answerText,
      answerSaFiles,
      saAllowText: !!saCfg.allowText,
      saAllowImage: !!saCfg.allowImage,
      saAllowFile: !!saCfg.allowFile
    })
  },
  saveCurrentAnswer() {
    const q = this.data.q
    if (!q) return
    const qt = Number(q.qType)
    const qid = q.id != null ? String(q.id) : ''
    if (!qid) return

    let ans = ''
    if (qt === 1) {
      ans = safeUpper(this.data.answerSingleKey)
    } else if (qt === 3) {
      ans = this.data.answerTfKey === 'F' ? 'F' : 'T'
    } else if (qt === 2) {
      ans = Array.isArray(this.data.answerMultiKeys) ? this.data.answerMultiKeys.join(',') : ''
    } else if (qt === 5) {
      const cfg = getSaConfig(q)
      const text = cfg.allowText ? String(this.data.answerText || '').trim() : ''
      const files = Array.isArray(this.data.answerSaFiles) ? this.data.answerSaFiles : []
      ans = JSON.stringify({ text, files })
    } else {
      ans = String(this.data.answerText || '').trim()
    }

    const pack = this.getPack() || {}
    const nextPack = {
      ...pack,
      answers: {
        ...(pack.answers || {}),
        [qid]: ans
      }
    }
    this.persistPack(nextPack)
  },
  onSingleChange(e) {
    this.setData({ answerSingleKey: e.detail.value != null ? String(e.detail.value) : '' })
  },
  onTfChange(e) {
    const v = e.detail.value != null ? String(e.detail.value) : 'T'
    this.setData({ answerTfKey: v === 'F' ? 'F' : 'T' })
  },
  onMultiChange(e) {
    const vs = Array.isArray(e.detail.value) ? e.detail.value : []
    const ordered = vs.map((x) => safeUpper(x)).filter((x) => x).sort()
    this.setData({ answerMultiKeys: ordered })
  },
  onTextInput(e) {
    this.setData({ answerText: e.detail.value || '' })
  },
  chooseSaImage() {
    if (!this.data.q || this.data.saUploading) return
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const files = (res && res.tempFiles) || []
        if (!files.length) return
        const f = files[0]
        const name = (f.tempFilePath || '').split('/').pop() || 'image.jpg'
        this.uploadSaFile(f.tempFilePath, name)
      }
    })
  },
  chooseSaFile() {
    if (!this.data.q || this.data.saUploading) return
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      success: (res) => {
        const files = (res && res.tempFiles) || []
        if (!files.length) return
        const f = files[0]
        this.uploadSaFile(f.path, f.name || 'file')
      }
    })
  },
  uploadSaFile(filePath, originalFileName) {
    if (!filePath) return
    const token = wx.getStorageSync('token') || ''
    this.setData({ saUploading: true })
    wx.showLoading({ title: '上传中...' })
    wx.uploadFile({
      url: `${baseURL}/api/student/exams/${encodeURIComponent(this.data.paperId)}/upload-answer-file?courseId=${encodeURIComponent(this.data.courseId)}`,
      filePath,
      name: 'file',
      formData: { originalFileName: String(originalFileName || '').trim().slice(0, 255) },
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success: (res) => {
        wx.hideLoading()
        this.setData({ saUploading: false })
        if (res.statusCode < 200 || res.statusCode >= 300) {
          wx.showToast({ title: '上传失败', icon: 'none' })
          return
        }
        let body = {}
        try { body = JSON.parse(res.data || '{}') } catch (e) {}
        if (!body.fileUrl) {
          wx.showToast({ title: '上传返回异常', icon: 'none' })
          return
        }
        const next = (this.data.answerSaFiles || []).concat([{
          fileUrl: body.fileUrl,
          fileName: body.fileName || originalFileName || '附件',
          fileType: body.fileType || ''
        }])
        this.setData({ answerSaFiles: next })
      },
      fail: () => {
        wx.hideLoading()
        this.setData({ saUploading: false })
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    })
  },
  removeSaFile(e) {
    const url = e && e.currentTarget ? e.currentTarget.dataset.url : ''
    if (!url) return
    const next = (this.data.answerSaFiles || []).filter((x) => x.fileUrl !== url)
    this.setData({ answerSaFiles: next })
  },
  prevQ() {
    this.saveCurrentAnswer()
    const idx = Math.max(0, (Number(this.data.index) || 0) - 1)
    this.setData({ index: idx })
    this.applyQuestion()
  },
  nextQ() {
    this.saveCurrentAnswer()
    const idx = Math.min((this.data.total || 1) - 1, (Number(this.data.index) || 0) + 1)
    this.setData({ index: idx })
    this.applyQuestion()
  },
  async submitPaper() {
    if (this.data.submitting) return
    this.saveCurrentAnswer()
    const pack = this.getPack() || {}
    const answersMap = pack.answers || {}
    const qs = Array.isArray(pack.questions) ? pack.questions : []
    const answers = qs
      .map((q) => {
        const qid = q && q.id != null ? q.id : null
        if (qid == null) return null
        const key = String(qid)
        const ans = answersMap[key] != null ? String(answersMap[key]) : ''
        return { questionId: qid, answer: ans }
      })
      .filter((x) => x)

    this.setData({ submitting: true })
    try {
      const res = await postJson(
        `/api/student/exams/${encodeURIComponent(this.data.paperId)}/attempts/${encodeURIComponent(this.data.attemptId)}/submit?courseId=${encodeURIComponent(this.data.courseId)}`,
        { answers }
      )
      try {
        wx.removeStorageSync(storageKey(this.data.attemptId))
      } catch (e) {}
      wx.showToast({ title: '已提交', icon: 'success' })
      // 返回试卷详情页；让其 onShow 重新拉取结果
      setTimeout(() => {
        wx.navigateBack()
      }, 600)
      // 这里保留 res 给需要时扩展展示
      void res
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '提交失败', icon: 'none' })
      this.setData({ submitting: false })
    }
  }
})

