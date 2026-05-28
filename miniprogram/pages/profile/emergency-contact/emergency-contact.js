const { isLoggedIn, getUser } = require('../../../utils/auth')
const { request, putJson } = require('../../../utils/request')

const RELATION_OPTIONS = ['父亲', '母亲', '配偶', '兄弟姐妹', '其他']

function relationIndexByValue(value) {
  const idx = RELATION_OPTIONS.indexOf(value || '')
  return idx >= 0 ? idx : 0
}

Page({
  data: {
    loading: true,
    saving: false,
    relationOptions: RELATION_OPTIONS,
    relationIndex: 0,
    form: {
      emergencyContactName: '',
      emergencyContactPhone: '',
      emergencyContactRelation: ''
    }
  },
  onShow() {
    if (!isLoggedIn()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    const user = getUser()
    if (!user || user.roleCode !== 'STUDENT') {
      wx.showToast({ title: '仅学生可填写', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 400)
      return
    }
    this.loadContact()
  },
  async loadContact() {
    this.setData({ loading: true })
    try {
      const data = await request('/api/student/profile/emergency-contact', 'GET')
      const form = {
        emergencyContactName: (data && data.emergencyContactName) || '',
        emergencyContactPhone: (data && data.emergencyContactPhone) || '',
        emergencyContactRelation: (data && data.emergencyContactRelation) || ''
      }
      this.setData({
        form,
        relationIndex: relationIndexByValue(form.emergencyContactRelation)
      })
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '加载失败', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  onField(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    const form = { ...this.data.form }
    form[field] = value
    this.setData({ form })
  },
  onRelationChange(e) {
    const relationIndex = Number(e.detail.value || 0)
    const form = { ...this.data.form }
    form.emergencyContactRelation = RELATION_OPTIONS[relationIndex] || ''
    this.setData({ relationIndex, form })
  },
  async submit() {
    const form = this.data.form
    const name = (form.emergencyContactName || '').trim()
    const phone = (form.emergencyContactPhone || '').trim()
    const relation = (form.emergencyContactRelation || '').trim()
    if (!name) {
      wx.showToast({ title: '请填写联络人姓名', icon: 'none' })
      return
    }
    if (!phone) {
      wx.showToast({ title: '请填写联络人电话', icon: 'none' })
      return
    }
    if (!relation) {
      wx.showToast({ title: '请选择与本人关系', icon: 'none' })
      return
    }
    this.setData({ saving: true })
    try {
      await putJson('/api/student/profile/emergency-contact', {
        emergencyContactName: name,
        emergencyContactPhone: phone,
        emergencyContactRelation: relation
      })
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 400)
    } catch (e) {
      wx.showToast({ title: (e && e.message) ? e.message : '保存失败', icon: 'none' })
    } finally {
      this.setData({ saving: false })
    }
  }
})
