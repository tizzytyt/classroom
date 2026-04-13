Page({
  data: {
    url: '',
    title: '视频播放'
  },
  onLoad(options) {
    const url = options && options.url ? decodeURIComponent(options.url) : ''
    const title = options && options.title ? decodeURIComponent(options.title) : '视频播放'
    if (!url) {
      wx.showToast({ title: '缺少视频地址', icon: 'none' })
      return
    }
    this.setData({ url, title })
    wx.setNavigationBarTitle({ title })
  }
})

