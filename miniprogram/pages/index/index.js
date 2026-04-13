const { logout, refreshProfile, getUser, isLoggedIn } = require('../../utils/auth')
const { request } = require('../../utils/request')

Page({
  data: {
    user: getUser(),
    displayName: '-',
    courses: [],
    loadingCourses: false,
    joinCode: '',
    createForm: {
      name: '',
      intro: '',
      assignmentPct: '70',
      checkinPct: '20',
      resourcePct: '10',
      examPct: '0'
    },
    creatingCourse: false
  },
  async onShow() {
    if (!isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.setData({ user: getUser() })
    this.updateDisplayName()
    if (this.data.user && this.data.user.roleCode === 'ADMIN') {
      wx.reLaunch({ url: '/pages/admin/index/index' })
      return
    }
    try {
      const u = await refreshProfile()
      this.setData({ user: u })
      this.updateDisplayName()
    } catch (e) {}
    this.loadCourses()
  },
  updateDisplayName() {
    const u = this.data.user
    const name = u ? (u.realName || u.username || '-') : '-'
    this.setData({ displayName: name })
  },
  onJoinCodeInput(e) {
    this.setData({ joinCode: (e.detail.value || '').trim() })
  },
  onCreateNameInput(e) {
    this.setData({ 'createForm.name': (e.detail.value || '').trim() })
  },
  onCreateIntroInput(e) {
    this.setData({ 'createForm.intro': (e.detail.value || '').trim() })
  },
  onCreateAssignPctInput(e) {
    this.setData({ 'createForm.assignmentPct': (e.detail.value || '').trim() })
  },
  onCreateCheckinPctInput(e) {
    this.setData({ 'createForm.checkinPct': (e.detail.value || '').trim() })
  },
  onCreateResourcePctInput(e) {
    this.setData({ 'createForm.resourcePct': (e.detail.value || '').trim() })
  },
  onCreateExamPctInput(e) {
    this.setData({ 'createForm.examPct': (e.detail.value || '').trim() })
  },
  async joinByCode() {
    if (!this.data.joinCode) {
      wx.showToast({ title: '请输入课程码', icon: 'none' })
      return
    }
    try {
      const code = encodeURIComponent(this.data.joinCode)
      const c = await request(`/api/student/join/code?courseCode=${code}`, 'POST', {})
      wx.showToast({ title: '加入课程成功', icon: 'success' })
      this.setData({ joinCode: '' })
      this.loadCourses()
      if (c && c.id) {
        wx.navigateTo({
          url: `/pages/course/detail/detail?id=${c.id}`
        })
      }
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加入失败', icon: 'none' })
    }
  },
  async createCourse() {
    const user = this.data.user
    if (!user || user.roleCode !== 'TEACHER') {
      wx.showToast({ title: '仅教师可创建课程', icon: 'none' })
      return
    }
    const f = this.data.createForm || {}
    const name = (f.name || '').trim()
    const intro = (f.intro || '').trim()
    if (!name) {
      wx.showToast({ title: '请输入课程名称', icon: 'none' })
      return
    }
    const ap = f.assignmentPct != null && String(f.assignmentPct).trim() !== '' ? Number(f.assignmentPct) : null
    const cp = f.checkinPct != null && String(f.checkinPct).trim() !== '' ? Number(f.checkinPct) : null
    const rp = f.resourcePct != null && String(f.resourcePct).trim() !== '' ? Number(f.resourcePct) : null
    const ep = f.examPct != null && String(f.examPct).trim() !== '' ? Number(f.examPct) : null
    const hasAny = ap != null || cp != null || rp != null || ep != null
    if (hasAny) {
      if (ap == null || cp == null || rp == null || ep == null || [ap, cp, rp, ep].some((n) => Number.isNaN(n) || n < 0)) {
        wx.showToast({ title: '请填写有效的四项占比（0～100）', icon: 'none' })
        return
      }
      if (Math.abs(ap + cp + rp + ep - 100) > 0.5) {
        wx.showToast({ title: '四项占比之和须为 100', icon: 'none' })
        return
      }
    }
    this.setData({ creatingCourse: true })
    try {
      const payload = { name, intro }
      if (hasAny) {
        payload.assignmentWeight = ap / 100
        payload.checkinWeight = cp / 100
        payload.resourceWeight = rp / 100
        payload.examWeight = ep / 100
      }
      const created = await request('/api/teacher/courses', 'POST', payload)
      wx.showToast({ title: '创建成功', icon: 'success' })
      this.setData({
        createForm: { name: '', intro: '', assignmentPct: '70', checkinPct: '20', resourcePct: '10', examPct: '0' }
      })
      await this.loadCourses()
      if (created && created.id) {
        wx.navigateTo({
          url: `/pages/course/detail/detail?id=${encodeURIComponent(String(created.id))}`
        })
      }
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '创建失败', icon: 'none' })
    } finally {
      this.setData({ creatingCourse: false })
    }
  },
  goCourse(e) {
    const id = e.currentTarget.dataset.id
    if (!id) return
    wx.navigateTo({
      url: `/pages/course/detail/detail?id=${encodeURIComponent(String(id))}`
    })
  },
  async loadCourses() {
    this.setData({ loadingCourses: true })
    try {
      const roleCode = this.data.user && this.data.user.roleCode
      const path = roleCode === 'TEACHER' ? '/api/teacher/courses' : '/api/student/courses'
      const list = await request(path, 'GET')
      this.setData({ courses: Array.isArray(list) ? list : [] })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loadingCourses: false })
    }
  },
  doLogout() {
    logout()
  }
})
