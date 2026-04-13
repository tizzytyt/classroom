const { request } = require('../../../utils/request')
const { baseURL } = require('../../../utils/config')
const { withFileAccessToken, previewDownloadedFile } = require('../../../utils/fileAccess')

Page({
  data: {
    courseId: null,
    list: [],
    loading: false
  },
  onLoad(options) {
    const cid = options && options.courseId != null ? String(options.courseId).trim() : ''
    if (!cid) {
      wx.showToast({ title: '缺少课程ID', icon: 'none' })
      return
    }
    this.setData({ courseId: cid })
    this.load()
  },
  isVideo(fileType, fileName) {
    const t = (fileType || '').toLowerCase()
    const n = (fileName || '').toLowerCase()
    if (t.startsWith('video/')) return true
    return ['.mp4', '.mov', '.avi', '.mkv', '.wmv', '.webm', '.flv', '.m4v'].some(ext => n.endsWith(ext))
  },
  async load() {
    this.setData({ loading: true })
    try {
      const res = await request(`/api/student/recommendations?courseId=${this.data.courseId}`, 'GET')
      this.setData({ list: Array.isArray(res) ? res : [] })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  openRec(e) {
    const item = e.currentTarget.dataset.item
    if (!item) return
    request(`/api/student/recommendations/${item.pushId}/read`, 'POST', {}).catch(() => {})
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
        ).catch(() => wx.showToast({ title: '无法打开', icon: 'none' }))
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '打开失败', icon: 'none' })
      }
    })
  }
})

