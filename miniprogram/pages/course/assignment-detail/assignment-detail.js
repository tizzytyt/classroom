const { request, postJson } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')
const { baseURL } = require('../../../utils/config')
const { isPastDueAt } = require('../../../utils/assignmentDue')
const { withFileAccessToken, previewDownloadedFile } = require('../../../utils/fileAccess')

function parseAttachmentUrlList(json) {
  if (!json || typeof json !== 'string') return []
  try {
    const arr = JSON.parse(json)
    return Array.isArray(arr) ? arr.filter(Boolean) : []
  } catch (e) {
    return []
  }
}

Page({
  data: {
    assignmentId: '',
    courseId: '',
    assignment: null,
    submission: null,
    grade: null,
    contentText: '',
    attachmentFiles: [],
    loading: false,
    submitting: false,
    user: null,
    isTeacher: false,
    loadingSubmissions: false,
    teacherSubmissions: [],
    gradingSubmissionId: '',
    currentGradingStudentName: '',
    gradeForm: {
      score: '',
      comment: ''
    },
    gradingSaving: false,
    pastDue: false
  },
  onLoad(options) {
    const id = options.id != null && String(options.id).trim() !== '' ? String(options.id).trim() : ''
    const courseId = options.courseId != null && String(options.courseId).trim() !== '' ? String(options.courseId).trim() : ''
    if (!id) {
      wx.showToast({ title: '缺少作业ID', icon: 'none' })
      return
    }
    const user = getUser()
    const isTeacher = !!(user && user.roleCode === 'TEACHER')
    this.setData({ assignmentId: id, courseId, user, isTeacher })
    this.loadDetail()
  },
  async loadDetail() {
    this.setData({ loading: true })
    try {
      const res = this.data.isTeacher
        ? await request(
            `/api/teacher/courses/${encodeURIComponent(this.data.courseId)}/assignments/${encodeURIComponent(this.data.assignmentId)}`,
            'GET'
          )
        : await request(`/api/student/assignments/${encodeURIComponent(this.data.assignmentId)}`, 'GET')
      if (this.data.isTeacher) {
        const a = res.assignment || null
        this.setData({
          assignment: a,
          submission: null,
          grade: null,
          contentText: '',
          attachmentFiles: [],
          pastDue: isPastDueAt(a && a.dueAt)
        })
        await this.loadTeacherSubmissions()
      } else {
        const attList = res.submissionAttachmentUrls
        let attachmentFiles = []
        if (Array.isArray(attList) && attList.length) {
          attachmentFiles = attList.map((u) => ({
            fileUrl: u,
            fileName: (u || '').split('/').pop() || u
          }))
        }
        const a = res.assignment || null
        this.setData({
          assignment: a,
          submission: res.submission || null,
          grade: res.grade || null,
          contentText: (res.submission && res.submission.submitText) ? res.submission.submitText : '',
          attachmentFiles,
          pastDue: isPastDueAt(a && a.dueAt)
        })
      }
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  onInput(e) {
    if (this.data.grade || this.data.pastDue) return
    this.setData({ contentText: e.detail.value || '' })
  },
  chooseAttachment() {
    if (this.data.isTeacher || this.data.grade || this.data.pastDue) return
    wx.showActionSheet({
      itemList: ['选择文件', '从相册选图片'],
      success: (r) => {
        if (r.tapIndex === 1) {
          wx.chooseMedia({
            count: 9 - (this.data.attachmentFiles || []).length,
            mediaType: ['image'],
            sourceType: ['album', 'camera'],
            success: (mr) => {
              const files = (mr && mr.tempFiles) ? mr.tempFiles : []
              files.forEach((f) => {
                if (!f || !f.tempFilePath) return
                const guessName = (f.tempFilePath || '').split('/').pop() || 'image.jpg'
                this.doUploadAssignmentFile(f.tempFilePath, f.size, 'image/jpeg', guessName)
              })
            },
            fail: () => {}
          })
          return
        }
        wx.chooseMessageFile({
          count: 9 - (this.data.attachmentFiles || []).length,
          type: 'file',
          success: (res2) => {
            const files = res2 && res2.tempFiles ? res2.tempFiles : []
            files.forEach((f) => {
              if (f && f.path) this.doUploadAssignmentFile(f.path, f.size, f.type, f.name)
            })
          },
          fail: () => {}
        })
      }
    })
  },
  doUploadAssignmentFile(filePath, size, type, originalFileName) {
    if (this.data.grade || this.data.pastDue) return
    if ((this.data.attachmentFiles || []).length >= 9) {
      wx.showToast({ title: '最多9个附件', icon: 'none' })
      return
    }
    const token = wx.getStorageSync('token') || ''
    const formData = {}
    if (originalFileName) formData.originalFileName = String(originalFileName).trim().slice(0, 255)
    wx.showLoading({ title: '上传中...' })
    wx.uploadFile({
      url: `${baseURL}/api/student/assignments/${encodeURIComponent(this.data.assignmentId)}/upload`,
      filePath,
      name: 'file',
      formData,
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success: (uploadRes) => {
        wx.hideLoading()
        if (uploadRes.statusCode < 200 || uploadRes.statusCode >= 300) {
          let msg = '上传失败'
          try {
            const body = JSON.parse(uploadRes.data || '{}')
            if (body && body.message) msg = body.message
          } catch (e) {}
          wx.showToast({ title: msg, icon: 'none' })
          return
        }
        let body = {}
        try {
          body = JSON.parse(uploadRes.data || '{}')
        } catch (e) {}
        const fileUrl = body.fileUrl || ''
        const fileName = body.fileName || (originalFileName || '文件')
        if (!fileUrl) {
          wx.showToast({ title: '上传返回异常', icon: 'none' })
          return
        }
        const next = (this.data.attachmentFiles || []).concat([{ fileUrl, fileName }])
        this.setData({ attachmentFiles: next })
        wx.showToast({ title: '已添加', icon: 'success' })
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    })
  },
  removeAttachment(e) {
    if (this.data.grade || this.data.pastDue) return
    const url = e.currentTarget.dataset.url
    if (!url) return
    const attachmentFiles = (this.data.attachmentFiles || []).filter((x) => x.fileUrl !== url)
    this.setData({ attachmentFiles })
  },
  async submit() {
    if (this.data.isTeacher || this.data.grade || this.data.pastDue) return
    const text = (this.data.contentText || '').trim()
    const urls = (this.data.attachmentFiles || []).map((x) => x.fileUrl).filter(Boolean)
    if (!text && !urls.length) {
      wx.showToast({ title: '请填写说明或上传附件', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      await postJson(`/api/student/assignments/${encodeURIComponent(this.data.assignmentId)}/submit`, {
        text,
        attachmentUrls: urls
      })
      wx.showToast({ title: '提交成功', icon: 'success' })
      // 提交成功后返回上一页（上页一般在 onShow 刷新列表/状态）
      setTimeout(() => {
        wx.navigateBack({ delta: 1 })
      }, 500)
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '提交失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },
  openSubmittedFile(e) {
    const url = e.currentTarget.dataset.url
    if (!url) return
    const full = url.startsWith('http') ? url : baseURL + url
    const authFull = withFileAccessToken(full)
    const lower = url.toLowerCase()
    if (/\.(jpg|jpeg|png|gif|webp|bmp)$/.test(lower)) {
      wx.previewImage({ urls: [authFull], current: authFull })
      return
    }
    wx.showLoading({ title: '打开中...' })
    const token = wx.getStorageSync('token') || ''
    wx.downloadFile({
      url: authFull,
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success: (res) => {
        wx.hideLoading()
        if (res.statusCode != null && res.statusCode !== 200) {
          wx.showToast({ title: '打开失败', icon: 'none' })
          return
        }
        const nameFromUrl = (() => {
          const s = String(url || '').split('?')[0]
          const i = s.lastIndexOf('/')
          return i >= 0 ? s.slice(i + 1) : s
        })()
        previewDownloadedFile(res.tempFilePath, nameFromUrl, '', '附件', url)
          .catch(() => wx.showToast({ title: '无法打开', icon: 'none' }))
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '打开失败', icon: 'none' })
      }
    })
  },
  async loadTeacherSubmissions() {
    if (!this.data.isTeacher) return
    this.setData({ loadingSubmissions: true })
    try {
      const list = await request(
        `/api/teacher/courses/${encodeURIComponent(this.data.courseId)}/assignments/${encodeURIComponent(this.data.assignmentId)}/submissions`,
        'GET'
      )
      const raw = Array.isArray(list) ? list : []
      const teacherSubmissions = raw.map((x) => ({
        ...x,
        submissionId: x.submissionId != null ? String(x.submissionId) : '',
        attachmentList: parseAttachmentUrlList(x.attachmentUrls)
      }))
      this.setData({ teacherSubmissions })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载提交列表失败', icon: 'none' })
    } finally {
      this.setData({ loadingSubmissions: false })
    }
  },
  startGrade(e) {
    if (!this.data.isTeacher) return
    const submissionId = e && e.currentTarget ? e.currentTarget.dataset.submissionId : null
    const sid = submissionId != null ? String(submissionId) : ''
    if (!sid) return
    const target = (this.data.teacherSubmissions || []).find((x) => String(x.submissionId) === sid)
    if (target && target.gradeScore != null) {
      wx.showToast({ title: '已批改不可修改', icon: 'none' })
      return
    }
    this.setData({
      gradingSubmissionId: sid,
      currentGradingStudentName: (target && target.studentName) ? target.studentName : '',
      'gradeForm.score': (target && target.gradeScore != null) ? String(target.gradeScore) : '',
      'gradeForm.comment': (target && target.gradeComment != null) ? (target.gradeComment || '') : ''
    })
  },
  onGradeScoreInput(e) {
    this.setData({ 'gradeForm.score': (e.detail.value || '').trim() })
  },
  onGradeCommentInput(e) {
    this.setData({ 'gradeForm.comment': e.detail.value || '' })
  },
  cancelGrade() {
    this.setData({
      gradingSubmissionId: '',
      currentGradingStudentName: '',
      'gradeForm.score': '',
      'gradeForm.comment': '',
      gradingSaving: false
    })
  },
  async saveGrade() {
    if (!this.data.isTeacher || !this.data.gradingSubmissionId) return
    const scoreStr = this.data.gradeForm.score
    if (!scoreStr && scoreStr !== '0') {
      wx.showToast({ title: '请输入分数', icon: 'none' })
      return
    }
    const score = Number(scoreStr)
    if (Number.isNaN(score)) {
      wx.showToast({ title: '分数格式错误', icon: 'none' })
      return
    }
    this.setData({ gradingSaving: true })
    try {
      await request(
        `/api/teacher/courses/${encodeURIComponent(this.data.courseId)}/assignments/${encodeURIComponent(this.data.assignmentId)}/submissions/${encodeURIComponent(this.data.gradingSubmissionId)}/grade`,
        'PUT',
        { score, comment: this.data.gradeForm.comment || '' }
      )
      wx.showToast({ title: '评分保存成功', icon: 'success' })
      this.cancelGrade()
      await this.loadTeacherSubmissions()
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '保存失败', icon: 'none' })
    } finally {
      this.setData({ gradingSaving: false })
    }
  }
})
