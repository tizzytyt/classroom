const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')
const { baseURL } = require('../../../utils/config')

function isVideo(fileType, fileName) {
  const t = (fileType || '').toLowerCase()
  const n = (fileName || '').toLowerCase()
  if (t.startsWith('video/')) return true
  return ['.mp4', '.mov', '.avi', '.mkv', '.wmv', '.webm', '.flv', '.m4v'].some(ext => n.endsWith(ext))
}

function isPlayableInWxVideo(fileType, fileName) {
  const t = (fileType || '').toLowerCase()
  const n = (fileName || '').toLowerCase()
  if (t === 'video/mp4' || t === 'application/mp4') return true
  return ['.mp4', '.m4v', '.mov'].some(ext => n.endsWith(ext))
}

function fileNameOnly(name) {
  const raw = String(name || '').trim()
  if (!raw) return ''
  const slash = Math.max(raw.lastIndexOf('/'), raw.lastIndexOf('\\'))
  return slash >= 0 ? raw.slice(slash + 1).trim() : raw
}

function safeDecode(text) {
  const raw = String(text || '')
  if (!raw) return ''
  try {
    return decodeURIComponent(raw)
  } catch (e) {
    return raw
  }
}

Page({
  data: {
    courseId: '',
    list: [],
    loading: false,
    isTeacher: false,
    uploading: false
  },
  parseTitle(fileName) {
    const cleanedName = fileNameOnly(safeDecode(fileName))
    const raw = cleanedName.trim()
    if (!raw) return '录播课'
    const dot = raw.lastIndexOf('.')
    const base = dot <= 0 ? raw : raw.slice(0, dot).trim()
    const title = (base || raw).trim()
    // 临时文件名/随机串/路径残留时，给稳定中文标题，避免乱码观感
    const looksRandom = /^[a-f0-9]{24,}$/i.test(title) || /^wxfile/i.test(title)
    if (!title || looksRandom) {
      return '录播课_' + Date.now()
    }
    return title
  },
  onLoad(options) {
    const cid = options && options.courseId != null ? String(options.courseId).trim() : ''
    if (!cid) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    const user = getUser()
    const isTeacher = !!(user && user.roleCode === 'TEACHER')
    this.setData({ courseId: cid, isTeacher })
    this.loadList()
  },
  async loadList() {
    this.setData({ loading: true })
    try {
      const path = this.data.isTeacher
        ? `/api/teacher/courses/${this.data.courseId}/resources`
        : `/api/student/resources?courseId=${this.data.courseId}`
      const raw = await request(path, 'GET')
      const all = Array.isArray(raw) ? raw : []
      const list = all.filter(item => isVideo(item && item.fileType, item && item.fileName))
      this.setData({ list })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载录播课失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  async openVideo(e) {
    const id = e && e.currentTarget && e.currentTarget.dataset ? e.currentTarget.dataset.id : null
    if (!id) return
    const item = (this.data.list || []).find(x => String(x.id) === String(id))
    if (!item || !item.fileUrl) return
    if (!isPlayableInWxVideo(item.fileType, item.fileName)) {
      wx.showToast({ title: '该视频格式小程序不支持，请转为MP4后上传', icon: 'none' })
      return
    }
    const url = item.fileUrl.startsWith('http') ? item.fileUrl : (baseURL + item.fileUrl)
    if (!this.data.isTeacher) {
      request(`/api/student/resources/progress?resourceId=${encodeURIComponent(String(item.id))}&status=2&percent=100`, 'POST', {})
        .catch(() => {})
    }
    wx.navigateTo({
      url: `/pages/course/video-player/video-player?url=${encodeURIComponent(url)}&title=${encodeURIComponent(item.title || '录播课')}`
    })
  },
  chooseAndUploadRecording() {
    if (!this.data.isTeacher || this.data.uploading) return
    wx.showActionSheet({
      itemList: ['从文件选择（保留文件名）', '从相册/相机选择视频'],
      success: (r) => {
        if (r.tapIndex === 0) {
          wx.chooseMessageFile({
            count: 1,
            type: 'file',
            extension: ['mp4', 'mov', 'm4v', 'avi', 'mkv', 'wmv', 'webm', 'flv'],
            success: (res) => {
              const files = (res && res.tempFiles) ? res.tempFiles : []
              if (!files.length) return
              const f = files[0]
              const guessName = fileNameOnly(f.name || f.path) || 'recording.mp4'
              this.doUploadRecording(f.path, f.size, f.type || 'video/mp4', guessName, this.parseTitle(guessName))
            },
            fail: () => wx.showToast({ title: '未选择文件', icon: 'none' })
          })
          return
        }
        wx.chooseMedia({
          count: 1,
          mediaType: ['video'],
          sourceType: ['album', 'camera'],
          success: (res) => {
            const files = (res && res.tempFiles) ? res.tempFiles : []
            if (!files.length) return
            const f = files[0]
            const guessName = fileNameOnly(f.name || f.tempFilePath) || 'recording.mp4'
            const suggestTitle = this.parseTitle(guessName)
            this.doUploadRecording(f.tempFilePath, f.size, 'video/mp4', guessName, suggestTitle)
          },
          fail: () => wx.showToast({ title: '未选择视频', icon: 'none' })
        })
      }
    })
  },
  doUploadRecording(filePath, size, type, originalFileName, preferredTitle) {
    const token = wx.getStorageSync('token') || ''
    this.setData({ uploading: true })
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
          this.setData({ uploading: false })
          wx.showToast({ title: '上传失败', icon: 'none' })
          return
        }
        let body = {}
        try { body = JSON.parse(uploadRes.data || '{}') } catch (e) {}
        const normalizedTitle = (preferredTitle || '').trim() || this.parseTitle(body.fileName || originalFileName)
        const payload = {
          title: normalizedTitle,
          description: '录播课',
          category: '视频',
          fileUrl: body.fileUrl || '',
          fileName: body.fileName || originalFileName || 'recording.mp4',
          fileSize: Number(body.fileSize != null ? body.fileSize : (size || 0)) || 0,
          fileType: (body.fileType || type || 'video/mp4').trim()
        }
        if (!payload.fileUrl || !payload.fileName) {
          this.setData({ uploading: false })
          wx.showToast({ title: '上传返回异常', icon: 'none' })
          return
        }
        wx.showLoading({ title: '发布中...' })
        request(`/api/teacher/courses/${this.data.courseId}/resources`, 'POST', payload)
          .then(async () => {
            wx.hideLoading()
            this.setData({ uploading: false })
            wx.showToast({ title: '上传成功', icon: 'success' })
            await this.loadList()
          })
          .catch((e) => {
            wx.hideLoading()
            this.setData({ uploading: false })
            wx.showToast({ title: (e && e.message) ? e.message : '发布失败', icon: 'none' })
          })
      },
      fail: () => {
        wx.hideLoading()
        this.setData({ uploading: false })
        wx.showToast({ title: '上传失败', icon: 'none' })
      }
    })
  }
})

