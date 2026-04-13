const { login, isLoggedIn, getUser } = require('../../utils/auth')

Page({
  data: {
    selectedRole: 'STUDENT',
    username: '',
    password: '',
    showPassword: false,
    loading: false
  },
  onLoad() {
    try {
      const r = wx.getStorageSync('login_role')
      if (r) this.setData({ selectedRole: r })
    } catch (e) {}
  },
  onShow() {
    if (isLoggedIn()) {
      const u = getUser()
      if (u && u.roleCode === 'ADMIN') wx.reLaunch({ url: '/pages/admin/index/index' })
      else wx.reLaunch({ url: '/pages/index/index' })
    }
  },
  selectRole(e) {
    const role = e.currentTarget.dataset.role
    if (!role) return
    this.setData({ selectedRole: role })
    try {
      wx.setStorageSync('login_role', role)
    } catch (err) {}
  },
  onUsername(e) {
    this.setData({ username: (e.detail.value || '').trim() })
  },
  onPassword(e) {
    this.setData({ password: (e.detail.value || '').trim() })
  },
  togglePassword() {
    this.setData({ showPassword: !this.data.showPassword })
  },
  async submit() {
    if (!this.data.username || !this.data.password) {
      wx.showToast({ title: '请输入用户名和密码', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    try {
      const resp = await login(this.data.username, this.data.password)
      if (resp && resp.roleCode && resp.roleCode !== this.data.selectedRole) {
        try {
          wx.removeStorageSync('token')
          wx.removeStorageSync('user')
        } catch (err) {}
        wx.showToast({ title: '身份不匹配，请选择正确身份', icon: 'none' })
        return
      }
      if (resp && resp.roleCode === 'ADMIN') wx.reLaunch({ url: '/pages/admin/index/index' })
      else wx.reLaunch({ url: '/pages/index/index' })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '登录失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  }
})
