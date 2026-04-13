const { isLoggedIn, getUser, refreshProfile, logout } = require('../../../utils/auth')
const { request } = require('../../../utils/request')

Page({
  data: {
    user: getUser(),
    displayName: '-',
    stats: {
      usersTotal: '-',
      usersActive: '-',
      admins: '-',
      teachers: '-',
      students: '-',
      courses: '-',
      resources: '-',
      assignments: '-',
      checkins: '-'
    }
  },
  async onShow() {
    if (!isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const u0 = getUser()
    if (!u0 || u0.roleCode !== 'ADMIN') {
      wx.showToast({ title: '无权限', icon: 'none' })
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.setData({ user: u0 })
    this.updateDisplayName()
    try {
      const u = await refreshProfile()
      this.setData({ user: u })
      this.updateDisplayName()
    } catch (e) {}
    this.loadStats()
  },
  updateDisplayName() {
    const u = this.data.user
    const name = u ? (u.realName || u.username || '-') : '-'
    this.setData({ displayName: name })
  },
  async loadStats() {
    try {
      const s = await request('/api/admin/stats', 'GET')
      this.setData({
        stats: {
          usersTotal: s.usersTotal,
          usersActive: s.usersActive,
          admins: s.admins,
          teachers: s.teachers,
          students: s.students,
          courses: s.courses,
          resources: s.resources,
          assignments: s.assignments,
          checkins: s.checkins
        }
      })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    }
  },
  goUsers() {
    wx.navigateTo({ url: '/pages/admin/users/users' })
  },
  doLogout() {
    logout()
  }
})

