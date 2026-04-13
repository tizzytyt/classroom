const { isLoggedIn, getUser } = require('../../../utils/auth')
const { request } = require('../../../utils/request')

function roleCodeByIndex(i) {
  return i === 0 ? 'STUDENT' : i === 1 ? 'TEACHER' : 'ADMIN'
}

function roleIndexByCode(code) {
  if (code === 'TEACHER') return 1
  if (code === 'ADMIN') return 2
  return 0
}

Page({
  data: {
    mode: 'create',
    saving: false,
    original: null,
    roleOptions: ['学生', '教师', '管理员'],
    roleIndex: 0,
    statusOptions: ['启用', '禁用'],
    statusIndex: 0,
    form: {
      id: null,
      username: '',
      password: '',
      realName: '',
      roleCode: 'STUDENT',
      status: 1,
      phone: '',
      email: ''
    }
  },
  onLoad(query) {
    const mode = query && query.mode ? query.mode : 'create'
    this.setData({ mode })
    wx.setNavigationBarTitle({ title: mode === 'create' ? '新增用户' : '编辑用户' })
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
    if (mode === 'edit') {
      const channel = this.getOpenerEventChannel()
      channel.on('user', user => {
        if (!user) return
        const f = {
          id: user.id,
          username: user.username || '',
          password: '',
          realName: user.realName || '',
          roleCode: user.roleCode || 'STUDENT',
          status: user.status === 0 ? 0 : 1,
          phone: user.phone || '',
          email: user.email || ''
        }
        this.setData({
          original: { ...f },
          form: f,
          roleIndex: roleIndexByCode(f.roleCode),
          statusIndex: f.status === 1 ? 0 : 1
        })
      })
    }
  },
  onField(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    const form = { ...this.data.form }
    form[field] = value
    this.setData({ form })
  },
  onRoleChange(e) {
    const roleIndex = Number(e.detail.value || 0)
    const form = { ...this.data.form }
    form.roleCode = roleCodeByIndex(roleIndex)
    this.setData({ roleIndex, form })
  },
  onStatusChange(e) {
    const statusIndex = Number(e.detail.value || 0)
    const form = { ...this.data.form }
    form.status = statusIndex === 0 ? 1 : 0
    this.setData({ statusIndex, form })
  },
  async submit() {
    const form = this.data.form
    if (!form.username || !String(form.username).trim()) {
      wx.showToast({ title: '请输入用户名', icon: 'none' })
      return
    }
    this.setData({ saving: true })
    try {
      if (this.data.mode === 'create') {
        await request('/api/admin/users', 'POST', {
          username: String(form.username).trim(),
          password: (form.password && String(form.password).trim()) ? String(form.password).trim() : null,
          realName: form.realName,
          roleCode: form.roleCode,
          phone: form.phone,
          email: form.email,
          status: form.status
        })
      } else {
        await request(`/api/admin/users/${form.id}`, 'PUT', {
          realName: form.realName,
          phone: form.phone,
          email: form.email
        })
        if (this.data.original && this.data.original.roleCode !== form.roleCode) {
          await request(`/api/admin/users/${form.id}/role`, 'POST', { roleCode: form.roleCode })
        }
        if (this.data.original && this.data.original.status !== form.status) {
          await request(`/api/admin/users/${form.id}/status`, 'POST', { status: form.status })
        }
      }
      wx.showToast({ title: '保存成功', icon: 'success' })
      try {
        wx.setStorageSync('admin_users_refresh', 1)
      } catch (err) {}
      setTimeout(() => wx.navigateBack(), 400)
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '保存失败', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  }
})
