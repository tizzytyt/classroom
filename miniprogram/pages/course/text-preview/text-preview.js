Page({
  data: {
    content: ''
  },
  onLoad(options) {
    const key = options.storageKey ? decodeURIComponent(options.storageKey) : ''
    const title = options.title ? decodeURIComponent(options.title) : '文本预览'
    let content = ''
    try {
      if (key) {
        content = wx.getStorageSync(key) || ''
        wx.removeStorageSync(key)
      }
    } catch (e) {}
    wx.setNavigationBarTitle({ title: title.length > 18 ? title.slice(0, 18) + '…' : title })
    this.setData({ content: content || '（无内容或已过期，请重新打开）' })
  }
})
