const { request } = require('../../../utils/request')
const { baseURL } = require('../../../utils/config')

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

/** 默认截止：7 天后当天 23:59 */
function defaultDuePicker() {
  const due = new Date()
  due.setDate(due.getDate() + 7)
  due.setHours(23, 59, 0, 0)
  return {
    dueDate: formatDate(due),
    dueTime: formatTime(due)
  }
}

Page({
  data: {
    courseId: '',
    creating: false,
    dueDate: '',
    dueTime: '',
    /** 教师上传的作业附件（选填），对应后端 attachmentUrl */
    attachmentUrl: '',
    attachmentFileName: '',
    attachmentUploading: false,
    createForm: {
      title: '',
      content: '',
      totalScore: ''
    }
  },
  resetDuePicker() {
    const r = defaultDuePicker()
    this.setData({ dueDate: r.dueDate, dueTime: r.dueTime })
  },
  onLoad(options) {
    const cid = options.courseId != null && String(options.courseId).trim() !== '' ? String(options.courseId).trim() : ''
    if (!cid) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    this.setData({ courseId: cid })
    this.resetDuePicker()
  },
  onDueDateChange(e) {
    this.setData({ dueDate: e.detail.value || '' })
  },
  onDueTimeChange(e) {
    this.setData({ dueTime: e.detail.value || '' })
  },
  onCreateTitleInput(e) {
    this.setData({ 'createForm.title': (e.detail.value || '').trim() })
  },
  onCreateContentInput(e) {
    this.setData({ 'createForm.content': e.detail.value || '' })
  },
  onCreateTotalScoreInput(e) {
    this.setData({ 'createForm.totalScore': (e.detail.value || '').trim() })
  },
  chooseAssignmentAttachment() {
    if (this.data.attachmentUploading || this.data.creating) return
    wx.showActionSheet({
      itemList: ['选择任意文件', '从相册/相机选择视频'],
      success: (r) => {
        if (r.tapIndex === 1) {
          wx.chooseMedia({
            count: 1,
            mediaType: ['video'],
            sourceType: ['album', 'camera'],
            success: (mr) => {
              const files = (mr && mr.tempFiles) ? mr.tempFiles : []
              if (!files.length) return
              const f = files[0]
              const guessName = (f.tempFilePath || '').split('/').pop() || 'video.mp4'
              this.doUploadAssignmentAttachment(f.tempFilePath, guessName)
            },
            fail: () => wx.showToast({ title: '未选择视频', icon: 'none' })
          })
          return
        }
        wx.chooseMessageFile({
          count: 1,
          type: 'file',
          success: (res) => {
            const files = res && res.tempFiles ? res.tempFiles : []
            if (!files.length) return
            const f = files[0]
            this.doUploadAssignmentAttachment(f.path, f.name)
          },
          fail: () => wx.showToast({ title: '未选择文件', icon: 'none' })
        })
      }
    })
  },
  doUploadAssignmentAttachment(filePath, originalFileName) {
    const token = wx.getStorageSync('token') || ''
    this.setData({ attachmentUploading: true })
    wx.showLoading({ title: '上传中...' })
    const formData = {}
    if (originalFileName) {
      formData.originalFileName = String(originalFileName).trim().slice(0, 255)
    }
    wx.uploadFile({
      url: `${baseURL}/api/teacher/courses/${this.data.courseId}/resources/upload`,
      filePath,
      name: 'file',
      formData,
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success: (uploadRes) => {
        wx.hideLoading()
        if (uploadRes.statusCode < 200 || uploadRes.statusCode >= 300) {
          this.setData({ attachmentUploading: false })
          let msg = '上传失败'
          try {
            const errBody = JSON.parse(uploadRes.data || '{}')
            if (errBody && errBody.message) msg = errBody.message
          } catch (e) {}
          wx.showToast({ title: msg, icon: 'none' })
          return
        }
        let body = {}
        try {
          body = JSON.parse(uploadRes.data || '{}')
        } catch (e) {}
        const fileUrl = body.fileUrl || ''
        const fileName = (body.fileName || '').trim() || (originalFileName || '').trim() || '附件'
        if (!fileUrl) {
          this.setData({ attachmentUploading: false })
          wx.showToast({ title: '上传返回数据异常', icon: 'none' })
          return
        }
        this.setData({
          attachmentUploading: false,
          attachmentUrl: fileUrl,
          attachmentFileName: fileName
        })
        wx.showToast({ title: '附件已上传', icon: 'success' })
      },
      fail: () => {
        wx.hideLoading()
        this.setData({ attachmentUploading: false })
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    })
  },
  clearAssignmentAttachment() {
    if (this.data.attachmentUploading) return
    this.setData({ attachmentUrl: '', attachmentFileName: '' })
  },
  async createAssignment() {
    const f = this.data.createForm || {}
    if (!f.title) {
      wx.showToast({ title: '请输入作业标题', icon: 'none' })
      return
    }
    const { dueDate, dueTime } = this.data
    const dueAt = toDateTimePayload(dueDate, dueTime)
    if (!dueAt) {
      wx.showToast({ title: '请完整选择截止的日期、时间', icon: 'none' })
      return
    }
    const dueMs = new Date(dueAt.replace(/-/g, '/')).getTime()
    if (!Number.isFinite(dueMs) || dueMs <= Date.now()) {
      wx.showToast({ title: '截止时间须晚于当前时间', icon: 'none' })
      return
    }
    const payload = {
      title: f.title,
      content: f.content || '',
      dueAt,
      totalScore: f.totalScore ? Number(f.totalScore) : null,
      attachmentUrl: this.data.attachmentUrl ? this.data.attachmentUrl : null
    }
    this.setData({ creating: true })
    try {
      await request(`/api/teacher/courses/${this.data.courseId}/assignments`, 'POST', payload)
      wx.showToast({ title: '创建成功', icon: 'success' })
      wx.navigateBack()
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '创建失败', icon: 'none' })
      this.setData({ creating: false })
    }
  }
})

