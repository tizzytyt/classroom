const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')
const { baseURL } = require('../../../utils/config')
const { withFileAccessToken, previewDownloadedFile } = require('../../../utils/fileAccess')

Page({
  data: {
    courseId: null,
    list: [],
    loading: false,
    user: null,
    isTeacher: false,
    learnedMap: {},
    uploading: false,
    publishForm: {
      title: '',
      description: '',
      category: ''
    },
    editingResourceId: null,
    editForm: {
      title: '',
      description: '',
      category: ''
    },
    savingEdit: false
  },
  isItemLearned(item) {
    if (!item || typeof item !== 'object') return false
    // 兼容后端可能返回的不同进度字段
    const status = Number(item.learnStatus != null ? item.learnStatus : item.status)
    const percent = Number(item.learnPercent != null ? item.learnPercent : item.percent)
    return !!(item.learned || item.isLearned || status === 2 || percent >= 100)
  },
  setLearned(resourceId) {
    if (!resourceId) return
    const key = String(resourceId)
    const nextList = (this.data.list || []).map((item) => {
      if (String(item.id) !== key) return item
      return Object.assign({}, item, { learned: true })
    })
    this.setData({
      [`learnedMap.${key}`]: true,
      list: nextList
    })
  },
  syncLearnedProgress(resourceId) {
    if (this.data.isTeacher || !resourceId) return
    const id = String(resourceId)
    request(`/api/student/resources/progress?resourceId=${encodeURIComponent(id)}&status=2&percent=100`, 'POST', {})
      .catch(() => {})
  },
  isVideo(fileType, fileName) {
    const t = (fileType || '').toLowerCase()
    const n = (fileName || '').toLowerCase()
    if (t.startsWith('video/')) return true
    return ['.mp4', '.mov', '.avi', '.mkv', '.wmv', '.webm', '.flv', '.m4v'].some(ext => n.endsWith(ext))
  },
  onLoad(options) {
    const cid = options && options.courseId != null ? String(options.courseId).trim() : ''
    if (!cid) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    const user = getUser()
    const isTeacher = !!(user && user.roleCode === 'TEACHER')
    this.setData({
      courseId: cid,
      user,
      isTeacher
    })
    this.loadResources(isTeacher)
  },
  async loadResources(isTeacherOverride) {
    const isTeacher = typeof isTeacherOverride === 'boolean' ? isTeacherOverride : this.data.isTeacher
    this.setData({ loading: true })
    try {
      const res = isTeacher
        ? await request(`/api/teacher/courses/${this.data.courseId}/resources`, 'GET')
        : await request(`/api/student/resources?courseId=${this.data.courseId}`, 'GET')
      const rawList = Array.isArray(res) ? res : []
      const learnedMap = {}
      const list = rawList.map((item) => {
        const learned = this.isItemLearned(item)
        const idKey = item && item.id != null ? String(item.id) : ''
        if (idKey && learned) learnedMap[idKey] = true
        return Object.assign({}, item, { learned })
      })
      this.setData({ list, learnedMap })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载资源失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  async markComplete(e) {
    if (this.data.isTeacher) return
    const id = e.currentTarget.dataset.id
    if (!id) return
    try {
      await request(`/api/student/resources/progress?resourceId=${encodeURIComponent(String(id))}&status=2&percent=100`, 'POST', {})
      wx.showToast({ title: '已标记完成', icon: 'success' })
      this.loadResources()
    } catch (err) {
      wx.showToast({ title: (err && err.message) ? err.message : '操作失败', icon: 'none' })
    }
  },

  openResource(e) {
    if (this.data.isTeacher) return
    const id = e.currentTarget.dataset.id
    if (!id) return
    const item = (this.data.list || []).find(x => String(x.id) === String(id))
    if (!item) return

    // 打开即视为已学习（本地立即展示 ✅，同时后台同步进度）
    this.setLearned(item.id)
    this.syncLearnedProgress(item.id)

    const rawUrl = item.fileUrl ? (item.fileUrl.startsWith('http') ? item.fileUrl : (baseURL + item.fileUrl)) : ''
    if (!rawUrl) {
      wx.showToast({ title: '文件URL为空', icon: 'none' })
      return
    }
    const url = withFileAccessToken(rawUrl)
    if (this.isVideo(item.fileType, item.fileName)) {
      wx.navigateTo({
        url: `/pages/course/video-player/video-player?url=${encodeURIComponent(url)}&title=${encodeURIComponent(item.title || '视频')}`
      })
      return
    }

    wx.showLoading({ title: '打开中...' })
    wx.downloadFile({
      url,
      header: (() => {
        const token = wx.getStorageSync('token') || ''
        return token ? { Authorization: 'Bearer ' + token } : {}
      })(),
      success: (res) => {
        wx.hideLoading()
        if (res.statusCode != null && res.statusCode !== 200) {
          wx.showToast({ title: '打开失败', icon: 'none' })
          return
        }
        previewDownloadedFile(
          res.tempFilePath,
          item.fileName,
          item.fileType,
          item.title || item.fileName,
          item.fileUrl
        ).catch(() => wx.showToast({ title: '无法打开该文件', icon: 'none' }))
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '打开失败', icon: 'none' })
      }
    })
  },

  onPublishTitleInput(e) {
    this.setData({ 'publishForm.title': (e.detail.value || '').trim() })
  },
  onPublishDescInput(e) {
    this.setData({ 'publishForm.description': (e.detail.value || '').trim() })
  },
  onPublishCategoryInput(e) {
    this.setData({ 'publishForm.category': (e.detail.value || '').trim() })
  },
  parseTitleAndCategory(fileName) {
    const raw = (fileName || '').trim()
    if (!raw) return { title: '', category: '' }
    const dot = raw.lastIndexOf('.')
    if (dot <= 0 || dot === raw.length - 1) {
      return { title: raw, category: 'other' }
    }
    return {
      title: raw.slice(0, dot).trim() || raw,
      category: raw.slice(dot + 1).trim().toLowerCase() || 'other'
    }
  },
  applyPublishFormByFileName(fileName) {
    const parsed = this.parseTitleAndCategory(fileName)
    this.setData({
      'publishForm.title': parsed.title,
      'publishForm.category': parsed.category
    })
  },
  chooseAndUploadFile() {
    if (!this.data.isTeacher || this.data.uploading) return
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
              this.applyPublishFormByFileName(guessName)
              this.doUploadFile(f.tempFilePath, f.size, 'video/mp4', guessName)
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
            this.applyPublishFormByFileName(f.name)
            this.doUploadFile(f.path, f.size, f.type, f.name)
          },
          fail: () => wx.showToast({ title: '未选择文件', icon: 'none' })
        })
      }
    })
  },

  doUploadFile(filePath, size, type, originalFileName) {
    const token = wx.getStorageSync('token') || ''
    const form = this.data.publishForm || {}
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
          let msg = '上传失败'
          try {
            const errBody = JSON.parse(uploadRes.data || '{}')
            if (errBody && errBody.message) msg = errBody.message
          } catch (e) {}
          wx.showToast({ title: msg, icon: 'none' })
          return
        }
        let body = {}
        try { body = JSON.parse(uploadRes.data || '{}') } catch (e) {}
        const fileUrl = body.fileUrl || ''
        const fileName = body.fileName || ''
        const fileSize = body.fileSize != null ? body.fileSize : (size || 0)
        const fileType = (body.fileType || type || '').trim() || 'application/octet-stream'
        if (!fileUrl || !fileName) {
          this.setData({ uploading: false })
          wx.showToast({ title: '上传返回数据异常', icon: 'none' })
          return
        }
        const payload = {
          title: (form.title || '').trim(),
          description: (form.description || '').trim(),
          category: (form.category || '').trim(),
          fileUrl,
          fileName,
          fileSize: Number(fileSize) || 0,
          fileType
        }
        wx.showLoading({ title: '发布中...' })
        request(`/api/teacher/courses/${this.data.courseId}/resources`, 'POST', payload)
          .then(async () => {
            wx.hideLoading()
            wx.showToast({ title: '发布成功', icon: 'success' })
            this.setData({
              uploading: false,
              publishForm: {
                title: '',
                description: '',
                category: ''
              }
            })
            await this.loadResources()
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
  },
  startEdit(e) {
    if (!this.data.isTeacher) return
    const resourceId = e && e.currentTarget ? e.currentTarget.dataset.id : null
    if (!resourceId) return
    const rid = String(resourceId)
    const target = (this.data.list || []).find(x => String(x.id) === rid)
    this.setData({
      editingResourceId: rid,
      'editForm.title': (target && target.title) || '',
      'editForm.description': (target && target.description) || '',
      'editForm.category': (target && target.category) || ''
    })
  },
  onEditTitleInput(e) {
    this.setData({ 'editForm.title': (e.detail.value || '').trim() })
  },
  onEditDescInput(e) {
    this.setData({ 'editForm.description': (e.detail.value || '').trim() })
  },
  onEditCategoryInput(e) {
    this.setData({ 'editForm.category': (e.detail.value || '').trim() })
  },
  cancelEdit() {
    this.setData({
      editingResourceId: null,
      'editForm.title': '',
      'editForm.description': '',
      'editForm.category': '',
      savingEdit: false
    })
  },
  async saveEdit() {
    if (!this.data.isTeacher || !this.data.editingResourceId) return
    const payload = {
      title: (this.data.editForm.title || '').trim(),
      description: (this.data.editForm.description || '').trim(),
      category: (this.data.editForm.category || '').trim()
    }
    if (!payload.title) {
      wx.showToast({ title: '请输入资源标题', icon: 'none' })
      return
    }
    if (!payload.category) {
      wx.showToast({ title: '请输入资源分类', icon: 'none' })
      return
    }
    this.setData({ savingEdit: true })
    try {
      const rid = encodeURIComponent(String(this.data.editingResourceId))
      await request(`/api/teacher/courses/${this.data.courseId}/resources/${rid}`, 'PUT', payload)
      wx.showToast({ title: '保存成功', icon: 'success' })
      this.cancelEdit()
      await this.loadResources()
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '保存失败', icon: 'none' })
    } finally {
      this.setData({ savingEdit: false })
    }
  },
  async deleteResource(e) {
    if (!this.data.isTeacher) return
    const resourceId = e && e.currentTarget ? e.currentTarget.dataset.id : null
    if (!resourceId) return
    const rid = String(resourceId)
    wx.showModal({
      title: '删除资源',
      content: '确认删除该资源？删除后学生将无法再访问。',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await request(`/api/teacher/courses/${this.data.courseId}/resources/${encodeURIComponent(rid)}`, 'DELETE', {})
          wx.showToast({ title: '删除成功', icon: 'success' })
          if (String(this.data.editingResourceId) === rid) this.cancelEdit()
          await this.loadResources()
        } catch (err) {
          wx.showToast({ title: (err && err.message) ? err.message : '删除失败', icon: 'none' })
        }
      }
    })
  }
})

