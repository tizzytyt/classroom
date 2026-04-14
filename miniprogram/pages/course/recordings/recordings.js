const { request } = require('../../../utils/request')
const { getUser } = require('../../../utils/auth')
const { baseURL } = require('../../../utils/config')
const { withFileAccessToken } = require('../../../utils/fileAccess')

function isVideo(fileType, fileName) {
  const t = (fileType || '').toLowerCase()
  const n = (fileName || '').toLowerCase()
  if (t.startsWith('video/')) return true
  return ['.mp4', '.mov', '.avi', '.mkv', '.wmv', '.webm', '.flv', '.m4v'].some(ext => n.endsWith(ext))
}

Page({
  data: {
    courseId: '',
    list: [],
    loading: false,
    isTeacher: false
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
    const rawUrl = item.fileUrl.startsWith('http') ? item.fileUrl : (baseURL + item.fileUrl)
    const url = withFileAccessToken(rawUrl)
    if (!this.data.isTeacher) {
      request(`/api/student/resources/progress?resourceId=${encodeURIComponent(String(item.id))}&status=2&percent=100`, 'POST', {})
        .catch(() => {})
    }
    wx.navigateTo({
      url: `/pages/course/video-player/video-player?url=${encodeURIComponent(url)}&title=${encodeURIComponent(item.title || '录播课')}`
    })
  }
})

