const { isLoggedIn, getUser } = require('../../../utils/auth')
const { request, postJson } = require('../../../utils/request')

function roleName(code) {
  if (code === 'ADMIN') return '管理员'
  if (code === 'TEACHER') return '教师'
  if (code === 'STUDENT') return '学生'
  return code || '-'
}

Page({
  data: {
    keyword: '',
    roleOptions: ['全部角色', '学生', '教师', '管理员'],
    roleIndex: 0,
    statusOptions: ['全部状态', '启用', '禁用'],
    statusIndex: 0,
    page: 1,
    size: 10,
    total: 0,
    items: [],
    loading: false,
    hasMore: true
  },
  onShow() {
    if (!isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const u = getUser()
    if (!u || u.roleCode !== 'ADMIN') {
      wx.showToast({ title: '无权限', icon: 'none' })
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    let refresh = false
    try {
      refresh = !!wx.getStorageSync('admin_users_refresh')
      if (refresh) wx.removeStorageSync('admin_users_refresh')
    } catch (e) {}
    if (refresh || this.data.items.length === 0) this.reload()
  },
  onReachBottom() {
    if (this.data.loading) return
    if (!this.data.hasMore) return
    this.loadMore()
  },
  onPullDownRefresh() {
    this.reload().finally(() => wx.stopPullDownRefresh())
  },
  onKeyword(e) {
    this.setData({ keyword: (e.detail.value || '').trim() })
  },
  onRoleChange(e) {
    this.setData({ roleIndex: Number(e.detail.value || 0) })
  },
  onStatusChange(e) {
    this.setData({ statusIndex: Number(e.detail.value || 0) })
  },
  resetFilter() {
    this.setData({ keyword: '', roleIndex: 0, statusIndex: 0 })
    this.reload()
  },
  doSearch() {
    this.reload()
  },
  buildQuery(page) {
    const roleIndex = this.data.roleIndex
    const statusIndex = this.data.statusIndex
    const roleCode = roleIndex === 1 ? 'STUDENT' : roleIndex === 2 ? 'TEACHER' : roleIndex === 3 ? 'ADMIN' : ''
    const status = statusIndex === 1 ? 1 : statusIndex === 2 ? 0 : null
    const q = {
      page,
      size: this.data.size
    }
    if (this.data.keyword) q.keyword = this.data.keyword
    if (roleCode) q.roleCode = roleCode
    if (status !== null) q.status = status
    return q
  },
  async reload() {
    this.setData({ page: 1, hasMore: true, items: [], total: 0 })
    return this.fetchPage(1, true)
  },
  async loadMore() {
    return this.fetchPage(this.data.page + 1, false)
  },
  async fetchPage(page, replace) {
    this.setData({ loading: true })
    try {
      const res = await request('/api/admin/users', 'GET', this.buildQuery(page))
      const items = Array.isArray(res.items) ? res.items : []
      const normalized = items.map(it => ({
        ...it,
        roleName: roleName(it.roleCode),
        // 后端删除为软删除：status=0 且 username 会追加 _deleted_{id}
        isDeleted: typeof it.username === 'string' && it.username.indexOf('_deleted_') >= 0
      }))
      const merged = replace ? normalized : this.data.items.concat(normalized)
      const total = typeof res.total === 'number' ? res.total : Number(res.total || 0)
      const hasMore = merged.length < total
      this.setData({
        page,
        total,
        items: merged,
        hasMore
      })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  goCreate() {
    wx.navigateTo({ url: '/pages/admin/user-form/user-form?mode=create' })
  },
  findById(id) {
    const list = this.data.items || []
    for (let i = 0; i < list.length; i++) {
      if (String(list[i].id) === String(id)) return list[i]
    }
    return null
  },
  openActions(e) {
    const id = e.currentTarget.dataset.id
    const user = this.findById(id)
    if (!user) return
    if (user.isDeleted) {
      wx.showToast({ title: '该账号已删除', icon: 'none' })
      return
    }
    const actions = ['编辑资料', '修改角色', user.status === 1 ? '禁用账号' : '启用账号', '重置密码(默认123456)', '删除账号']
    wx.showActionSheet({
      itemList: actions,
      success: async r => {
        const idx = r.tapIndex
        if (idx === 0) this.goEdit(user)
        if (idx === 1) this.changeRole(user)
        if (idx === 2) this.toggleStatus(user)
        if (idx === 3) this.resetPassword(user)
        if (idx === 4) this.deleteUser(user)
      }
    })
  },
  goEdit(user) {
    wx.navigateTo({
      url: '/pages/admin/user-form/user-form?mode=edit',
      success: res => {
        res.eventChannel.emit('user', user)
      }
    })
  },
  changeRole(user) {
    wx.showActionSheet({
      itemList: ['学生', '教师', '管理员'],
      success: async r => {
        const roleCode = r.tapIndex === 0 ? 'STUDENT' : r.tapIndex === 1 ? 'TEACHER' : 'ADMIN'
        try {
          await request(`/api/admin/users/${user.id}/role`, 'POST', { roleCode })
          wx.showToast({ title: '已更新', icon: 'success' })
          this.reload()
        } catch (e) {
          wx.showToast({ title: (e && e.message) ? e.message : '操作失败', icon: 'none' })
        }
      }
    })
  },
  toggleStatus(user) {
    const next = user.status === 1 ? 0 : 1
    const title = next === 1 ? '确认启用该账号？' : '确认禁用该账号？'
    wx.showModal({
      title: '提示',
      content: title,
      success: async r => {
        if (!r.confirm) return
        try {
          await request(`/api/admin/users/${user.id}/status`, 'POST', { status: next })
          wx.showToast({ title: '已更新', icon: 'success' })
          this.reload()
        } catch (e) {
          wx.showToast({ title: (e && e.message) ? e.message : '操作失败', icon: 'none' })
        }
      }
    })
  },
  resetPassword(user) {
    wx.showModal({
      title: '重置密码',
      content: '确认把密码重置为默认值 123456？',
      success: async r => {
        if (!r.confirm) return
        try {
          // Spring 后端使用 @RequestBody，需以 JSON 方式提交
          await postJson(`/api/admin/users/${user.id}/reset-password`, {})
          wx.showToast({ title: '已重置', icon: 'success' })
        } catch (e) {
          wx.showToast({ title: (e && e.message) ? e.message : '操作失败', icon: 'none' })
        }
      }
    })
  },
  deleteUser(user) {
    wx.showModal({
      title: '删除账号',
      content: '确认删除该账号？删除后将被禁用且用户名会被释放。',
      confirmColor: '#ff3b30',
      success: async r => {
        if (!r.confirm) return
        try {
          await request(`/api/admin/users/${user.id}`, 'DELETE', {})
          wx.showToast({ title: '已删除', icon: 'success' })
          this.reload()
        } catch (e) {
          wx.showToast({ title: (e && e.message) ? e.message : '操作失败', icon: 'none' })
        }
      }
    })
  }
})
